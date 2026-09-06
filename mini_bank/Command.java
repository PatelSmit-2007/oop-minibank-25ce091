package project.mini_bank;

public record Command(
        TransactionType type,
        String accountNumber,
        long amount
) {
}