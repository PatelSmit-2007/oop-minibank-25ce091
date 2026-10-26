# MiniBank — Object-Oriented Banking System in Java

A modular, fault-tolerant, concurrent, and persistent banking application developed in Java across 14 iterative milestones. The project incorporates modern Java features, design patterns, robust error handling, multithreading, reflection, NIO, and collections.

---

## Architecture Overview

MiniBank is architected across modular packages adhering to separation of concerns:

- **`project.mini_bank.model`**: Domain entities and account hierarchy.
  - `Account` (Abstract base class implementing `Transactable`, `InterestBearing`, `Serializable`, `Comparable<Account>`)
  - `SavingsAccount` (Enforces minimum balance, implements `Premium`)
  - `CurrentAccount` (Supports overdraft facilities)
  - `FixedDepositAccount` (Term-locked deposit with higher interest)
  - `Customer` (Customer profile with nested static `Address` and `Cloneable`)
  - `Transactable` (Interface for deposit and withdrawal operations)
  - `InterestBearing` (Interface with default `yearlyInterest()` calculation)
  - `Premium` (Marker interface for tier identification)
- **`project.mini_bank.model.annotation`**: Custom runtime annotations.
  - `@Id` (Entity identifier for generic repository mapping)
  - `@Positive` (Field validation ensuring numerical values are $> 0$)
  - `@MaxLength` (Field validation constraining string length)
  - `@Audit` (Method-level annotation tracking service operations)
- **`project.mini_bank.exception`**: Checked exception hierarchy.
  - `BankException` (Base checked exception)
  - `InsufficientFundsException` (Carries explicit `shortfall` amount)
  - `AccountNotFoundException` (Thrown for invalid account lookups)
  - `InvalidAmountException` (Thrown for non-positive transaction amounts)
- **`project.mini_bank.repository`**: Reusable generic data access layer.
  - `Repository<T>` (Generic repository storing entities in `HashMap<String, T>`, dynamically extracting `@Id` keys via reflection, `@SafeVarargs saveAll()`, and bounded generic `average()`)
- **`project.mini_bank.service`**: Business logic, concurrency engine, and service orchestration.
  - `BankService` (Central service wiring repository, validation, logging, and transactions)
  - `AccountStore` (Collections-based account management and sorting with `Comparator`)
  - `TransactionProcessor` (Thread pool of 4 workers over thread-safe `ConcurrentHashMap`)
  - `TransactionBuffer` (Producer-consumer buffer coordinating threads with `wait()` and `notifyAll()`)
  - `AccountWorker` (Worker task for concurrent operations)
  - `WithdrawRule` (Functional interface for customized withdrawal constraints)
  - `MiniBank` (Main entry point with interactive/automated menu dispatcher)
- **`project.mini_bank.util`**: Helper utilities and file I/O.
  - `AnnotationValidator` (Reflection-based validator for `@Positive` and `@MaxLength`)
  - `StatePersister` (Object serialization/deserialization with `ObjectOutputStream`/`ObjectInputStream`)
  - `TransactionLog` (NIO append-only transaction logger using `Files.write`)
  - `ReportGenerator` (NIO directory scanning with `DirectoryStream`, metadata reading, and aggregation)
  - `TransactionSession` (`AutoCloseable` session for audit and safe closure)
  - `StatementFormatter` (Account statement string builder)
  - `Validator` (Regex validators for Mobile, Email, PAN, and IFSC)
  - `AuditLogger` (Reflective invocation logger reading `@Audit`)
- **`project.mini_bank.test`**: Unit testing suite.
  - `Test` (Custom test annotation)
  - `MiniBankTest` (Automated test suite verifying deposits, overdraft limits, and thread safety)

---

## Key Technical Features

1. **Polymorphic Account Modeling**: Abstract base class with specialized subclasses and interface segregation.
2. **Custom Reflection Engine**: Dynamic runtime extraction of primary keys via `@Id` and field validation with `@Positive` and `@MaxLength`.
3. **Explicit Error Handling**: Fault-tolerant design returning meaningful exception objects rather than boolean error flags.
4. **Concurrency & Thread Safety**:
   - `synchronized` methods ensuring atomic deposits and withdrawals.
   - `ConcurrentHashMap` for high-throughput thread-safe access.
   - Thread pool task execution using `ExecutorService` (`FixedThreadPool(4)`).
   - Consistent lock ordering preventing circular-wait deadlocks during transfers.
   - Inter-thread coordination via `wait()` and `notifyAll()`.
5. **NIO & Serialization Persistence**: Binary state persistence (`.ser`) and log aggregation with file attributes.
6. **Generic Repository Pattern**: Type-safe generic repository usable for any entity with an `@Id` annotation.
7. **Reflective Audit Logging**: Decorates service methods with `@Audit` to track execution and parameters.

---

## Building and Running

### Compile and Package
```powershell
# Compile all source files
javac -cp .. -d out mini_bank/exception/*.java mini_bank/model/annotation/*.java mini_bank/model/*.java mini_bank/repository/*.java mini_bank/util/*.java mini_bank/service/*.java mini_bank/test/*.java

# Build runnable JAR with manifest
jar cvfm MiniBank.jar Manifest.txt -C out .
```

### Run Application
```powershell
java -jar MiniBank.jar
```

---

## Version History & Roadmap

| Version | Date | Key Highlights |
| :--- | :--- | :--- |
| **v1** | 2026-08-16 | Initial baseline console application with menu structure. |
| **v2** | 2026-08-22 | Introduced `Account` and `Customer` entity models. |
| **v3** | 2026-08-30 | Added nested `Address` model and `Cloneable` customer cloning. |
| **v4** | 2026-09-06 | Regex input validators, command parser, and statement formatter. |
| **v5** | 2026-09-13 | Inheritance hierarchy with `SavingsAccount`, `CurrentAccount`, and `FixedDepositAccount`. |
| **v6** | 2026-09-20 | Interfaces (`Transactable`, `InterestBearing`, `Premium`, `WithdrawRule`), packaging into `model`, `util`, `service`, and runnable JAR. |
| **v7** | 2026-09-27 | Custom annotations (`@Id`, `@Positive`, `@MaxLength`) and reflection-based `AnnotationValidator`. |
| **v8** | 2026-10-04 | Milestone 1: Custom exception hierarchy (`BankException`, `InsufficientFundsException`), shortfall tracking, `transfer()`, and `AutoCloseable` `TransactionSession`. |
| **v9** | 2026-10-11 | Thread safety with `synchronized` methods and `AccountWorker` concurrency test. |
| **v10**| 2026-10-18 | `TransactionProcessor` thread pool, producer-consumer `TransactionBuffer`, and deadlock prevention via lock ordering. |
| **v11**| 2026-10-21 | Serialization (`StatePersister`), append-only logging (`TransactionLog`), and NIO report generation (`ReportGenerator`). |
| **v12**| 2026-10-23 | Milestone 2: Collections framework with `AccountStore`, `Comparable`, `Comparator`, and `ConcurrentHashMap`. |
| **v13**| 2026-10-25 | Generic `Repository<T>`, reflective `@Id` extraction, `@SafeVarargs saveAll()`, and bounded generic `average()`. |
| **v14**| 2026-10-26 | Milestone 3 (Capstone): `BankService` orchestration, connected interactive menu, `@Audit` logging, automated test suite, and final JAR. |
