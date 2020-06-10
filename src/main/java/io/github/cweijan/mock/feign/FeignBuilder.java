package io.github.cweijan.mock.feign;

import feign.Feign;
import feign.RequestInterceptor;
import feign.Retryer;
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
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.temporal.Temporal;
import java.util.*;

/**
 * @author cweijan
 * @since 2020/05/25 18:08
 */
public abstract class FeignBuilder {

    static final List<RequestInterceptor> REQUEST_INTERCEPTORS = new ArrayList<>();
    private static final UrlParser URL_PARSER = new StandardUrlParser();
    private static final FeignClientWrapper FEIGN_CLIENT_WRAPPER = new CglibClient();
    private static final ParameterNameDiscoverer NAME_DISCOVERER = new LocalVariableTableParameterNameDiscoverer();

    private static DynamicType.Builder<?> initMethodBuilder(DynamicType.Builder<?> builder, Method method) {

        Type genericReturnType = method.getGenericReturnType();
        DynamicType.Builder.MethodDefinition.ParameterDefinition<?> methodBuild;
        if (genericReturnType instanceof ParameterizedType) {
            methodBuild = builder.defineMethod(method.getName(), genericReturnType, Visibility.PUBLIC);
        } else {
            methodBuild = builder.defineMethod(method.getName(), method.getReturnType(), Visibility.PUBLIC);
        }
        boolean isQuery = method.getAnnotation(GetMapping.class) != null || method.getAnnotation(DeleteMapping.class) != null;
        Parameter[] parameters = method.getParameters();
        String[] parameterNames = NAME_DISCOVERER.getParameterNames(method);
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            if(parameter.getType().isInterface())continue;
            DynamicType.Builder.MethodDefinition.ParameterDefinition.Annotatable<?> tempBuilder = methodBuild.withParameter(parameter.getType(), Objects.requireNonNull(parameterNames)[i]).annotateParameter(parameter.getAnnotations());
            if (isQuery && parameter.getAnnotation(PathVariable.class) == null) {
                tempBuilder = tempBuilder.annotateParameter(AnnotationDescription.Builder.ofType(isSimple(parameter.getType()) ? RequestParam.class : SpringQueryMap.class).build());
            }
            methodBuild = tempBuilder;
        }

        builder = methodBuild.withoutCode().annotateMethod(method.getDeclaredAnnotations());
        return builder;
    }

    /**
     * if class is simple type
     *
     * @param type parameter type
     * @return return true when is simple
     */
    public static boolean isSimple(Class<?> type) {
        return type.isPrimitive() ||
                Temporal.class.isAssignableFrom(type) ||
                Date.class.isAssignableFrom(type) ||
                (type.getPackage() != null && type.getPackage().getName().startsWith("java.lang"));
    }

    /**
     * Create feign interface class by controller class.
     *
     * @param controllerClass 目标Controlller
     * @return feign接口
     */
    public static Class<?> generateFeignInterface(Class<?> controllerClass) {
        validateClass(controllerClass);
        DynamicType.Builder<?> builder = new ByteBuddy().makeInterface().merge(Visibility.PUBLIC);
        for (Method method : controllerClass.getMethods()) {
            if (AnnotationUtils.findAnnotation(method, RequestMapping.class) != null) {
                builder = initMethodBuilder(builder, method);
            }
        }
        return builder.make().load(FeignBuilder.class.getClassLoader()).getLoaded();
    }

    /**
     * create target controller proxy.
     *
     * @param controllerClass target controller
     * @param feignClient     feign client
     * @return proxy controller
     */
    static <T> T generateProxy(Class<T> controllerClass, Object feignClient) {
        return FEIGN_CLIENT_WRAPPER.create(controllerClass, new StandardFeignInvoke(feignClient));
    }

    /**
     * create feign client and store to holder.
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
                .retryer(new Retryer.Default(100, 1, 1))
                .encoder(SpringCodecHolder.getEncoder())
                .client(new InspectClient(null, null))
                .decoder(new OptionalDecoder(new ResponseEntityDecoder(SpringCodecHolder.getDecoder())))
                .contract(new SpringMvcContract(Collections.emptyList(), new DefaultFormattingConversionService()))
                .target(feignInterface, url);
    }

    /**
     * check inject class is valid
     *
     * @param controllerClass inject target class
     */
    private static <T> void validateClass(Class<T> controllerClass) {

        Objects.requireNonNull(controllerClass);

        if (AnnotationUtils.findAnnotation(controllerClass, Controller.class) == null
                && controllerClass.getAnnotation(FeignClient.class) == null) {
            throw new UnsupportedOperationException("只支持创建controller代理对象!");
        }

    }

}
