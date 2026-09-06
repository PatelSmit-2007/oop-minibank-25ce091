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

        // 1. Test input validators
        System.out.println("=== VALIDATOR TESTS ===");

        System.out.println("Valid mobile: "
                + Validator.isValidMobile("9876543210"));
        System.out.println("Invalid mobile: "
                + Validator.isValidMobile("12345"));

        System.out.println("Valid email: "
                + Validator.isValidEmail("smit@example.com"));
        System.out.println("Invalid email: "
                + Validator.isValidEmail("smit@"));

        System.out.println("Valid PAN: "
                + Validator.isValidPan("ABCDE1234F"));
        System.out.println("Invalid PAN: "
                + Validator.isValidPan("ABCDE12345"));

        System.out.println("Valid IFSC: "
                + Validator.isValidIfsc("SBIN0001234"));
        System.out.println("Invalid IFSC: "
                + Validator.isValidIfsc("SBIN1234567"));

        // 2. Parse a transaction command
        System.out.println("\n=== COMMAND PARSER ===");

        Command command = CommandParser.parse("DEPOSIT AC0001 500");

        System.out.println("Type: " + command.type());
        System.out.println("Account Number: " + command.accountNumber());
        System.out.println("Amount: " + command.amount());

        // 3. Generate an account statement
        System.out.println("\n=== ACCOUNT STATEMENT ===");

        Account account = new Account("Smit", 5000);

        System.out.println(
                StatementFormatter.buildStatement(account)
        );
    }
}