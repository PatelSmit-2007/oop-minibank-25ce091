package project.mini_bank.model;

import project.mini_bank.exception.AccountNotFoundException;
import project.mini_bank.exception.BankException;
import project.mini_bank.exception.InsufficientFundsException;
import project.mini_bank.exception.InvalidAmountException;
import project.mini_bank.model.annotation.Id;
import project.mini_bank.model.annotation.MaxLength;
import project.mini_bank.model.annotation.Positive;

public abstract class Account implements Transactable, InterestBearing {

    @Id
    private final String accountNumber;

    @MaxLength(30)
    private String ownerName;

    @Positive
    private long balance;

    private boolean active;

    private static long accountCounter = 0;

    private static String generateAccountNumber() {
        accountCounter++;
        return String.format("AC%04d", accountCounter);
    }

    public Account(String ownerName, long openingBalance) {
        this.accountNumber = generateAccountNumber();
        this.ownerName = ownerName;
        this.balance = openingBalance;
        this.active = true;
    }

    public Account(String ownerName) {
        this(ownerName, 0);
    }

    @Override
    public void deposit(long amount) throws InvalidAmountException {
        if (amount <= 0) {
            throw new InvalidAmountException("Deposit amount must be > 0. Provided: ₹" + amount);
        }
        balance += amount;
    }

    @Override
    public boolean withdraw(long amount) throws InsufficientFundsException, InvalidAmountException {
        if (amount <= 0) {
            throw new InvalidAmountException("Withdrawal amount must be > 0. Provided: ₹" + amount);
        }
        if (!canWithdraw(amount)) {
            long shortfall = calculateShortfall(amount);
            throw new InsufficientFundsException(
                    "Insufficient funds for withdrawal of ₹" + amount + ". Shortfall: ₹" + shortfall,
                    shortfall
            );
        }
        balance -= amount;
        return true;
    }

    public long calculateShortfall(long amount) {
        return amount > balance ? amount - balance : 0;
    }

    public void transfer(Account to, long amount) throws BankException {
        if (to == null) {
            throw new AccountNotFoundException("Transfer failed: destination account not found.");
        }
        if (to.equals(this)) {
            throw new BankException("Transfer failed: cannot transfer to the same account.");
        }

        boolean debited = false;
        try {
            this.withdraw(amount);
            debited = true;
            to.deposit(amount);
        } catch (BankException e) {
            if (debited) {
                try {
                    this.deposit(amount); // Rollback debit
                } catch (InvalidAmountException rollbackEx) {
                    // Suppressed rollback exception
                }
            }
            throw e; // re-throw BankException
        } finally {
            // Transaction cleanup / completion hook
        }
    }

    // Abstract methods: subclasses must implement these
    @Override
    public abstract double interestRate();

    public abstract boolean canWithdraw(long amount);

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getOwnerName() {
        return ownerName;
    }

    @Override
    public long getBalance() {
        return balance;
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public String toString() {
        return "Account Number: " + accountNumber
                + ", Owner: " + ownerName
                + ", Balance: ₹" + balance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Account)) {
            return false;
        }

        Account other = (Account) o;
        return accountNumber.equals(other.accountNumber);
    }

    @Override
    public int hashCode() {
        return accountNumber.hashCode();
    }
}
