package io.github.cweijan.mock.feign.parse;

import io.github.cweijan.mock.context.HttpMockContext;
import org.springframework.util.StringUtils;

/**
 * @author cweijan
 * @since 2020/05/28 16:32
 */
public abstract class AbstractParser implements UrlParser {

    @Override
    public String parse(HttpMockContext httpMockContext, Class<?> controllerClass) {
        return getScheme(httpMockContext, controllerClass) + "://" +
                getHost(httpMockContext, controllerClass) +
                getContextPath(httpMockContext, controllerClass) +
                getPath(httpMockContext, controllerClass);
    }

    protected abstract String getPath(HttpMockContext httpMockContext, Class<?> controllerClass);

    protected String getContextPath(HttpMockContext httpMockContext, Class<?> controllerClass) {
        String contextPath = httpMockContext.getContextPath();
        return contextPath==null?"":contextPath;
    }

    protected String getHost(HttpMockContext httpMockContext, Class<?> controllerClass) {
        return httpMockContext.getHost() + ":" + httpMockContext.getPort();
    }

    protected String getScheme(HttpMockContext httpMockContext, Class<?> controllerClass) {
        return httpMockContext.getScheme();
    }

    protected String wrapPath(String url) {
        if (StringUtils.isEmpty(url)) return "/";

        if (!url.startsWith("/")) {
            return "/" + url;
        }

        return url;
    }

}
