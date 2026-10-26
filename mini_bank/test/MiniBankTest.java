package project.mini_bank.test;

import java.lang.reflect.Method;
import project.mini_bank.exception.BankException;
import project.mini_bank.exception.InsufficientFundsException;
import project.mini_bank.exception.InvalidAmountException;
import project.mini_bank.model.Account;
import project.mini_bank.model.SavingsAccount;
import project.mini_bank.service.AccountWorker;

public class MiniBankTest {

    @Test
    public void testDepositIncreasesBalance() throws InvalidAmountException {
        Account account = new SavingsAccount("TestUser1", 1000, 500);
        account.deposit(500);
        if (account.getBalance() != 1500) {
            throw new AssertionError("Expected balance 1500 but found " + account.getBalance());
        }
    }

    @Test
    public void testWithdrawalOverLimitThrowsException() throws BankException {
        Account account = new SavingsAccount("TestUser2", 1000, 500);
        boolean caught = false;
        try {
            account.withdraw(800); // 1000 - 800 = 200, below minBalance 500
        } catch (InsufficientFundsException e) {
            caught = true;
            if (e.getShortfall() != 300) {
                throw new AssertionError("Expected shortfall 300 but got " + e.getShortfall());
            }
        }
        if (!caught) {
            throw new AssertionError("Expected InsufficientFundsException was not thrown!");
        }
    }

    @Test
    public void testConcurrentDepositsKeepBalanceCorrect() throws InterruptedException {
        Account account = new SavingsAccount("ConcurrentUser", 0, 0);
        int threadCount = 10;
        int depositsPerThread = 500;
        long amount = 10;
        long expectedBalance = (long) threadCount * depositsPerThread * amount;

        Thread[] threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(new AccountWorker(account, depositsPerThread, amount));
            threads[i].start();
        }

        for (Thread t : threads) {
            t.join();
        }

        if (account.getBalance() != expectedBalance) {
            throw new AssertionError("Expected final balance " + expectedBalance + " but got " + account.getBalance());
        }
    }

    public static void runAllTests() {
        System.out.println("Running MiniBank automated JUnit test suite using @Test:");
        int passed = 0;
        int failed = 0;
        MiniBankTest instance = new MiniBankTest();

        for (Method method : MiniBankTest.class.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Test.class)) {
                try {
                    method.invoke(instance);
                    System.out.println(" [PASS] " + method.getName());
                    passed++;
                } catch (Exception e) {
                    System.err.println(" [FAIL] " + method.getName() + " -> " + e.getCause().getMessage());
                    failed++;
                }
            }
        }

        System.out.println("Test Results: " + passed + " passed, " + failed + " failed.");
    }

    public static void main(String[] args) {
        runAllTests();
    }
}
