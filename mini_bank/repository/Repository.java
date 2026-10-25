package project.mini_bank.repository;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import project.mini_bank.model.annotation.Id;

public class Repository<T> {

    // Requirement 1: Generic storage using HashMap<String, T>
    private final Map<String, T> storage = new HashMap<>();

    // Requirement 1 & 2: Save entity and extract key from field annotated with @Id via reflection
    public T save(T entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Cannot save null entity.");
        }

        String id = extractId(entity);
        if (id == null) {
            throw new IllegalArgumentException(
                    "Entity of type " + entity.getClass().getSimpleName()
                            + " has no valid @Id field value."
            );
        }

        storage.put(id, entity);
        return entity;
    }

    public T findById(String id) {
        return storage.get(id);
    }

    public ArrayList<T> findAll() {
        return new ArrayList<>(storage.values());
    }

    public boolean delete(String id) {
        return storage.remove(id) != null;
    }

    // Requirement 3: @SafeVarargs saveAll to save multiple items
    @SafeVarargs
    public final void saveAll(T... items) {
        if (items != null) {
            for (T item : items) {
                save(item);
            }
        }
    }

    // Requirement 4: Generic method with a bounded type parameter <N extends Number>
    public static <N extends Number> double average(N[] values) {
        if (values == null || values.length == 0) {
            return 0.0;
        }
        double sum = 0.0;
        for (N val : values) {
            if (val != null) {
                sum += val.doubleValue();
            }
        }
        return sum / values.length;
    }

    // Helper method to reflectively extract @Id field value traversing inheritance hierarchy
    private String extractId(T entity) {
        Class<?> clazz = entity.getClass();
        while (clazz != null && clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Id.class)) {
                    field.setAccessible(true);
                    try {
                        Object val = field.get(entity);
                        return val != null ? String.valueOf(val) : null;
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Failed to access @Id field: " + field.getName(), e);
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }
}
