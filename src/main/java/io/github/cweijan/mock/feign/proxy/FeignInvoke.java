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
     * @return feign execute response
     * @throws Throwable 执行代理方法时跑抛出的任意异常
     */
    Object invoke(Method method, Object[] args) throws Throwable;

}
