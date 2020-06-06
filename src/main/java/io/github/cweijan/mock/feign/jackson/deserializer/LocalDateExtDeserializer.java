package io.github.cweijan.mock.feign.jackson.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 扩展jackson反序列, 使其支持时间戳转LocalDate
 * @author cweijan
 * @since 2020/05/21 10:06
 */
public class LocalDateExtDeserializer extends LocalDateDeserializer {

    public LocalDateExtDeserializer() {
        super(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    public LocalDateExtDeserializer(DateTimeFormatter dtf) {
        super(dtf);
    }

    @Override
    protected LocalDateDeserializer withDateFormat(DateTimeFormatter formatter) {
        return new LocalDateExtDeserializer(formatter);
    }

    @Override
    public LocalDate deserialize(JsonParser parse, DeserializationContext context) throws IOException {
        if (parse.hasToken(JsonToken.VALUE_NUMBER_INT)) {
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(parse.getLongValue()), ZoneId.systemDefault()).toLocalDate();
        }
        return super.deserialize(parse, context);
    }

}
