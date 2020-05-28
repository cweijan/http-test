package io.github.cweijan.mock.feign.parse;

import io.github.cweijan.mock.context.HttpMockContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author cweijan
 * @since 2020/05/28 16:28
 */
public class RequestMappingParser extends AbstractParser {

    @Override
    public boolean supportParse(Class<?> controllerClass) {
        return AnnotationUtils.findAnnotation(controllerClass, RequestMapping.class) != null;
    }

    @Override
    protected String getPath(HttpMockContext httpMockContext, Class<?> controllerClass) {

        RequestMapping requestMapping = AnnotationUtils.findAnnotation(controllerClass, RequestMapping.class);
        String[] value = requestMapping.value();

        return value.length > 0 ? wrapPath(value[0]) : "/";
    }

}
