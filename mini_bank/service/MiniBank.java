package project.mini_bank.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import project.mini_bank.exception.AccountNotFoundException;
import project.mini_bank.exception.BankException;
import project.mini_bank.exception.InsufficientFundsException;
import project.mini_bank.exception.InvalidAmountException;
import project.mini_bank.model.Account;
import project.mini_bank.model.CurrentAccount;
import project.mini_bank.model.FixedDepositAccount;
import project.mini_bank.model.Premium;
import project.mini_bank.model.SavingsAccount;
import project.mini_bank.repository.Repository;
import project.mini_bank.test.MiniBankTest;
import project.mini_bank.util.AnnotationValidator;
import project.mini_bank.util.ReportGenerator;
import project.mini_bank.util.StatePersister;
import project.mini_bank.util.TransactionLog;
import project.mini_bank.util.TransactionSession;

import static project.mini_bank.util.StatementFormatter.buildStatement;

public class MiniBank {

    record BankInfo(String name, String branch) {
    }

    enum MenuOption {
        OPEN_ACCOUNT,
        DEPOSIT,
        WITHDRAW,
        TRANSFER,
        STATEMENT,
        LIST_ACCOUNTS,
        BATCH_PROCESS,
        GENERATE_REPORT,
        RUN_TESTS,
        EXIT
    }

    public static void main(String[] args) {

        BankInfo info = new BankInfo("Mini Bank", "Main Branch");
        System.out.println("=================================================");
        System.out.println("       " + info.name() + " (" + info.branch() + ")");
        System.out.println("  MILESTONE 3 CAPSTONE APPLICATION INITIALIZING  ");
        System.out.println("=================================================");

        // Data paths for persistence and logging
        Path dataDir = Paths.get("data");
        Path stateFile = dataDir.resolve("accounts.ser");
        Path logsDir = dataDir.resolve("logs");
        Path logFile = logsDir.resolve("transactions_capstone.log");
        Path reportFile = dataDir.resolve("reports").resolve("end_of_day_report.txt");

        // Requirement 1 & 2: Repository and BankService wiring
        Repository<Account> repository = new Repository<>();
        BankService bankService = new BankService(repository);
        bankService.setLogFile(logFile);

        // Requirement 2: Load saved accounts on start-up (Practical 11)
        if (Files.exists(stateFile)) {
            try {
                System.out.println("\n[STARTUP] Loading persisted accounts from " + stateFile + "...");
                Account[] loaded = StatePersister.load(stateFile);
                repository.saveAll(loaded);
                System.out.println("[STARTUP] Restored " + loaded.length + " account(s) into Repository.");
            } catch (Exception e) {
                System.err.println("[STARTUP] Could not load state: " + e.getMessage());
            }
        }

        // If repository is empty, seed baseline accounts
        if (repository.findAll().isEmpty()) {
            System.out.println("\n[STARTUP] Seeding baseline accounts into BankService...");
            bankService.openAccount("SAVINGS", "Smit", 5000, 1000);
            bankService.openAccount("CURRENT", "Rahul", 2000, 3000);
            bankService.openAccount("FIXED_DEPOSIT", "Amit", 10000, 0);
        }

        List<Account> currentAccounts = bankService.listAccounts();
        Account primaryAccount = currentAccounts.get(0);
        Account secondaryAccount = currentAccounts.size() > 1 ? currentAccounts.get(1) : primaryAccount;

        System.out.println("\n=== PRACTICAL 1 INTERACTIVE CAPSTONE MENU DISPATCHER ===");
        for (MenuOption option : MenuOption.values()) {
            System.out.println("\n--- Executing Menu Option: " + option + " ---");
            switch (option) {
                case OPEN_ACCOUNT -> {
                    Account newAcc = bankService.openAccount("SAVINGS", "Priya", 15000, 2000);
                    System.out.println("Successfully opened: " + newAcc);
                }
                case DEPOSIT -> {
                    try {
                        System.out.println("Depositing ₹2500 into " + primaryAccount.getAccountNumber() + "...");
                        bankService.deposit(primaryAccount.getAccountNumber(), 2500);
                        System.out.println("Updated: " + primaryAccount);
                    } catch (BankException e) {
                        System.err.println("Deposit failed: " + e.getMessage());
                    }
                }
                case WITHDRAW -> {
                    try {
                        System.out.println("Withdrawing ₹1000 from " + primaryAccount.getAccountNumber() + "...");
                        bankService.withdraw(primaryAccount.getAccountNumber(), 1000);
                        System.out.println("Updated: " + primaryAccount);
                    } catch (BankException e) {
                        System.err.println("Withdrawal failed: " + e.getMessage());
                    }
                }
                case TRANSFER -> {
                    try {
                        System.out.println("Transferring ₹1500 from " + primaryAccount.getAccountNumber()
                                + " to " + secondaryAccount.getAccountNumber() + "...");
                        bankService.transfer(primaryAccount.getAccountNumber(), secondaryAccount.getAccountNumber(), 1500);
                        System.out.println("Transfer completed successfully.");
                    } catch (BankException e) {
                        System.err.println("Transfer failed: " + e.getMessage());
                    }
                }
                case STATEMENT -> {
                    try {
                        System.out.println(bankService.statement(primaryAccount.getAccountNumber()));
                    } catch (BankException e) {
                        System.err.println("Statement error: " + e.getMessage());
                    }
                }
                case LIST_ACCOUNTS -> {
                    System.out.println("Active Accounts in MiniBank:");
                    for (Account acc : bankService.listAccounts()) {
                        System.out.println(" * " + acc);
                    }
                }
                case BATCH_PROCESS -> {
                    // Requirement 3: Safe batch processing over thread pool
                    System.out.println("Submitting batch transactions to TransactionProcessor...");
                    List<Runnable> batch = new ArrayList<>();
                    for (int i = 1; i <= 6; i++) {
                        final int idx = i;
                        batch.add(() -> {
                            try {
                                if (idx % 2 == 0) {
                                    bankService.deposit(primaryAccount.getAccountNumber(), 200);
                                } else {
                                    bankService.withdraw(primaryAccount.getAccountNumber(), 100);
                                }
                            } catch (BankException e) {
                                System.err.println("Batch task error: " + e.getMessage());
                            }
                        });
                    }
                    bankService.processBatch(batch);
                    System.out.println("Batch tasks dispatched to 4-worker thread pool.");
                }
                case GENERATE_REPORT -> {
                    // Requirement 3: Report generation as menu option
                    try {
                        System.out.println("Generating End-of-Day Report via NIO...");
                        String report = bankService.generateReport(logsDir, reportFile);
                        System.out.println(report);
                    } catch (Exception e) {
                        System.err.println("Report generation error: " + e.getMessage());
                    }
                }
                case RUN_TESTS -> {
                    // Requirement 4: Running automated JUnit tests with @Test
                    MiniBankTest.runAllTests();
                }
                case EXIT -> {
                    // Requirement 2: Save accounts on exit (Practical 11)
                    bankService.stopProcessor();
                    try {
                        System.out.println("\n[SHUTDOWN] Saving all accounts to " + stateFile + "...");
                        Account[] toSave = repository.findAll().toArray(new Account[0]);
                        StatePersister.save(toSave, stateFile);
                        System.out.println("[SHUTDOWN] Successfully persisted " + toSave.length + " account(s).");
                    } catch (Exception e) {
                        System.err.println("[SHUTDOWN] Failed to save state: " + e.getMessage());
                    }
                    System.out.println("[SHUTDOWN] MiniBank Session closed cleanly. Goodbye!");
                }
            }
        }
    }
}
