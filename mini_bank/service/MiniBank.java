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

        // Store different account types using one common parent type
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

            // 1. Successful withdrawal
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

            // 2. Withdrawal with Insufficient Funds (checking shortfall)
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

            // 3. Deposit with Invalid Amount (non-positive)
            try {
                System.out.println("\nAttempting invalid deposit of ₹-200...");
                accounts[0].deposit(-200);
            } catch (InvalidAmountException e) {
                System.out.println("Caught Expected Error: " + e.getMessage());
            } finally {
                System.out.println("Operation 3 completed.");
            }

            // 4. Successful Transfer
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

            // 5. Transfer to non-existent (null) account
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

        System.out.println("\n=== MULTITHREADING & SYNCHRONIZATION DEMO ===");
        // Requirement 1-4: Run 10 threads against one account with AccountWorker and observe synchronization
        Account concurrentAccount = new SavingsAccount("Concurrent User", 1000, 500);
        int numThreads = 10;
        int depositsPerThread = 500;
        long depositAmount = 10;
        long expectedBalance = 1000 + ((long) numThreads * depositsPerThread * depositAmount);

        System.out.println("Shared Account Initial Balance : ₹" + concurrentAccount.getBalance());
        System.out.println("Expected Final Balance         : ₹" + expectedBalance + " ("
                + numThreads + " threads x " + depositsPerThread + " deposits of ₹" + depositAmount + ")");

        Thread[] workers = new Thread[numThreads];
        for (int i = 0; i < numThreads; i++) {
            workers[i] = new Thread(
                    new AccountWorker(concurrentAccount, depositsPerThread, depositAmount),
                    "Worker-Thread-" + (i + 1)
            );
            System.out.println("Main: Created thread [" + workers[i].getName() + "] in NEW state.");
        }

        System.out.println("\nMain: Starting all worker threads...");
        for (Thread worker : workers) {
            worker.start();
        }

        System.out.println("Main: Waiting for threads to finish with join()...");
        for (Thread worker : workers) {
            try {
                worker.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Thread interrupted: " + e.getMessage());
            }
        }

        System.out.println("Main: All worker threads completed.");
        System.out.println("Actual Final Balance           : ₹" + concurrentAccount.getBalance());
        if (concurrentAccount.getBalance() == expectedBalance) {
            System.out.println("Result: SUCCESS - Thread-safe synchronization prevented race conditions!");
        } else {
            System.out.println("Result: RACE CONDITION DETECTED! Balance mismatch.");
        }

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
