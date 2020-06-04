package io.github.cweijan.mock.jupiter.exception;

/**
 * 当连接springboot应用失败时抛出该异常
 * @author cweijan
 * @since 2020/06/04 11:21
 */
public class ConnectSpringBootException extends RuntimeException{
    public ConnectSpringBootException(String host,Integer port, Throwable cause) {
        super("连接失败 -> "+host+":"+port, cause);
    }
}
