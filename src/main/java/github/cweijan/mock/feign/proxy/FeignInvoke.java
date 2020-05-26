package github.cweijan.mock.feign.proxy;

import java.lang.reflect.Method;

/**
 * @author cweijan
 * @since 2020/05/26 14:47
 */
public interface FeignInvoke {

    Object invoke(Method method, Object[] args);

}
