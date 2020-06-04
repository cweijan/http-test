package io.github.cweijan.mock.feign.proxy;

/**
 * 创建feign客户端包装类
 * @author cweijan
 * @since 2020/05/26 14:12
 */
public interface FeignClientWrapper {

    <T> T create(Class<T> controllerClass,FeignInvoke feignInvoke);

}
