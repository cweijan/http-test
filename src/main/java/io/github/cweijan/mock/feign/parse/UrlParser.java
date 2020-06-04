package io.github.cweijan.mock.feign.parse;

import io.github.cweijan.mock.context.HttpMockContext;

/**
 * 用于解析feign调用最终url
 * @author cweijan
 * @since 2020/05/28 16:27
 */
public interface UrlParser {

    boolean supportParse(Class<?> controllerClass);

    String parse(HttpMockContext httpMockContext, Class<?> controllerClass);
}
