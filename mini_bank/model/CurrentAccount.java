package project.mini_bank.model;

public class CurrentAccount extends Account {

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

    public long getOverdraftLimit() {
        return overdraftLimit;
    }
}
