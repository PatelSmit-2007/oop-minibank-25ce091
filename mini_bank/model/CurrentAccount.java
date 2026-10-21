package project.mini_bank.model;

public class CurrentAccount extends Account {

    private static final long serialVersionUID = 1L;

    private long overdraftLimit;

    public CurrentAccount(
            String ownerName,
            long openingBalance,
            long overdraftLimit) {

        super(ownerName, openingBalance);
        this.overdraftLimit = overdraftLimit;
    }

    @Override
    public double interestRate() {
        return 0.0;
    }

    @Override
    public boolean canWithdraw(long amount) {
        return amount > 0
                && getBalance() - amount >= -overdraftLimit;
    }

    @Override
    public long calculateShortfall(long amount) {
        long maxAvailable = getBalance() + overdraftLimit;
        return amount > maxAvailable ? amount - maxAvailable : 0;
    }

    public long getOverdraftLimit() {
        return overdraftLimit;
    }
}
