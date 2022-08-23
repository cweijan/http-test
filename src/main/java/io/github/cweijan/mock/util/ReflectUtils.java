package io.github.cweijan.mock.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author weijan
 * @since 2019/8/16 17:21
 */
public abstract class ReflectUtils {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Logger logger = LoggerFactory.getLogger(ReflectUtils.class);

    private static final HashMap<Class<?>, Field[]> fieldCache = new HashMap<>();
    private static final HashMap<Class<?>, Map<String, PropertyResolve>> resolveCache = new HashMap<>();

    /**
     * 获取类的field,包括父类的
     * @param clazz 要提取field的类
     * @return field列表
     */
    public static Field[] getFieldArray(Class<?> clazz) {
        if (clazz == null) return new Field[]{};
        Field[] fields = clazz.getDeclaredFields();
        clazz = clazz.getSuperclass();
        while (clazz != null && clazz != Object.class) {
            Field[] tempFields = clazz.getDeclaredFields();
            Field[] newFields = new Field[fields.length + tempFields.length];
            System.arraycopy(fields, 0, newFields, 0, fields.length);
            System.arraycopy(tempFields, 0, newFields, fields.length, tempFields.length);
            fields = newFields;
            clazz = clazz.getSuperclass();
        }
        return fields;
    }


    /**
     * 为对象的field进行赋值
     *
     * @param instance    实例对象
     * @param field       field对象
     * @param targetValue field进行的赋值
     */
    public static void setFieldValue(Object instance, Field field, Object targetValue) {
        if (instance == null || field == null || targetValue == null) return;

        try {
            if (!field.getType().isAssignableFrom(targetValue.getClass())) {
                targetValue = tryAdapaterValue(targetValue, field.getType());
            }

            PropertyResolve propertyResolve = getPropertyMap(instance.getClass()).get(field.getName());
            if (propertyResolve != null) {
                Method writeMethod = getPropertyMap(instance.getClass()).get(field.getName()).getWriteMethod();
                if (writeMethod != null) {
                    if (!Modifier.isPublic(writeMethod.getDeclaringClass().getModifiers())) {
                        writeMethod.setAccessible(true);
                    }
                    writeMethod.invoke(instance, targetValue);
                    return;
                }
            }

            field.setAccessible(true);
            field.set(instance, targetValue);
        } catch (Exception e) {
            System.out.println("set " + field.getName() + " field value fail : " + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * 根据fieldName获取class对象的field
     *
     * @param clazz     class对象
     * @param fieldName 要获取的fieldName
     * @return 返回已经设置为accessible的field
     */
    public static Field getField(Class<?> clazz, String fieldName) {
        if (clazz == null || StringUtils.isEmpty(fieldName)) return null;

        boolean cacheField = false;
        Field[] fields = cacheField ? fieldCache.computeIfAbsent(clazz, ReflectUtils::getFieldArray) : getFieldArray(clazz);
        for (Field field : fields) {
            field.setAccessible(true);
            if (field.getName().equalsIgnoreCase(fieldName)) return field;
        }

        return null;
    }

    public static Object tryAdapaterValue(Object origin, Class<?> targetType) throws ParseException {
        if (origin == null || targetType == null) return null;

        if (targetType == String.class) {
            return origin.toString();
        }

        if (targetType == Boolean.class || targetType == boolean.class) {
            if ("true".equalsIgnoreCase(origin.toString()) || origin.equals("1")) {
                return Boolean.TRUE;
            }
            return Boolean.FALSE;
        }

        if (targetType == Integer.class || targetType == int.class) {
            return Integer.parseInt(origin.toString());
        }

        if (targetType == Double.class || targetType == double.class) {
            return Double.parseDouble(origin.toString());
        }

        Class<?> originClass = origin.getClass();
        if (targetType == Long.class || targetType == long.class) {
            if (Date.class.isAssignableFrom(originClass)) {
                return ((Date) origin).getTime();
            }
            if (originClass == LocalDateTime.class) {
                return ((LocalDateTime) origin).toInstant(OffsetDateTime.now().getOffset()).toEpochMilli();
            }
            if (originClass == LocalDate.class) {
                return ((LocalDate) origin).atStartOfDay(ZoneOffset.systemDefault()).toInstant().toEpochMilli();
            }
            return Long.parseLong(origin.toString());
        }

        if (targetType.isEnum()) {
            Class<? extends Enum<?>> enumTargetType = (Class<? extends Enum<?>>) targetType;
            if (Number.class.isAssignableFrom(originClass) || originClass.isPrimitive()) {
                int i = Integer.parseInt(origin.toString());
                return convertEnum(enumTargetType, i);
            }
            if (originClass.isEnum()) {
                int i = ((Enum<?>) origin).ordinal();
                return convertEnum(enumTargetType, i);
            }
        }

        if (targetType == Date.class) {
            if (LocalDateTime.class == originClass) {
                return new Date(((LocalDateTime) origin).toInstant(OffsetDateTime.now().getOffset()).toEpochMilli());
            }
            if (Number.class.isAssignableFrom(originClass) || originClass.isPrimitive()) {
                return new Date((Long) origin);
            }
            if (LocalDate.class == originClass) {
                return new Date(((LocalDate) origin).atStartOfDay(ZoneOffset.systemDefault()).toInstant().toEpochMilli());
            }
            if (CharSequence.class.isAssignableFrom(originClass)) {
                return DATE_FORMAT.parse((String) origin);
            }
        }

        if (targetType == LocalDateTime.class) {
            if (Date.class.isAssignableFrom(originClass)) {
                return LocalDateTime.ofInstant(((Date) origin).toInstant(), ZoneId.systemDefault());
            }
            if (Number.class.isAssignableFrom(originClass) || originClass.isPrimitive()) {
                return LocalDateTime.ofInstant(Instant.ofEpochMilli((Long) origin), ZoneId.systemDefault());
            }
            if (CharSequence.class.isAssignableFrom(originClass)) {
                return LocalDateTime.parse((CharSequence) origin, DATE_TIME_FORMATTER);
            }
        }

        if (targetType == LocalDate.class) {
            if (Date.class.isAssignableFrom(originClass)) {
                return LocalDateTime.ofInstant(Instant.ofEpochMilli(((Date) origin).getTime()), ZoneId.systemDefault()).toLocalDate();
            }
            if (Number.class.isAssignableFrom(originClass) || originClass.isPrimitive()) {
                return LocalDateTime.ofInstant(Instant.ofEpochMilli((Long) origin), ZoneId.systemDefault()).toLocalDate();
            }
            if (CharSequence.class.isAssignableFrom(originClass)) {
                return LocalDate.parse((CharSequence) origin);
            }
        }

        if (targetType == LocalTime.class) {
            if (Date.class.isAssignableFrom(originClass)) {
                return LocalDateTime.ofInstant(Instant.ofEpochMilli(((Date) origin).getTime()), ZoneId.systemDefault()).toLocalTime();
            }
            if (Number.class.isAssignableFrom(originClass) || originClass.isPrimitive()) {
                return LocalDateTime.ofInstant(Instant.ofEpochMilli((Long) origin), ZoneId.systemDefault()).toLocalTime();
            }
            if (CharSequence.class.isAssignableFrom(originClass)) {
                return LocalTime.parse((CharSequence) origin);
            }
        }

        if (targetType == BigDecimal.class) {
            return new BigDecimal(origin.toString());
        }

        if (targetType == BigInteger.class) {
            return new BigInteger(origin.toString());
        }

        if (targetType == Float.class || targetType == float.class) {
            return Float.parseFloat(origin.toString());
        }

        if (targetType == Short.class || targetType == short.class) {
            return Short.parseShort(origin.toString());
        }

        return JSON.convert(origin, targetType);
    }

    private static <T extends Enum<?>> T convertEnum(Class<T> enumType, Integer ordinal) {
        Enum<?>[] enumConstants = enumType.getEnumConstants();
        if (enumConstants.length > ordinal) {
            return (T) enumConstants[ordinal];
        } else {
            logger.error(enumType.getName() + " enum index out bound for " + ordinal);
        }
        return null;
    }
    private static Map<String, PropertyResolve> getPropertyMap(Class<?> clazz) {
        return resolveCache.computeIfAbsent(clazz, c -> {
            try {
                return Arrays.stream(Introspector.getBeanInfo(clazz).getPropertyDescriptors()).map(PropertyResolve::new)
                        .collect(Collectors.toMap(PropertyResolve::getName, Function.identity()));
            } catch (IntrospectionException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static class PropertyResolve {
        private final String name;
        private Method readMethod;
        private Method writeMethod;
        private final PropertyDescriptor propertyDescriptor;

        public Method getReadMethod() {
            if (readMethod == null) {
                readMethod = propertyDescriptor.getReadMethod();
            }
            return readMethod;
        }

        public Method getWriteMethod() {
            if (writeMethod == null) {
                writeMethod = propertyDescriptor.getWriteMethod();
            }
            return writeMethod;
        }

        public PropertyResolve(PropertyDescriptor propertyDescriptor) {
            this.propertyDescriptor = propertyDescriptor;
            this.name = propertyDescriptor.getName();
        }

        public String getName() {
            return name;
        }
    }

}
