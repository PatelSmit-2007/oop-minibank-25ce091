package project.mini_bank.service;

import project.mini_bank.model.Account;

@FunctionalInterface
public interface WithdrawRule {
    boolean allow(Account account, long amount);
}
