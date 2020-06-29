package io.github.cweijan.mock.feign.proxy;


import io.github.cweijan.mock.feign.config.InternalConfig;
import io.github.cweijan.mock.request.Any;
import io.github.cweijan.mock.request.Generator;
import io.github.cweijan.mock.util.JSON;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

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
    public Object invoke(Method method, Object[] args) throws Throwable {
        Object invoke;
        try {
            Parameter[] parameters = method.getParameters();
            if(Any.get(method)){
                for (int i = 0; i < args.length; i++) {
                    if (args[i] == null) args[i] = Generator.request(parameters[i].getType());
                }
            }
            Object param=args.length==1?args[0]:args;
            System.out.println("Request -> " + method.getName() + " -> " +
                    (InternalConfig.PRETTY_REQUEST?JSON.printJSON(param):JSON.toJSON(param)));
            Method feignMethod = feignClient.getClass().getMethod(method.getName(), method.getParameterTypes());
            invoke = feignMethod.invoke(feignClient, args);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
        return invoke;
    }
}
