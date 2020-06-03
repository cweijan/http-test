package io.github.cweijan.mock.jupiter;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.*;

/**
 * @author cweijan
 * @since 2020/05/25 16:13
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@ExtendWith(HttpMockExtension.class)
public @interface HttpTest {
    String host() default "127.0.0.1";

    int port();

    String scheme() default "http";

    String contextPath() default "";
}
