package project.mini_bank.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import project.mini_bank.model.annotation.MaxLength;
import project.mini_bank.model.annotation.Positive;

public class AnnotationValidator {

    public static String[] validate(Object obj) {
        if (obj == null) {
            return new String[0];
        }

        List<String> errors = new ArrayList<>();
        Class<?> clazz = obj.getClass();

        while (clazz != null && clazz != Object.class) {
            Field[] fields = clazz.getDeclaredFields();

            for (Field field : fields) {
                try {
                    // Check @Positive rule
                    if (field.isAnnotationPresent(Positive.class)) {
                        field.setAccessible(true);
                        Object value = field.get(obj);

                        if (value instanceof Number num) {
                            if (num.doubleValue() <= 0) {
                                Positive positive = field.getAnnotation(Positive.class);
                                errors.add(field.getName() + ": " + positive.message()
                                        + " (actual: " + value + ")");
                            }
                        }
                    }

                    // Check @MaxLength rule
                    if (field.isAnnotationPresent(MaxLength.class)) {
                        field.setAccessible(true);
                        Object value = field.get(obj);

                        if (value instanceof String str) {
                            MaxLength maxLength = field.getAnnotation(MaxLength.class);
                            if (str.length() > maxLength.value()) {
                                errors.add(field.getName() + ": " + maxLength.message()
                                        + " (max: " + maxLength.value()
                                        + ", actual: " + str.length() + ")");
                            }
                        }
                    }

                } catch (IllegalAccessException e) {
                    errors.add("Unable to access field " + field.getName() + ": " + e.getMessage());
                }
            }

            clazz = clazz.getSuperclass();
        }

        return errors.toArray(new String[0]);
    }
}
