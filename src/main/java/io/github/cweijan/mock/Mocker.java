package io.github.cweijan.mock;

import feign.RequestInterceptor;
import io.github.cweijan.mock.context.HttpMockContext;
import io.github.cweijan.mock.feign.FeignMockerContext;
import io.github.cweijan.mock.feign.SpringCodecHolder;
import io.github.cweijan.mock.request.Generator;
import org.springframework.http.converter.HttpMessageConverter;

/**
 * @author cweijan
 * @since 2020/05/21 17:30
 */
public abstract class Mocker {

    /**
     * 调用controller方法时会发送请求到本地spring boot应用
     * 调用该方法会创建3个核心对象: feign接口, feign接口代理, controller代理
     *
     * @param controllerClass 目标controller
     * @param port            springboot应用端口
     * @param <T>             controller
     * @return controller的http代理类
     */
    public static <T> T api(Class<T> controllerClass, Integer port) {
        return api(controllerClass, new HttpMockContext("http", "127.0.0.1", port));
    }

    /**
     * 调用controller方法时会发送请求到本地spring boot应用
     * 调用该方法会创建3个核心对象: feign接口, feign接口代理, controller代理
     *
     * @param controllerClass 目标controller
     * @param context         目标web应用上下文
     * @param <T>             controller
     * @return controller的http代理类
     */
    public static <T> T api(Class<T> controllerClass, HttpMockContext context) {

        return FeignMockerContext.getFeignClientWrapper(controllerClass, context);
    }

    /**
     * 增加请求拦截器, 对所有请求生效
     *
     * @param requestInterceptor 请求拦截器
     */
    public static void addRequestInterceptor(RequestInterceptor requestInterceptor) {
        FeignMockerContext.addInterceptor(requestInterceptor);
    }

    /**
     * 增加RequestBody消息转换器(Preview)
     *
     * @param httpMessageConverter 消息转换器
     */
    public static void addHttpMesagerConvert(HttpMessageConverter<?> httpMessageConverter) {
        SpringCodecHolder.addHttpMessageConveter(httpMessageConverter);
    }

    /**
     * 创建虚拟对象并对field随机赋值
     *
     * @param <T>       要解析的类型
     * @param mockClass 目标类型
     * @return 目标实例
     */
    public static <T> T mock(Class<T> mockClass) {
        return Generator.any(mockClass);
    }

}
