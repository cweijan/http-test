package io.github.cweijan.mock.jupiter;

import io.github.cweijan.mock.util.ReflectUtils;
import org.junit.jupiter.api.extension.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Controller;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * @author cweijan
 * @since 2020/05/25 16:16
 */
public class HttpMockExtension implements ParameterResolver, TestInstancePostProcessor {

    private MockInstanceHolder mockInstanceHolder;

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext extensionContext) {

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
        this.mockInstanceHolder = new MockInstanceHolder(httpTest.scheme(), httpTest.host(), httpTest.port());
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
                ReflectUtils.setFieldValue(o, declaredField, mockInstanceHolder.getInstance(declaredField.getType()));
            }
        }
    }
}
