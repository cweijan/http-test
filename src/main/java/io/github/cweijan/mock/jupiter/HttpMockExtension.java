package io.github.cweijan.mock.jupiter;

import ch.qos.logback.classic.LoggerContext;
import io.github.cweijan.mock.context.HttpMockContext;
import io.github.cweijan.mock.jupiter.environment.HttpMockContextParser;
import io.github.cweijan.mock.jupiter.exception.ConnectSpringBootException;
import org.junit.jupiter.api.extension.*;
import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Controller;
import org.springframework.util.ReflectionUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.Socket;

/**
 * @author cweijan
 * @since 2020/05/25 16:16
 */
public class HttpMockExtension implements ParameterResolver, TestInstancePostProcessor {

    private MockInstanceHolder mockInstanceHolder;

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext extensionContext) {
        disableLoggin();
        initContext(testInstance);
        resolveInject(testInstance);

    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return AnnotationUtils.findAnnotation(parameterContext.getParameter().getType(), Controller.class) != null;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {

        return mockInstanceHolder.getInstance(parameterContext.getParameter().getType());
    }

    private void initContext(Object testInstance) {
        HttpTest httpTest = testInstance.getClass().getAnnotation(HttpTest.class);
        HttpMockContext mockContext = new HttpMockContextParser(httpTest).parse();
        checkBootRunning(mockContext);
        this.mockInstanceHolder = new MockInstanceHolder(mockContext);
    }

    private boolean checkBootRunning(HttpMockContext mockContext) {
        try (Socket ignored = new Socket(mockContext.getHost(),  mockContext.getPort())) {
            return true;
        } catch (IOException ioException) {
            throw new ConnectSpringBootException(mockContext.getHost(),mockContext.getPort(),ioException);
        }
    }

    private void disableLoggin() {
        ILoggerFactory iLoggerFactory = LoggerFactory.getILoggerFactory();
        if (iLoggerFactory.getClass() == LoggerContext.class) {
            ((LoggerContext) iLoggerFactory).reset();
        }
    }

    private void resolveInject(Object o) {
        Field[] declaredFields = o.getClass().getDeclaredFields();
        for (Field declaredField : declaredFields) {
            int modifiers = declaredField.getModifiers();
            if (Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers)) {
                continue;
            }
            if (declaredField.getDeclaredAnnotation(Autowired.class) != null
                    || declaredField.getDeclaredAnnotation(Resource.class) != null
                    || declaredField.getDeclaredAnnotation(Qualifier.class) != null) {
                declaredField.setAccessible(true);
                ReflectionUtils.setField(declaredField, o, mockInstanceHolder.getInstance(declaredField.getType()));
            }
        }
    }
}
