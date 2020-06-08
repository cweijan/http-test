package io.github.cweijan.mock.util;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.DateSerializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import io.github.cweijan.mock.feign.jackson.deserializer.LocalDateExtDeserializer;
import io.github.cweijan.mock.feign.jackson.deserializer.LocalDateTimeExtDeserializer;
import io.github.cweijan.mock.feign.jackson.serializer.LocalDateExtSerializer;
import io.github.cweijan.mock.feign.jackson.serializer.LocalDateTimeExtSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * @author cweijan
 */
public abstract class JSON {
    private static ObjectMapper mapper;
    private static ObjectMapper withEmptyMapper;
    private static SimpleModule dateModule;
    private static final Logger logger = LoggerFactory.getLogger(JSON.class);

    public static void init(String pattern) {
        //datetime parse
        dateModule = new SimpleModule();
        //配置序列化
        dateModule.addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern("HH:mm:ss")));
        dateModule.addSerializer(LocalDate.class, new LocalDateExtSerializer(false));
        dateModule.addSerializer(LocalDateTime.class, new LocalDateTimeExtSerializer(false, pattern));
        dateModule.addSerializer(Date.class, new DateSerializer(false, new SimpleDateFormat(pattern)));
        //配置反序列化
        dateModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ofPattern("HH:mm:ss")));
        dateModule.addDeserializer(LocalDate.class, new LocalDateExtDeserializer());
        dateModule.addDeserializer(LocalDateTime.class, new LocalDateTimeExtDeserializer(pattern));
        //without empty
        mapper = new ObjectMapper();
        mapper.setSerializationInclusion(Include.NON_EMPTY);
        buldCommonMapper(mapper);
        //within empty
        withEmptyMapper = new ObjectMapper();
        withEmptyMapper.setSerializationInclusion(Include.ALWAYS);
        buldCommonMapper(withEmptyMapper);
    }

    /**
     * 设置mappepr的通用属性
     */
    private static void buldCommonMapper(ObjectMapper mapper) {
        mapper.registerModule(dateModule);
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        mapper.configure(Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        mapper.configure(Feature.ALLOW_SINGLE_QUOTES, true);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * 将对象转换成json
     *
     * @param originalObject 要转换的对象
     * @return json字符串
     */
    public static String toJSON(Object originalObject) {

        if (originalObject == null) return null;
        if (originalObject instanceof String) return String.valueOf(originalObject);

        String json = null;
        try {
            json = mapper.writeValueAsString(originalObject);
        } catch (Exception e) {
            logger.error("toJson error:", e);
        }

        return json;
    }

    /**
     * 将对象转换成json字节流数组
     *
     * @param originalObject 要转换的对象
     * @return 字节流数组
     */
    public static byte[] toJsonByte(Object originalObject) {

        if (originalObject == null) return null;

        byte[] json = null;
        try {
            json = mapper.writeValueAsBytes(originalObject);
        } catch (Exception e) {
            logger.error("toJson error:", e);
        }

        return json;
    }


    /**
     * 将对象转换成json,并包含空属性
     *
     * @param originalObject 要转换的对象
     * @return json字符串
     */
    public static String toJsonWithEmpty(Object originalObject) {

        if (originalObject == null) return null;
        String json = null;
        try {
            json = withEmptyMapper.writeValueAsString(originalObject);
        } catch (Exception e) {
            logger.error("toJson error:", e);
        }

        return json;
    }

    /**
     * 将json转成List
     *
     * @param json      json字符串
     * @param valueType list泛型
     * @return 对象集合
     */
    public static <T> List<T> parseList(String json, Class<T> valueType) {

        return (List<T>) parseCollection(json, List.class, valueType);
    }

    /**
     * 将json转成List
     *
     * @param json      json字符串
     * @param valueType list泛型
     * @return 对象集合
     */
    public static <T, E extends Collection<?>> Collection<T> parseCollection(String json, Class<E> collectionClass, Class<T> valueType) {

        if (StringUtils.isEmpty(json) || valueType == null) return null;

        JavaType javaType = mapper.getTypeFactory().constructParametricType(collectionClass, valueType);

        Collection<T> objectList;
        try {
            objectList = mapper.readValue(json, javaType);
        } catch (Exception e) {
            logger.error("parseList error:" + e.getMessage(), e);
            objectList = null;
        }

        return objectList;
    }

    /**
     * 将json转成指定的类对象
     *
     * @param json json字符串
     * @param type 要转换的目标类型
     */
    @SuppressWarnings("unchecked")
    public static <T> T parse(String json, Class<T> type) {

        if (StringUtils.isEmpty(json) || type == null) return null;
        if (type == String.class) return (T) json;

        T result;
        try {
            result = mapper.readValue(json, type);
        } catch (Exception e) {
            logger.error("parse error:" + e.getMessage(), e);
            result = null;
        }

        return result;
    }

    public static SimpleModule getDateModule() {
        return dateModule;
    }
}
