package project.mini_bank.model;

public record Command(
        TransactionType type,
        String accountNumber,
        long amount
) {
}
