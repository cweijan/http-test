package io.github.cweijan.mock.feign;

import feign.RequestInterceptor;
import io.github.cweijan.mock.context.HttpMockContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author cweijan
 * @since 2020/05/22 15:36
 */
public class FeignMockerContext {

    private static final Map<Class<?>, Object> PROXY_CACHE = new HashMap<>();

    static {
        addInterceptor(template -> template.header("feign-flag", "true"));
    }

    public static void addInterceptor(RequestInterceptor requestInterceptor) {
        FeignBuilder.REQUEST_INTERCEPTORS.add(requestInterceptor);
    }

    /**
     * 根据目标controller的方法构造出feign接口
     *
     * @param controllerClass 目标Controlller
     * @param context
     */
    @SuppressWarnings("unchecked")
    public static <T> T getFeignClientWrapper(Class<T> controllerClass, HttpMockContext context) {

        return (T) PROXY_CACHE.computeIfAbsent(controllerClass, keyClass -> {
            Objects.requireNonNull(controllerClass);
            Object feignClient = FeignBuilder.createFeignClient(controllerClass, context);
            return FeignBuilder.generateProxy(controllerClass, feignClient);
        });
    }

}
