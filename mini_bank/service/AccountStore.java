package project.mini_bank.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import project.mini_bank.model.Account;

public class AccountStore {

    // Requirement 1: HashMap keyed by account number
    private final Map<String, Account> accounts = new HashMap<>();

    // Requirement 2: Transaction history in an ArrayList<String>
    private final ArrayList<String> transactionHistory = new ArrayList<>();

    public void add(Account account) {
        if (account != null) {
            accounts.put(account.getAccountNumber(), account);
            transactionHistory.add("Added account: " + account.getAccountNumber() + " (" + account.getOwnerName() + ")");
        }
    }

    public Account get(String accountNumber) {
        return accounts.get(accountNumber);
    }

    public Collection<Account> getAll() {
        return new ArrayList<>(accounts.values());
    }

    public void recordTransaction(String entry) {
        transactionHistory.add(entry);
    }

    public List<String> getTransactionHistory() {
        return new ArrayList<>(transactionHistory);
    }

    // Requirement 3: Return ArrayList<Account> sorted by balance with a Comparator
    public ArrayList<Account> sortedByBalance() {
        ArrayList<Account> list = new ArrayList<>(accounts.values());
        list.sort(Comparator.comparingLong(Account::getBalance));
        return list;
    }
}
