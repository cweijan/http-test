package io.github.cweijan.mock.feign.jackson.serializer;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * 将LocalDate转为时间戳
 *
 * @author cweijan
 * @version 2019/9/5 11:39
 */
public class LocalDateExtSerializer extends LocalDateSerializer {
    private final boolean writeDatesAsTimestamps;

    public LocalDateExtSerializer(boolean writeDatesAsTimestamps) {
        super(DateTimeFormatter.ISO_LOCAL_DATE);
        this.writeDatesAsTimestamps = writeDatesAsTimestamps;
    }

    protected LocalDateExtSerializer(LocalDateSerializer base,
                                  Boolean useTimestamp, DateTimeFormatter dtf, JsonFormat.Shape shape,Boolean writeDatesAsTimestamps) {
        super(base, useTimestamp, dtf, shape);
        this.writeDatesAsTimestamps=writeDatesAsTimestamps;
    }

    @Override
    protected LocalDateSerializer withFormat(Boolean useTimestamp, DateTimeFormatter dtf, JsonFormat.Shape shape) {
        return new LocalDateExtSerializer(this, useTimestamp, dtf, shape,writeDatesAsTimestamps);
    }

    @Override
    public void serialize(LocalDate localDate, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (writeDatesAsTimestamps) {
            jsonGenerator.writeNumber(localDate.atStartOfDay(ZoneOffset.systemDefault()).toInstant().toEpochMilli());
        } else {
            super.serialize(localDate, jsonGenerator, serializerProvider);
        }
    }
}
