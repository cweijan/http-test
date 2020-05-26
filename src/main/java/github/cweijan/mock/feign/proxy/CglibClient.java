package github.cweijan.mock.feign.proxy;

import org.springframework.cglib.proxy.Callback;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.objenesis.ObjenesisHelper;

/**
 * @author cweijan
 * @since 2020/05/26 14:23
 */
public class CglibClient implements FeignClientWrapper {
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
