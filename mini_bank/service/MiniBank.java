package project.mini_bank.service;

import project.mini_bank.exception.AccountNotFoundException;
import project.mini_bank.exception.BankException;
import project.mini_bank.exception.InsufficientFundsException;
import project.mini_bank.exception.InvalidAmountException;
import project.mini_bank.model.Account;
import project.mini_bank.model.CurrentAccount;
import project.mini_bank.model.FixedDepositAccount;
import project.mini_bank.model.Premium;
import project.mini_bank.model.SavingsAccount;
import project.mini_bank.util.AnnotationValidator;
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
        EXIT
    }

    public static void main(String[] args) {

        BankInfo info = new BankInfo("Mini Bank", "Main Branch");
        System.out.println("=== " + info.name() + " (" + info.branch() + ") ===");

        Account[] accounts = {
                new SavingsAccount("Smit", 5000, 1000),
                new CurrentAccount("Rahul", 2000, 3000),
                new FixedDepositAccount("Amit", 10000)
        };

        System.out.println("\n=== ACCOUNT DETAILS, INTEREST & CAPABILITIES ===");

        for (Account account : accounts) {
            System.out.println(account);
            System.out.println("Interest Rate   : " + account.interestRate() + "%");
            System.out.println("Yearly Interest : ₹" + account.yearlyInterest());

            if (account instanceof SavingsAccount savings) {
                System.out.println("Minimum Balance : ₹" + savings.getMinBalance());
            }

            if (account instanceof Premium) {
                System.out.println("Tier            : Premium Account");
            }

            System.out.println();
        }

        System.out.println("=== WITHDRAWAL RULES EVALUATION ===");

        WithdrawRule limitRuleAnonymous = new WithdrawRule() {
            @Override
            public boolean allow(Account account, long amount) {
                long maxDailyLimit = 3000;
                return amount <= maxDailyLimit && account.canWithdraw(amount);
            }
        };

        WithdrawRule basicRuleLambda = (account, amount) -> account.canWithdraw(amount);

        long testAmount = 4000;
        System.out.println("Testing withdrawal of ₹" + testAmount + " from Savings Account:");
        System.out.println("Anonymous class rule (Limit ₹3000) allow? "
                + limitRuleAnonymous.allow(accounts[0], testAmount));
        System.out.println("Lambda expression rule (Standard) allow? "
                + basicRuleLambda.allow(accounts[0], testAmount));

        System.out.println("\n=== FAULT-TOLERANT OPERATIONS (TRY-CATCH, FINALLY & TRY-WITH-RESOURCES) ===");

        try (TransactionSession session = new TransactionSession("TXN-2026-001")) {

            try {
                System.out.println("\nAttempting ₹2000 withdrawal from Savings...");
                accounts[0].withdraw(2000);
                System.out.println("Success! New Balance: ₹" + accounts[0].getBalance());
            } catch (InsufficientFundsException e) {
                System.err.println("Failed: " + e.getMessage() + " [Shortfall: ₹" + e.getShortfall() + "]");
            } catch (InvalidAmountException e) {
                System.err.println("Invalid Amount: " + e.getMessage());
            } finally {
                System.out.println("Operation 1 completed.");
            }

            try {
                System.out.println("\nAttempting ₹5000 withdrawal from Savings (balance ₹3000, min ₹1000)...");
                accounts[0].withdraw(5000);
            } catch (InsufficientFundsException e) {
                System.out.println("Caught Expected Error: " + e.getMessage());
                System.out.println("Shortfall Amount   : ₹" + e.getShortfall());
            } catch (InvalidAmountException e) {
                System.err.println("Invalid Amount: " + e.getMessage());
            } finally {
                System.out.println("Operation 2 completed.");
            }

            try {
                System.out.println("\nAttempting invalid deposit of ₹-200...");
                accounts[0].deposit(-200);
            } catch (InvalidAmountException e) {
                System.out.println("Caught Expected Error: " + e.getMessage());
            } finally {
                System.out.println("Operation 3 completed.");
            }

            try {
                System.out.println("\nTransferring ₹1000 from Smit to Rahul...");
                accounts[0].transfer(accounts[1], 1000);
                System.out.println("Transfer successful!");
                System.out.println("Smit Balance : ₹" + accounts[0].getBalance());
                System.out.println("Rahul Balance: ₹" + accounts[1].getBalance());
            } catch (BankException e) {
                System.err.println("Transfer Failed: " + e.getMessage());
            } finally {
                System.out.println("Operation 4 completed.");
            }

            try {
                System.out.println("\nTransferring ₹500 to null destination...");
                accounts[0].transfer(null, 500);
            } catch (AccountNotFoundException e) {
                System.out.println("Caught Expected Error: " + e.getMessage());
            } catch (BankException e) {
                System.err.println("Bank Error: " + e.getMessage());
            } finally {
                System.out.println("Operation 5 completed.");
            }

        } catch (Exception e) {
            System.err.println("Session error: " + e.getMessage());
        }

        System.out.println("\n=== BATCH TRANSACTION PROCESSING (THREAD POOL) ===");
        // Requirement 1 & 2: Process batch of transactions with TransactionProcessor (FixedThreadPool(4))
        TransactionProcessor processor = new TransactionProcessor();
        Account poolAccount = new SavingsAccount("Pool User", 5000, 1000);
        System.out.println("Pool Account Initial Balance: ₹" + poolAccount.getBalance());

        for (int i = 1; i <= 8; i++) {
            final int taskId = i;
            if (taskId % 2 == 0) {
                processor.submit(() -> {
                    try {
                        poolAccount.deposit(500);
                        System.out.println("[PoolTask-" + taskId + "] Deposited ₹500. Current: ₹" + poolAccount.getBalance());
                    } catch (InvalidAmountException e) {
                        System.err.println("Deposit error: " + e.getMessage());
                    }
                });
            } else {
                processor.submit(() -> {
                    try {
                        poolAccount.withdraw(200);
                        System.out.println("[PoolTask-" + taskId + "] Withdrew ₹200. Current: ₹" + poolAccount.getBalance());
                    } catch (BankException e) {
                        System.err.println("Withdraw error: " + e.getMessage());
                    }
                });
            }
        }

        System.out.println("Shutting down TransactionProcessor and awaiting termination...");
        processor.stop();
        System.out.println("All batch tasks completed! Final Pool Account Balance: ₹" + poolAccount.getBalance());

        System.out.println("\n=== PRODUCER-CONSUMER TRANSACTION BUFFER (WAIT/NOTIFY) ===");
        // Requirement 3: One thread produces transactions to a shared array buffer, another thread consumes
        TransactionBuffer buffer = new TransactionBuffer(3);

        Thread producerThread = new Thread(() -> {
            String[] transactions = {"TXN-01 (Deposit ₹500)", "TXN-02 (Withdraw ₹200)", "TXN-03 (Transfer ₹100)", "TXN-04 (Deposit ₹1000)", "TXN-05 (Withdraw ₹300)"};
            for (String txn : transactions) {
                try {
                    buffer.produce(txn);
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "TxnProducer");

        Thread consumerThread = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                try {
                    buffer.consume();
                    Thread.sleep(150);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "TxnConsumer");

        producerThread.start();
        consumerThread.start();

        try {
            producerThread.join();
            consumerThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("Producer-Consumer coordination completed successfully.");

        System.out.println("\n=== DEADLOCK AVOIDANCE (CONSISTENT LOCK ORDERING) ===");
        // Requirement 4: Concurrent transfers A->B and B->A with lock ordering by smaller accountNumber first
        Account accountA = new SavingsAccount("Alice", 10000, 1000);
        Account accountB = new SavingsAccount("Bob", 10000, 1000);

        System.out.println("Account A (" + accountA.getAccountNumber() + ") Initial: ₹" + accountA.getBalance());
        System.out.println("Account B (" + accountB.getAccountNumber() + ") Initial: ₹" + accountB.getBalance());

        Thread transferAtoB = new Thread(() -> {
            for (int i = 0; i < 20; i++) {
                try {
                    accountA.transfer(accountB, 50);
                } catch (BankException e) {
                    System.err.println("Transfer A->B error: " + e.getMessage());
                }
            }
            System.out.println("[Thread A->B] Completed all 20 transfers.");
        }, "Transfer-A-to-B");

        Thread transferBtoA = new Thread(() -> {
            for (int i = 0; i < 20; i++) {
                try {
                    accountB.transfer(accountA, 50);
                } catch (BankException e) {
                    System.err.println("Transfer B->A error: " + e.getMessage());
                }
            }
            System.out.println("[Thread B->A] Completed all 20 transfers.");
        }, "Transfer-B-to-A");

        transferAtoB.start();
        transferBtoA.start();

        try {
            transferAtoB.join();
            transferBtoA.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("Deadlock Avoidance Test: Both concurrent transfer threads completed without deadlocks!");
        System.out.println("Account A Final Balance: ₹" + accountA.getBalance());
        System.out.println("Account B Final Balance: ₹" + accountB.getBalance());

        System.out.println("\n=== GENERATING STATEMENT (STATIC IMPORT) ===");
        System.out.println(buildStatement(accounts[0]));

        System.out.println("\n=== ANNOTATION VALIDATION (REFLECTION) ===");
        Account invalidAccount = new SavingsAccount("InvalidAccountHolderNameExceedingLimit", -500, 1000);
        System.out.println("Validating account with negative balance and long name ("
                + invalidAccount.getAccountNumber() + "):");

        String[] errors = AnnotationValidator.validate(invalidAccount);
        if (errors.length > 0) {
            System.out.println("Validation failed with " + errors.length + " error(s):");
            for (String error : errors) {
                System.out.println(" - " + error);
            }
        } else {
            System.out.println("All validations passed.");
        }
    }
}
