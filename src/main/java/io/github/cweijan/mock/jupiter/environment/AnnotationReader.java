package io.github.cweijan.mock.jupiter.environment;

import io.github.cweijan.mock.jupiter.HttpTest;

/**
 * @author cweijan
 * @since 2020/06/03 22:57
 */
public class AnnotationReader implements HttpMockContextReader{
    private final HttpTest httpTest;

    public AnnotationReader(HttpTest httpTest) {
        this.httpTest = httpTest;
    }

    @Override
    public String getHost() {
        return httpTest.host();
    }

    @Override
    public String getContextPath() {
        return httpTest.contextPath();
    }

    @Override
    public Integer getPort() {
        return httpTest.port();
    }

    @Override
    public String getScheme() {
        return httpTest.scheme();
    }
}
