package project.mini_bank.model;

import project.mini_bank.exception.InsufficientFundsException;
import project.mini_bank.exception.InvalidAmountException;

public interface Transactable {
    void deposit(long amount) throws InvalidAmountException;
    boolean withdraw(long amount) throws InsufficientFundsException, InvalidAmountException;
}
