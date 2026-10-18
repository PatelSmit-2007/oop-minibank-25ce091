package project.mini_bank.service;

public class TransactionBuffer {

    private final String[] buffer;
    private final int capacity;
    private int count;
    private int in;
    private int out;

    public TransactionBuffer(int capacity) {
        this.capacity = capacity;
        this.buffer = new String[capacity];
        this.count = 0;
        this.in = 0;
        this.out = 0;
    }

    public synchronized void produce(String transaction) throws InterruptedException {
        while (count == capacity) {
            System.out.println("Buffer FULL. Producer is waiting...");
            wait();
        }
        buffer[in] = transaction;
        in = (in + 1) % capacity;
        count++;
        System.out.println("[Producer] Produced: " + transaction + " (Buffer items: " + count + "/" + capacity + ")");
        notifyAll();
    }

    public synchronized String consume() throws InterruptedException {
        while (count == 0) {
            System.out.println("Buffer EMPTY. Consumer is waiting...");
            wait();
        }
        String transaction = buffer[out];
        out = (out + 1) % capacity;
        count--;
        System.out.println("[Consumer] Processed: " + transaction + " (Buffer items: " + count + "/" + capacity + ")");
        notifyAll();
        return transaction;
    }
}
