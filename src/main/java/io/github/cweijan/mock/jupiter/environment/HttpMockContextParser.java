package io.github.cweijan.mock.jupiter.environment;

import io.github.cweijan.mock.context.HttpMockContext;
import io.github.cweijan.mock.jupiter.HttpTest;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author cweijan
 * @since 2020/06/03 22:54
 */
public class HttpMockContextParser {

    private final List<HttpMockContextReader> httpMockContextReaders = new ArrayList<>();

    public HttpMockContextParser(HttpTest httpTest) {
        this.httpMockContextReaders.add(new AnnotationReader(httpTest));
        this.httpMockContextReaders.add(new BootEnvironmentReader(httpTest));
    }

    public HttpMockContext parse() {

        String scheme = null;
        String host = null;
        String contextPath = null;
        Integer port = null;
        for (HttpMockContextReader httpMockContextReader : httpMockContextReaders) {
            if (StringUtils.isEmpty(scheme)) {
                scheme = httpMockContextReader.getScheme();
            }
            if (StringUtils.isEmpty(host)) {
                host = httpMockContextReader.getHost();
            }
            if (StringUtils.isEmpty(contextPath)) {
                contextPath = httpMockContextReader.getContextPath();
            }
            if (port == null || port == 0) {
                port = httpMockContextReader.getPort();
            }
        }

        return new HttpMockContext(scheme, host, port, contextPath);
    }

}
