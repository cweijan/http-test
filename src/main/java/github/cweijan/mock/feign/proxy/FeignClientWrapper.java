package github.cweijan.mock.feign.proxy;

/**
 * @author cweijan
 * @since 2020/05/26 14:12
 */
public interface FeignClientWrapper {

    <T> T create(Class<T> controllerClass,FeignInvoke feignInvoke);

}
