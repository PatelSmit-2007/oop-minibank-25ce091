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

        Account[] accounts = {
                new Account("Smit", 5000),
                new Account("Rahul", 3000),
                new Account("Amit")
        };

        accounts[0].deposit(2000);
        accounts[0].withdraw(1000);

        accounts[1].deposit(1500);
        accounts[1].withdraw(500);

        accounts[2].deposit(4000);
        accounts[2].withdraw(1000);

        for (Account account : accounts) {
            System.out.println(
                    account.getAccountNumber() +
                            " | " + account.getOwnerName() +
                            " | Balance: ₹" + account.getBalance()
            );
        }
    }
}