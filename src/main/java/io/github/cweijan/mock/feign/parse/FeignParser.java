package io.github.cweijan.mock.feign.parse;

import io.github.cweijan.mock.context.HttpMockContext;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author cweijan
 * @since 2020/05/28 16:40
 */
public class FeignParser extends AbstractParser {
    @Override
    protected String getPath(HttpMockContext httpMockContext, Class<?> controllerClass) {

        FeignClient feignClient = controllerClass.getAnnotation(FeignClient.class);

        return wrapPath(feignClient.path());
    }

    @Override
    public boolean supportParse(Class<?> controllerClass) {
        return controllerClass.getAnnotation(FeignClient.class) != null;
    }
}
