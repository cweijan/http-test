package io.github.cweijan.mock.feign.proxy;


import io.github.cweijan.mock.util.JSON;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author cweijan
 * @since 2020/05/26 14:49
 */
public class StandardFeignInvoke implements FeignInvoke {

    private final Object feignClient;

    public StandardFeignInvoke(Object feignClient) {
        this.feignClient = feignClient;
    }

    @Override
    public Object invoke(Method method, Object[] args) {
        System.out.println("Request Method: " + method.getName() + ", Request Param: " + JSON.toJSON(args));
        Object invoke = null;
        try {
            Method feignMethod = feignClient.getClass().getMethod(method.getName(), method.getParameterTypes());
            invoke = feignMethod.invoke(feignClient, args);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Response Body: " + JSON.toJSON(invoke));
        System.out.println("-----------------------------------------------------------------------------------------------------------------------");
        return invoke;
    }
}
