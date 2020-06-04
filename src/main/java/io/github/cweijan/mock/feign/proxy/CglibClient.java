package io.github.cweijan.mock.feign.proxy;

import org.springframework.cglib.proxy.Callback;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.objenesis.ObjenesisHelper;

/**
 * 用于创建controller代理, 内部代码为调用feign客户端.
 * @author cweijan
 * @since 2020/05/26 14:23
 */
public class CglibClient implements FeignClientWrapper {

    /**
     * create feign invoker wrapper
     * @param controllerClass wrapper class
     * @param feignInvoke invoker
     * @return feign wrpper proxy
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T create(Class<T> controllerClass, FeignInvoke feignInvoke) {

        Enhancer e = new Enhancer();
        e.setSuperclass(controllerClass);
        e.setCallbackType(MethodInterceptor.class);
        Class<?> dynamicClass = e.createClass();
        Enhancer.registerCallbacks(dynamicClass, new Callback[]{(MethodInterceptor) (o1, method, args, methodProxy) -> feignInvoke.invoke(method, args)});
        return (T) ObjenesisHelper.newInstance(dynamicClass);
    }

}
