package io.github.cweijan.mock.boot.controller;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author cweijan
 * @since 2020/06/03 21:13
 */
@RestControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler
    public String exceptionHandler(Throwable throwable){
        return throwable.getMessage();
    }

}
