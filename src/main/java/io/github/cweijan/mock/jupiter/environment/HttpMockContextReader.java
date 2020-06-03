package io.github.cweijan.mock.jupiter.environment;

/**
 * @author cweijan
 * @since 2020/06/03 22:51
 */
public interface HttpMockContextReader {

    String getHost();

    String getContextPath();

    Integer getPort();

    String getScheme();

}
