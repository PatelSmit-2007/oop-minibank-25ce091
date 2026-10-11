package project.mini_bank.service;

import project.mini_bank.exception.InvalidAmountException;
import project.mini_bank.model.Account;

public class AccountWorker implements Runnable {

    private final Account account;
    private final int times;
    private final long amount;

    public AccountWorker(Account account, int times, long amount) {
        this.account = account;
        this.times = times;
        this.amount = amount;
    }

    @Override
    public void run() {
        String threadName = Thread.currentThread().getName();
        System.out.println("[" + threadName + "] Thread lifecycle: RUNNING (Executing "
                + times + " deposits of ₹" + amount + ")");

        for (int i = 0; i < times; i++) {
            try {
                account.deposit(amount);
            } catch (InvalidAmountException e) {
                System.err.println("[" + threadName + "] Deposit failed: " + e.getMessage());
            }
        }

        System.out.println("[" + threadName + "] Thread lifecycle: TERMINATING (Work completed)");
    }
}
