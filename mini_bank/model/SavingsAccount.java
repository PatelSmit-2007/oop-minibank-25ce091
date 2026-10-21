package project.mini_bank.model;

public class SavingsAccount extends Account implements Premium {

    private static final long serialVersionUID = 1L;

    private long minBalance;

    public SavingsAccount(
            String ownerName,
            long openingBalance,
            long minBalance) {

        super(ownerName, openingBalance);
        this.minBalance = minBalance;
    }

    @Override
    public double interestRate() {
        return 4.0;
    }

    @Override
    public boolean canWithdraw(long amount) {
        return amount > 0
                && getBalance() - amount >= minBalance;
    }

    @Override
    public long calculateShortfall(long amount) {
        long required = amount + minBalance;
        return required > getBalance() ? required - getBalance() : 0;
    }

    public long getMinBalance() {
        return minBalance;
    }
}
