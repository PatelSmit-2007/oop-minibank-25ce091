package project.mini_bank;

import java.util.Scanner;

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

        // Store different account types using one common parent type
        Account[] accounts = {
                new SavingsAccount("Smit", 5000, 1000),
                new CurrentAccount("Rahul", 2000, 3000),
                new FixedDepositAccount("Amit", 10000)
        };

        System.out.println("=== ACCOUNT TYPES AND INTEREST RATES ===");

        for (Account account : accounts) {

            System.out.println(account);
            System.out.println(
                    "Interest Rate: " + account.interestRate() + "%"
            );

            // Pattern matching with instanceof
            if (account instanceof SavingsAccount savings) {
                System.out.println(
                        "Minimum Balance: ₹" + savings.getMinBalance()
                );
            }

            System.out.println();
        }

        System.out.println("=== WITHDRAWAL TESTS ===");

        System.out.println(
                "Savings withdraw ₹4000: "
                        + accounts[0].withdraw(4000)
        );

        System.out.println(
                "Current withdraw ₹4000: "
                        + accounts[1].withdraw(4000)
        );

        System.out.println(
                "Fixed Deposit withdraw ₹1000: "
                        + accounts[2].withdraw(1000)
        );

        System.out.println("\n=== UPDATED BALANCES ===");

        for (Account account : accounts) {
            System.out.println(account);
        }
    }
}