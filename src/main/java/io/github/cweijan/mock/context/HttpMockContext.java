package io.github.cweijan.mock.context;

/**
 * @author cweijan
 * @since 2020/05/25 17:04
 */
public class HttpMockContext {

    private final String scheme;
    private final String host;
    private final Integer port;
    private final String contextPath;

    public HttpMockContext(String scheme, String host, Integer port) {
        this(scheme,host,port,null);
    }

    public HttpMockContext(String scheme, String host, Integer port, String contextPath) {
        this.scheme = scheme;
        this.host = host;
        this.port = port;
        this.contextPath = contextPath;
    }

    public String getScheme() {
        return scheme;
    }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    public String getContextPath() {
        return contextPath;
    }
}
