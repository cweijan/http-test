package io.github.cweijan.mock.jupiter;

import ch.qos.logback.classic.LoggerContext;
import org.junit.jupiter.api.extension.*;
import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Controller;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * @author cweijan
 * @since 2020/05/25 16:16
 */
public class HttpMockExtension implements ParameterResolver, TestInstancePostProcessor, ExecutionCondition {

    private MockInstanceContext mockInstanceContext;
    private String reason;

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext extensionContext) {
        disableLoggin();
        initContext(testInstance);
        resolveInject(testInstance);
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return AnnotationUtils.findAnnotation(parameterContext.getParameter().getType(), Controller.class) != null;
    }

    private void initContext(Object testInstance) {
        HttpTest httpTest = testInstance.getClass().getAnnotation(HttpTest.class);
        this.mockInstanceContext = new MockInstanceContext(httpTest);
        this.reason = mockInstanceContext.checkBootRunning();
    }

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext extensionContext) {
        if (reason != null) {
            return ConditionEvaluationResult.disabled(reason + ", disable " + extensionContext.getDisplayName());
        }
        return ConditionEvaluationResult.enabled(null);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return mockInstanceContext.getInstance(parameterContext.getParameter().getType());
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
            declaredField.setAccessible(true);
            Object resolved = mockInstanceContext.resolveField(declaredField);
            if (resolved != null) {
                ReflectionUtils.setField(declaredField, o, resolved);
            }

        }
    }

}
