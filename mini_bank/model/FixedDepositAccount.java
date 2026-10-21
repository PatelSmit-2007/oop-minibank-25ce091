package project.mini_bank.model;

public class FixedDepositAccount extends Account {

    private static final long serialVersionUID = 1L;

    public FixedDepositAccount(
            String ownerName,
            long openingBalance) {

        super(ownerName, openingBalance);
    }

    @Override
    public double interestRate() {
        return 7.0;
    }

    @Override
    public boolean canWithdraw(long amount) {
        return false;
    }

    @Override
    public long calculateShortfall(long amount) {
        return amount;
    }
}
