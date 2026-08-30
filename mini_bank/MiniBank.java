package project.mini_bank;

import java.util.Scanner;

public class MiniBank {

    record BankInfo(String name, String branch) {
    }

    enum MenuOption {
        OPEN_ACCOUNT,
        DEPOSIT,
        WITHDRAW,
        TRANSFER,
        EXIT
    }

    public static void main(String[] args) {

        Account[] accounts = {
                new Account("Smit", 5000),
                new Account("Rahul", 3000),
                new Account("Amit")
        };

        accounts[0].deposit(2000);
        accounts[0].withdraw(1000);

        accounts[1].deposit(1500);
        accounts[1].withdraw(500);

        accounts[2].deposit(4000);
        accounts[2].withdraw(1000);

        // 1. Display accounts using toString()
        System.out.println("=== Account Details ===");

        for (Account account : accounts) {
            System.out.println(account.toString());
        }

        // 2. Compare accounts using equals()
        System.out.println("\n=== Account Equality ===");

        System.out.println(
                "Account 1 equals Account 2: "
                        + accounts[0].equals(accounts[1])
        );

        System.out.println(
                "Account 1 equals itself: "
                        + accounts[0].equals(accounts[0])
        );

        // 3. Check object type using instanceof
        System.out.println("\n=== instanceof Check ===");

        Object obj = accounts[0];

        if (obj instanceof Account) {
            System.out.println("Object is an Account");
        }

        // 4. Create a customer and nested Address object
        System.out.println("\n=== Customer and Address ===");

        Customer customer = new Customer(
                "Smit", "smit@example.com", "9876543210"
        );

        Customer.Address address = new Customer.Address(
                "123 Main Road", "Ahmedabad", "380001"
        );

        customer.setAddress(address);

        System.out.println("Customer ID: " + customer.getCustomerId());
        System.out.println("Customer Name: " + customer.getName());
        System.out.println("Address: " + customer.getAddress().getLine());
        System.out.println("City: " + customer.getAddress().getCity());
        System.out.println("Pincode: " + customer.getAddress().getPincode());

        // 5. Clone the customer
        System.out.println("\n=== Customer Cloning ===");

        Customer clonedCustomer = customer.clone();

        System.out.println("Original Customer: " + customer.getName());
        System.out.println("Cloned Customer: " + clonedCustomer.getName());
        System.out.println(
                "Different objects: " + (customer != clonedCustomer)
        );
    }
}