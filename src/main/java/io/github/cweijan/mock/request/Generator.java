package io.github.cweijan.mock.request;

import io.github.cweijan.mock.feign.FeignBuilder;
import io.github.cweijan.mock.request.string.ChineseStringGenerator;
import io.github.cweijan.mock.request.string.StringGenerator;
import io.github.cweijan.mock.util.ReflectUtils;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.BeanUtils;
import org.springframework.lang.Nullable;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @author cweijan
 * @since 2020/05/22 18:10
 */
public class Generator {

    private static final Random random = new Random();
    private static final StringGenerator stringGenerator = new ChineseStringGenerator();

    /**
     * 创建虚拟对象并对field随机赋值
     *
     * @param paramClass 目标类型
     * @return 目标实例
     * @throws BeanInstantiationException 当不存在无参构造方法时
     */
    @SuppressWarnings("unchecked")
    public static <T> T request(Class<T> paramClass) throws BeanInstantiationException {

        if (FeignBuilder.isSimple(paramClass)) {
            return (T) auto(paramClass);
        }

        Object instance = BeanUtils.instantiateClass(paramClass);

        Field[] fields = instance.getClass().getDeclaredFields();
        for (Field field : fields) {
            ReflectUtils.setFieldValue(instance, field, auto(field));

        }
        return (T) instance;
    }

    /**
     * 创建两个常见中文字符组成的字符串
     *
     * @return 随机中文字符串
     */
    public static String cword() {
        return stringGenerator.genrate(2);
    }

    /**
     * 随机返回区间内的一个值
     *
     * @param start 开始区间
     * @param end   结束区间
     * @return 随机值
     */
    public static int range(int start, int end) {
        return random.nextInt(end) + start;
    }

    public static <T> Set<T> set(Class<T> targetType) {
        return set(targetType, Generator.range(1, 5));
    }

    public static <T> Set<T> set(Class<T> targetType, int length) {
        Stream<T> objStream = IntStream.range(0, length).mapToObj(operand -> request(targetType));
        return objStream.collect(Collectors.toSet());
    }

    /**
     * 根据类型随机生成list
     *
     * @param targetType 目标类型
     * @param <T>
     * @return
     */
    public static <T> List<T> list(Class<T> targetType) {
        return list(targetType, Generator.range(1, 5));
    }

    /**
     * 根据类型随机生成list
     *
     * @param targetType 目标类型
     * @param length     list最大长度
     * @param <T>
     * @return
     */
    public static <T> List<T> list(Class<T> targetType, int length) {
        Stream<T> objStream = IntStream.range(0, length).mapToObj(operand -> request(targetType));
        return objStream.collect(Collectors.toList());
    }

    /**
     * 随机返回参数列表内的任意一个参数
     * @param values 参数列表
     * @return
     */
    public static Object peak(Object... values) {
        int length = values.length;
        if (length == 0) return null;

        int range = range(0, length - 1);
        return values[range];
    }

    /**
     * 根据类型自动创建一个随机值
     *
     * @param targetType 目标类型
     * @return 随机值
     */
    @Nullable
    public static Object auto(Class<?> targetType) {

        if (targetType == String.class) {
            return cword();
        }
        if (targetType.isEnum()) {
            Object[] enumConstants = targetType.getEnumConstants();
            return enumConstants[range(0, enumConstants.length)];
        }
        if (targetType == Boolean.class || targetType == boolean.class) {
            return random.nextBoolean();
        }
        if (targetType == Float.class || targetType == float.class) {
            return random.nextFloat();
        }
        if (targetType == Double.class || targetType == double.class) {
            return random.nextDouble();
        }
        if (targetType == Integer.class || targetType == int.class) {
            return range(0, 1024);
        }
        if (targetType == Long.class || targetType == long.class) {
            return Math.abs(random.nextLong());
        }
        if (targetType == Byte.class || targetType == byte.class) {
            return new Integer(random.nextInt(256)).byteValue();
        }

        if (Date.class.isAssignableFrom(targetType)) {
            return new Date();
        }
        if (targetType == LocalDateTime.class) {
            return LocalDateTime.now();
        }
        if (targetType == LocalDate.class) {
            return LocalDate.now();
        }
        if (targetType == LocalTime.class) {
            return LocalTime.now();
        }

        return null;
    }

    private static Object auto(Field field) {
        Class<?> targetType = field.getType();
        Class<?> genericType = ReflectUtils.getGenericType(field);
        if (Collection.class.isAssignableFrom(targetType)) {
            if (List.class.isAssignableFrom(targetType)) {
                return list(genericType);
            }
            if (Set.class.isAssignableFrom(targetType)) {
                return set(genericType);
            }
        }
        return auto(targetType);
    }
}
