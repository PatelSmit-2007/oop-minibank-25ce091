package project.mini_bank.service;

import project.mini_bank.model.Account;
import project.mini_bank.model.CurrentAccount;
import project.mini_bank.model.FixedDepositAccount;
import project.mini_bank.model.Premium;
import project.mini_bank.model.SavingsAccount;
import project.mini_bank.util.AnnotationValidator;

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
            // Calls default method from InterestBearing interface
            System.out.println("Yearly Interest : ₹" + account.yearlyInterest());

            // Pattern matching with instanceof
            if (account instanceof SavingsAccount savings) {
                System.out.println("Minimum Balance : ₹" + savings.getMinBalance());
            }

            // Marker interface check
            if (account instanceof Premium) {
                System.out.println("Tier            : Premium Account");
            }

            System.out.println();
        }

        System.out.println("=== WITHDRAWAL RULES EVALUATION ===");

        // 1. WithdrawRule using an Anonymous Class
        WithdrawRule limitRuleAnonymous = new WithdrawRule() {
            @Override
            public boolean allow(Account account, long amount) {
                long maxDailyLimit = 3000;
                return amount <= maxDailyLimit && account.canWithdraw(amount);
            }
        };

        // 2. WithdrawRule using a Lambda Expression
        WithdrawRule basicRuleLambda = (account, amount) -> account.canWithdraw(amount);

        long testAmount = 4000;
        System.out.println("Testing withdrawal of ₹" + testAmount + " from Savings Account:");
        System.out.println("Anonymous class rule (Limit ₹3000) allow? "
                + limitRuleAnonymous.allow(accounts[0], testAmount));
        System.out.println("Lambda expression rule (Standard) allow? "
                + basicRuleLambda.allow(accounts[0], testAmount));

        System.out.println("\n=== EXECUTING WITHDRAWALS ===");
        System.out.println("Savings withdraw ₹4000: " + accounts[0].withdraw(4000));
        System.out.println("Current withdraw ₹4000: " + accounts[1].withdraw(4000));
        System.out.println("Fixed Deposit withdraw ₹1000: " + accounts[2].withdraw(1000));

        System.out.println("\n=== GENERATING STATEMENT (STATIC IMPORT) ===");
        // Uses static import buildStatement from StatementFormatter
        System.out.println(buildStatement(accounts[0]));

        System.out.println("\n=== ANNOTATION VALIDATION (REFLECTION) ===");
        // Create an Account with a negative balance and exceeding name length to test reflection validator
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
