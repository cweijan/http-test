package io.github.cweijan.mock;

import io.github.cweijan.mock.util.JSON;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

class MockerTest {

    @Test
    void mock() {
        System.out.println(Mocker.mock(Integer.class));
        System.out.println(Mocker.mock(int.class));
        System.out.println(Mocker.mock(Boolean.class));
        System.out.println(Mocker.mock(boolean.class));
        System.out.println(Mocker.mock(Long.class));
        System.out.println(Mocker.mock(long.class));
        System.out.println(Mocker.mock(Double.class));
        System.out.println(Mocker.mock(double.class));
        System.out.println(Mocker.mock(Float.class));
        System.out.println(Mocker.mock(float.class));
        System.out.println(Mocker.mock(Date.class));
        System.out.println(Mocker.mock(Short.class));
        System.out.println(Mocker.mock(short.class));
        System.out.println(Mocker.mock(Character.class));
        System.out.println(Mocker.mock(char.class));
        System.out.println(Mocker.mock(BigDecimal.class));
        System.out.println(Mocker.mock(BigInteger.class));
        System.out.println(Mocker.mock(LocalDate.class));
        System.out.println(Mocker.mock(LocalDateTime.class));
        System.out.println(Mocker.mock(LocalTime.class));
        System.out.println(JSON.toJSON(Mocker.mock(int[].class)));
        System.out.println(JSON.toJSON(Mocker.mock(Long[].class)));
        System.out.println(JSON.toJSON(Mocker.mock(BigDecimal[].class)));
        System.out.println(JSON.toJSON(Mocker.mock(Integer[].class)));
        System.out.println(JSON.toJSON(Mocker.mock(LocalTime[].class)));
    }

}
