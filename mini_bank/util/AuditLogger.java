package project.mini_bank.util;

import java.lang.reflect.Method;
import project.mini_bank.model.annotation.Audit;

public class AuditLogger {

    public static void log(Class<?> targetClass, String methodName, Object... params) {
        try {
            for (Method method : targetClass.getDeclaredMethods()) {
                if (method.getName().equals(methodName) && method.isAnnotationPresent(Audit.class)) {
                    Audit audit = method.getAnnotation(Audit.class);
                    String action = audit.action().isEmpty() ? method.getName() : audit.action();
                    int paramCount = (params != null) ? params.length : 0;
                    System.out.println("[AUDIT TRAIL] Action: " + action
                            + " | Service Method: " + targetClass.getSimpleName() + "." + methodName
                            + " | Parameter count: " + paramCount);
                    return;
                }
            }
        } catch (Exception ignored) {
        }
    }
}
