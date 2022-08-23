package io.github.cweijan.mock.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.BeanUtils;
import org.springframework.objenesis.ObjenesisHelper;
import org.springframework.util.StringUtils;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author weijan
 * @since 2019/8/16 17:21
 */
public abstract class ReflectUtils {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Logger logger = LoggerFactory.getLogger(ReflectUtils.class);

    @FunctionalInterface
    public interface ListConvertCallBack<O, T> {
        /**
         * 列表每个对象转换完成的回调函数
         *
         * @param origin 原始对象
         * @param target 转换完成的对象
         */
        void callback(O origin, T target);
    }

    private static final HashMap<Class<?>, Field[]> fieldCache = new HashMap<>();
    private static final HashMap<Class<?>, Map<String, PropertyResolve>> resolveCache = new HashMap<>();
    private static final HashMap<Class<?>, Boolean> insMark = new HashMap<>();
    private static boolean cacheField = false;

    /**
     * 是否对Field进行缓存,默认不缓存,生产环境建议开启
     */
    public static void enableCache(boolean cacheField) {
        ReflectUtils.cacheField = cacheField;
    }

    /**
     * 将列表转换为另一个列表
     *
     * @param objectList  对象列表
     * @param targetClass 需要转换成的class
     * @param callBack    回调函数
     */
    public static <O, T> List<T> convert(List<O> objectList, Class<T> targetClass, ListConvertCallBack<O, T> callBack, String... ignoreProperties) {
        return objectList.stream().map(source -> {
            T target = convert(source, targetClass, ignoreProperties);
            callBack.callback(source, target);
            return target;

        }).collect(Collectors.toList());
    }

    /**
     * 将列表转换为另一个列表
     *
     * @param objectList  对象列表
     * @param targetClass 需要转换成的class
     */
    public static <T> List<T> convert(List<?> objectList, Class<T> targetClass, String... ignoreProperties) {
        return objectList.stream().map(source -> convert(source, targetClass, ignoreProperties)).collect(Collectors.toList());
    }

    /**
     * 将源对象的转换为新的对象<br/>
     * 与BeanUtils.copyProperties的不同点
     * <li>copyProperties是复制属性,需要手动创建对象, 该方法为直接对象转换,且支持转Map为Object</li>
     * <li>copyProperties方法的两个参数容易造成混淆, 该方法参数简单明了</li>
     *
     * @param source           源对象
     * @param targetClass      目标class
     * @param ignoreProperties 需要忽略的属性
     */
    public static <O, T> T convert(O source, Class<T> targetClass, String... ignoreProperties) {
        return convert(source, targetClass, null, ignoreProperties);
    }

    /**
     * 将源对象的转换为新的对象<br/>
     * 与BeanUtils.copyProperties的不同点
     * <li>copyProperties是复制属性,需要手动创建对象, 该方法为直接对象转换,且支持转Map为Object</li>
     * <li>copyProperties方法的两个参数容易造成混淆, 该方法参数简单明了</li>
     *
     * @param source           源对象
     * @param targetClass      目标class
     * @param callBack         转换完成回调接口
     * @param ignoreProperties 需要忽略的属性
     */
    public static <T> T convert(Object source, Class<T> targetClass, Consumer<T> callBack, String... ignoreProperties) {
        if (source == null || targetClass == null) return null;
        if (source.getClass() == targetClass && (targetClass.getPackage().getName().startsWith("java.lang") || targetClass.isPrimitive()))
            return (T) source;
        List<String> ignoreList = (ignoreProperties != null ? Arrays.asList(ignoreProperties) : Collections.emptyList());
        T instance = instantiateClass(targetClass);
        for (Field targetFiled : cacheField ? fieldCache.computeIfAbsent(targetClass, ReflectUtils::getFieldArray) : getFieldArray(targetClass)) {
            if (ignoreList.contains(targetFiled.getName()) || Modifier.isStatic(targetFiled.getModifiers())) {
                continue;
            }
            Object sourceValue;
            if (Map.class.isAssignableFrom(source.getClass())) {
                sourceValue = getValueFromMap((Map) source, targetFiled.getName());
            } else {
                sourceValue = getFieldValue(source, targetFiled.getName());
            }
            if (sourceValue != null) {
                if (Collection.class.isAssignableFrom(targetFiled.getType())) {
                    setFieldValue(instance, targetFiled, convert((List<?>) sourceValue, getGenericType(targetFiled)));
                } else {
                    setFieldValue(instance, targetFiled, sourceValue);
                }
            }
        }
        if (callBack != null) {
            callBack.accept(instance);
        }
        return instance;
    }

    /**
     * 通过反射克隆新对象
     */
    public static <T> T clone(T obj){
        Objects.requireNonNull(obj);
        Class<?> targetClass = obj.getClass();
        Object newObj = instantiateClass(targetClass);
        for (Field targetFiled : cacheField ? fieldCache.computeIfAbsent(targetClass, ReflectUtils::getFieldArray) : getFieldArray(targetClass)) {
            Object sourceValue = getFieldValue(obj, targetFiled);
            setFieldValue(newObj, targetFiled, sourceValue);
        }
        return (T) newObj;
    }

    public static <T> T instantiateClass(Class<T> targetClass) {
        if (insMark.get(targetClass) != null) return ObjenesisHelper.newInstance(targetClass);
        try {
            return BeanUtils.instantiateClass(targetClass);
        } catch (BeanInstantiationException ignored) {
            //如果没有午餐构造方法, 强制初始化
            insMark.put(targetClass, true);
            return ObjenesisHelper.newInstance(targetClass);
        }
    }

    private static Object getValueFromMap(Map sourceMap, String name) {
        Object sourceValue;
        sourceValue = sourceMap.get(name);
        if (sourceValue == null) {
            sourceValue = sourceMap.get(name.replaceAll("([A-Z])", "_$1").toLowerCase());
        }
        return sourceValue;
    }

    /**
     * 获取类的field,包括父类的
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
     * @param fieldName   field名称
     * @param targetValue field进行的赋值
     */
    public static void setFieldValue(Object instance, String fieldName, Object targetValue) {
        if (instance == null) return;
        setFieldValue(instance, getField(instance.getClass(), fieldName), targetValue);
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
     * 获取实例对象的field
     *
     * @param instance  实例对象
     * @param fieldName field名称
     * @return field值
     */
    public static Object getFieldValue(Object instance, String fieldName) {
        if (instance == null) return null;
        return getFieldValue(instance, getField(instance.getClass(), fieldName));
    }

    /**
     * 获取实例对象的field
     *
     * @param instance 实例对象
     * @param field    field对象
     * @return field值
     */
    public static Object getFieldValue(Object instance, Field field) {
        if (instance == null || field == null) return null;
        try {
            PropertyResolve propertyResolve = getPropertyMap(instance.getClass()).get(field.getName());
            if (propertyResolve != null) {
                Method readMethod = propertyResolve.getReadMethod();
                if (readMethod != null) {
                    if (!Modifier.isPublic(readMethod.getDeclaringClass().getModifiers())) {
                        readMethod.setAccessible(true);
                    }
                    return readMethod.invoke(instance);
                }
            }
            field.setAccessible(true);
            return field.get(instance);
        } catch (Exception e) {
            throw new RuntimeException(e);
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

        Field[] fields = cacheField ? fieldCache.computeIfAbsent(clazz, ReflectUtils::getFieldArray) : getFieldArray(clazz);
        for (Field field : fields) {
            field.setAccessible(true);
            if (field.getName().equalsIgnoreCase(fieldName)) return field;
        }

        return null;
    }

    /**
     * 获取Field的泛型
     */
    public static Class<?> getGenericType(Field field) {
        List<Class<?>> genericTypeList = getGenericTypeArray(field);
        return (genericTypeList == null || genericTypeList.size() == 0) ? null : genericTypeList.get(0);
    }

    /**
     * 获取class对象的泛型, example class: extends Base<String>
     */
    public static Class<?> getGenericType(Class<?> clazz) {
        List<Class<?>> genericTypeList = getGenericTypeArray(clazz);
        return (genericTypeList == null || genericTypeList.size() == 0) ? null : genericTypeList.get(0);
    }

    /**
     * 获取Field的泛型列表, example field: List<String> nameList
     */
    public static List<Class<?>> getGenericTypeArray(Field field) {
        if (field == null) return null;
        return getGenericTypeList(field.getGenericType());
    }

    /**
     * 获取class对象的泛型列表
     */
    public static List<Class<?>> getGenericTypeArray(Class<?> clazz) {
        if (clazz == null) return null;
        return getGenericTypeList(clazz.getGenericSuperclass());
    }

    /**
     * 根据type查找泛型,没有泛型则返回空
     * method 返回值: getGenericReturnType
     * parameter 泛型: getParameterizedType
     */
    public static List<Class<?>> getGenericTypeList(Type type) {
        if (type == null) {
            return null;
        }
        if (ParameterizedType.class.isAssignableFrom(type.getClass())) {
            return Stream.of(((ParameterizedType) type).getActualTypeArguments()).map(tempType -> {
                if (Class.class.isAssignableFrom(tempType.getClass())) {
                    return (Class<?>) tempType;
                } else {
                    return (Class<?>) ((ParameterizedType) tempType).getRawType();
                }
            }).collect(Collectors.toList());
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

    /**
     * 从包package中获取所有的Class
     *
     * @param pack
     * @return
     */
    public static Set<Class<?>> getClasses(String pack) {
        Set<Class<?>> classes = new LinkedHashSet<>();
        String packageName = pack;
        String packageDirName = packageName.replace('.', '/');
        Enumeration<URL> dirs;
        try {
            dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
            while (dirs.hasMoreElements()) {
                URL url = dirs.nextElement();
                String protocol = url.getProtocol();
                if ("file".equals(protocol)) {
                    String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                    findAndAddClassesInPackageByFile(packageName, filePath, classes);
                } else if ("jar".equals(protocol)) {
                    JarFile jar;
                    try {
                        jar = ((JarURLConnection) url.openConnection()).getJarFile();
                        Enumeration<JarEntry> entries = jar.entries();
                        while (entries.hasMoreElements()) {
                            JarEntry entry = entries.nextElement();
                            String name = entry.getName();
                            if (name.charAt(0) == '/') {
                                name = name.substring(1);
                            }
                            if (name.startsWith(packageDirName)) {
                                int idx = name.lastIndexOf('/');
                                if (idx != -1) {
                                    packageName = name.substring(0, idx).replace('/', '.');
                                }
                                if (name.endsWith(".class") && !entry.isDirectory()) {
                                    String className = name.substring(packageName.length() + 1, name.length() - 6);
                                    try {
                                        classes.add(Class.forName(packageName + '.' + className));
                                    } catch (ClassNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return classes;
    }

    /**
     * 以文件路径的形式来获取包下的所有Class
     *
     * @param packageName 包名
     * @param packagePath 包的物理文件路径
     * @param classes     集合
     */
    private static void findAndAddClassesInPackageByFile(String packageName, String packagePath, Set<Class<?>> classes) {
        File dir = new File(packagePath);
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        File[] dirfiles = dir.listFiles(file -> (file.isDirectory()) || (file.getName().endsWith(".class")));
        for (File file : dirfiles) {
            if (file.isDirectory()) {
                findAndAddClassesInPackageByFile(packageName + "." + file.getName(), file.getAbsolutePath(), classes);
            } else {
                String className = file.getName().substring(0, file.getName().length() - 6);
                try {
                    classes.add(Thread.currentThread().getContextClassLoader().loadClass(packageName + '.' + className));
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
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
