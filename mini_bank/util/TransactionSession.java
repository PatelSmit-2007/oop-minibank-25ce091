package project.mini_bank.util;

public class TransactionSession implements AutoCloseable {

    private final String sessionId;
    private boolean active;

    public TransactionSession(String sessionId) {
        this.sessionId = sessionId;
        this.active = true;
        System.out.println("[AuditLog] Session " + sessionId + " opened.");
    }

    public String getSessionId() {
        return sessionId;
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public void close() {
        this.active = false;
        System.out.println("[AuditLog] Session " + sessionId + " safely closed.");
    }
}
