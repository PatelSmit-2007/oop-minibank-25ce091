package project.mini_bank;

import java.util.Locale;

public class CommandParser {

    public static Command parse(String line) {

        if (line == null || line.isBlank()) {
            throw new IllegalArgumentException(
                    "Command cannot be empty."
            );
        }

        String[] parts = line.trim().split("\\s+");

        if (parts.length != 3) {
            throw new IllegalArgumentException(
                    "Expected: TYPE ACCOUNT_NUMBER AMOUNT"
            );
        }

        try {
            TransactionType type = TransactionType.valueOf(
                    parts[0].toUpperCase(Locale.ROOT)
            );

            String accountNumber = parts[1];
            long amount = Long.parseLong(parts[2]);

            if (amount <= 0) {
                throw new IllegalArgumentException(
                        "Amount must be greater than zero."
                );
            }

            return new Command(type, accountNumber, amount);

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid command: " + e.getMessage(), e
            );
        }
    }
}