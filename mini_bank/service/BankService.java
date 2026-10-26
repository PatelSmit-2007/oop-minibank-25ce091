package project.mini_bank.service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import project.mini_bank.exception.AccountNotFoundException;
import project.mini_bank.exception.BankException;
import project.mini_bank.exception.InvalidAmountException;
import project.mini_bank.model.Account;
import project.mini_bank.model.CurrentAccount;
import project.mini_bank.model.FixedDepositAccount;
import project.mini_bank.model.SavingsAccount;
import project.mini_bank.model.annotation.Audit;
import project.mini_bank.repository.Repository;
import project.mini_bank.util.AuditLogger;
import project.mini_bank.util.ReportGenerator;
import project.mini_bank.util.StatementFormatter;
import project.mini_bank.util.TransactionLog;

public class BankService {

    private final Repository<Account> repository;
    private final TransactionProcessor processor;
    private Path logFile;

    public BankService(Repository<Account> repository) {
        this.repository = repository;
        this.processor = new TransactionProcessor();
    }

    public void setLogFile(Path logFile) {
        this.logFile = logFile;
    }

    @Audit(action = "OPEN_ACCOUNT")
    public Account openAccount(String accountType, String ownerName, long initialBalance, long extraParameter) {
        AuditLogger.log(BankService.class, "openAccount", accountType, ownerName, initialBalance, extraParameter);

        Account account;
        switch (accountType.trim().toUpperCase()) {
            case "SAVINGS" -> account = new SavingsAccount(ownerName, initialBalance, extraParameter);
            case "CURRENT" -> account = new CurrentAccount(ownerName, initialBalance, extraParameter);
            case "FIXED_DEPOSIT", "FD" -> account = new FixedDepositAccount(ownerName, initialBalance);
            default -> throw new IllegalArgumentException("Unknown account type: " + accountType);
        }

        repository.save(account);
        processor.registerAccount(account);
        recordLog("OPEN_ACCOUNT " + account.getAccountNumber() + " " + initialBalance);
        return account;
    }

    @Audit(action = "DEPOSIT")
    public void deposit(String accountNumber, long amount) throws BankException {
        AuditLogger.log(BankService.class, "deposit", accountNumber, amount);
        Account account = findAccount(accountNumber);
        account.deposit(amount);
        recordLog("DEPOSIT " + accountNumber + " " + amount);
    }

    @Audit(action = "WITHDRAW")
    public boolean withdraw(String accountNumber, long amount) throws BankException {
        AuditLogger.log(BankService.class, "withdraw", accountNumber, amount);
        Account account = findAccount(accountNumber);
        boolean result = account.withdraw(amount);
        recordLog("WITHDRAW " + accountNumber + " " + amount);
        return result;
    }

    @Audit(action = "TRANSFER")
    public void transfer(String fromAccountNumber, String toAccountNumber, long amount) throws BankException {
        AuditLogger.log(BankService.class, "transfer", fromAccountNumber, toAccountNumber, amount);
        Account fromAccount = findAccount(fromAccountNumber);
        Account toAccount = findAccount(toAccountNumber);
        fromAccount.transfer(toAccount, amount);
        recordLog("TRANSFER " + fromAccountNumber + "->" + toAccountNumber + " " + amount);
    }

    @Audit(action = "STATEMENT")
    public String statement(String accountNumber) throws BankException {
        AuditLogger.log(BankService.class, "statement", accountNumber);
        Account account = findAccount(accountNumber);
        return StatementFormatter.buildStatement(account);
    }

    @Audit(action = "LIST_ACCOUNTS")
    public List<Account> listAccounts() {
        AuditLogger.log(BankService.class, "listAccounts");
        return new ArrayList<>(repository.findAll());
    }

    public void processBatch(List<Runnable> tasks) {
        if (tasks != null) {
            for (Runnable task : tasks) {
                processor.submit(task);
            }
        }
    }

    public void stopProcessor() {
        processor.stop();
    }

    public String generateReport(Path logsFolder, Path reportFile) throws IOException {
        return ReportGenerator.generate(logsFolder, reportFile);
    }

    public Repository<Account> getRepository() {
        return repository;
    }

    private Account findAccount(String accountNumber) throws AccountNotFoundException {
        if (accountNumber == null || accountNumber.isBlank()) {
            throw new AccountNotFoundException("Account number cannot be null or empty.");
        }
        Account account = repository.findById(accountNumber);
        if (account == null) {
            throw new AccountNotFoundException("Account " + accountNumber + " does not exist.");
        }
        return account;
    }

    private void recordLog(String entry) {
        if (logFile != null) {
            try {
                TransactionLog.append(logFile, entry);
            } catch (IOException ignored) {
            }
        }
    }
}
