package io.github.cweijan.mock.feign.jackson.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 扩展jackson反序列, 使其支持时间戳转LocalDateTime
 * @author cweijan
 * @since 2020/05/21 9:51
 */
public class LocalDateTimeExtDeserializer extends LocalDateTimeDeserializer {
    public LocalDateTimeExtDeserializer() {
        super(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public LocalDateTimeExtDeserializer(DateTimeFormatter formatter) {
        super(formatter);
    }

    @Override
    protected LocalDateTimeDeserializer withDateFormat(DateTimeFormatter formatter) {
        return new LocalDateTimeExtDeserializer(formatter);
    }

    @Override
    public LocalDateTime deserialize(JsonParser parse, DeserializationContext context) throws IOException{
        if(parse.hasToken(JsonToken.VALUE_NUMBER_INT)){
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(parse.getLongValue()), ZoneId.systemDefault());
        }
        return super.deserialize(parse, context);
    }

}
