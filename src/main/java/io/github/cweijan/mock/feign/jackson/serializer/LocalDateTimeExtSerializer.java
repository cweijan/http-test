package io.github.cweijan.mock.feign.jackson.serializer;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 将LocalDateTime转为时间戳
 *
 * @author cweijan
 * @version 2019/9/5 11:41
 */
public class LocalDateTimeExtSerializer extends LocalDateTimeSerializer {
    private final boolean writeDatesAsTimestamps;

    public LocalDateTimeExtSerializer(boolean writeDatesAsTimestamps, String pattern) {
        super(DateTimeFormatter.ofPattern(pattern));
        this.writeDatesAsTimestamps = writeDatesAsTimestamps;
    }

    protected LocalDateTimeExtSerializer(DateTimeFormatter f, Boolean writeDatesAsTimestamps) {
        super(f);
        this.writeDatesAsTimestamps = writeDatesAsTimestamps;
    }


    @Override
    protected LocalDateTimeSerializer withFormat(Boolean useTimestamp, DateTimeFormatter f, JsonFormat.Shape shape) {
        return new LocalDateTimeExtSerializer(f, writeDatesAsTimestamps);
    }

    @Override
    public void serialize(LocalDateTime localDateTime, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (writeDatesAsTimestamps) {
            jsonGenerator.writeNumber(localDateTime.toInstant(OffsetDateTime.now().getOffset()).toEpochMilli());
        } else {
            super.serialize(localDateTime, jsonGenerator, serializerProvider);
        }
    }
}
