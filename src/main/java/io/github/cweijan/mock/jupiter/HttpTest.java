package io.github.cweijan.mock.jupiter;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.*;

/**
 * 在测试类标注该注解后可进行http测试
 * @author cweijan
 * @since 2020/05/25 16:13
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@ExtendWith(HttpMockExtension.class)
public @interface HttpTest {
    /**
     * 测试应用host
     */
    String host() default "127.0.0.1";

    /**
     * 测试应用端口, 为空则读取spring boot配置文件
     */
    int port() default 0;

    /**
     * 测试协议
     */
    String scheme() default "http";

    /**
     * 测试上下文地址, 为空则读取spring boot配置文件
     */
    String contextPath() default "";
}
