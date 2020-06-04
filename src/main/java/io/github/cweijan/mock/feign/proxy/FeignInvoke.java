package io.github.cweijan.mock.feign.proxy;

import java.lang.reflect.Method;

/**
 * feign client执行接口
 * @author cweijan
 * @since 2020/05/26 14:47
 */
public interface FeignInvoke {

    /**
     * invoke feign client
     * @param method target method
     * @param args params
     * @return response
     */
    Object invoke(Method method, Object[] args) throws Throwable;

}
