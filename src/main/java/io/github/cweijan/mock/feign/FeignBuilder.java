package io.github.cweijan.mock.feign;

import feign.Feign;
import feign.RequestInterceptor;
import feign.optionals.OptionalDecoder;
import io.github.cweijan.mock.context.HttpMockContext;
import io.github.cweijan.mock.feign.parse.StandardUrlParser;
import io.github.cweijan.mock.feign.parse.UrlParser;
import io.github.cweijan.mock.feign.proxy.CglibClient;
import io.github.cweijan.mock.feign.proxy.FeignClientWrapper;
import io.github.cweijan.mock.feign.proxy.StandardFeignInvoke;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.cloud.openfeign.support.*;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.ResourceRegionHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.http.converter.xml.SourceHttpMessageConverter;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author cweijan
 * @since 2020/05/25 18:08
 */
public class FeignBuilder {

    static final List<RequestInterceptor> REQUEST_INTERCEPTORS = new ArrayList<>();
    private static final UrlParser URL_PARSER = new StandardUrlParser();
    private static final FeignClientWrapper FEIGN_CLIENT_WRAPPER = new CglibClient();
    private static final ParameterNameDiscoverer NAME_DISCOVERER = new LocalVariableTableParameterNameDiscoverer();
    private static final ObjectFactory<HttpMessageConverters> httpMessageConvertersObjectFactory = () -> new HttpMessageConverters(
            new ByteArrayHttpMessageConverter(), new StringHttpMessageConverter(), new ResourceHttpMessageConverter(),
            new ResourceRegionHttpMessageConverter(), new SourceHttpMessageConverter<>(), new AllEncompassingFormHttpMessageConverter(),
            new MappingJackson2HttpMessageConverter(), new Jaxb2RootElementHttpMessageConverter());

    private static DynamicType.Builder<?> initMethodBuilder(DynamicType.Builder<?> builder, Method method) {
        DynamicType.Builder.MethodDefinition.ParameterDefinition<?> methodBuild = builder.defineMethod(method.getName(), method.getReturnType(), Visibility.PUBLIC);
        boolean isQuery = method.getAnnotation(GetMapping.class) != null || method.getAnnotation(DeleteMapping.class) != null;
        Parameter[] parameters = method.getParameters();
        String[] parameterNames = NAME_DISCOVERER.getParameterNames(method);
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            if (isQuery && parameter.getAnnotations().length == 0) {
                methodBuild = methodBuild.withParameter(parameter.getType(), parameterNames != null ? parameterNames[i] : null)
                        .annotateParameter(AnnotationDescription.Builder.ofType(isSimple(parameter.getType()) ? RequestParam.class : SpringQueryMap.class).build());
            } else {
                methodBuild = methodBuild.withParameter(parameter.getType(), parameter.getName()).annotateParameter(parameter.getAnnotations());
            }
        }

        builder = methodBuild.withoutCode().annotateMethod(method.getDeclaredAnnotations());
        return builder;
    }

    public static boolean isSimple(Class<?> type) {
        return type.isPrimitive() ||
                Temporal.class.isAssignableFrom(type) ||
                Date.class.isAssignableFrom(type) ||
                (type.getPackage() != null && type.getPackage().getName().startsWith("java.lang"));
    }

    /**
     * 根据目标controller的方法构造出feign接口
     *
     * @param controllerClass 目标Controlller
     * @return feign接口
     */
    public static Class<?> generateFeignInterface(Class<?> controllerClass) {
        DynamicType.Builder<?> builder = new ByteBuddy().makeInterface().merge(Visibility.PUBLIC);
        for (Method method : controllerClass.getMethods()) {
            if (AnnotationUtils.findAnnotation(method, RequestMapping.class) != null) {
                builder = initMethodBuilder(builder, method);
            }
        }
        return builder.make().load(FeignBuilder.class.getClassLoader()).getLoaded();
    }

    /**
     * 创建目标controller的代理类
     *
     * @param controllerClass 目标controller
     * @param feignClient
     * @return 代理controller
     */
    static <T> T generateProxy(Class<T> controllerClass, Object feignClient) {
        return FEIGN_CLIENT_WRAPPER.create(controllerClass, new StandardFeignInvoke(feignClient));
    }

    /**
     * 创建一个feign客户端并存进上下文
     *
     * @param controllerClass 原始类型
     * @param mockContext     spring应用web上下文信息
     * @return feign实例
     */
    static Object createFeignClient(Class<?> controllerClass, HttpMockContext mockContext) {
        Class<?> feignInterface = generateFeignInterface(controllerClass);
        String url = URL_PARSER.parse(mockContext, controllerClass);
        return Feign.builder()
                .requestInterceptors(REQUEST_INTERCEPTORS)
                .encoder(new PageableSpringEncoder(new SpringEncoder(httpMessageConvertersObjectFactory)))
                .decoder(new OptionalDecoder(new ResponseEntityDecoder(new SpringDecoder(httpMessageConvertersObjectFactory))))
                .contract(new SpringMvcContract(Collections.emptyList(), new DefaultFormattingConversionService()))
                .target(feignInterface, url);
    }

}
