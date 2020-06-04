package io.github.cweijan.mock.jupiter.inject;

import io.github.cweijan.mock.jupiter.MockInstanceContext;
import io.github.cweijan.mock.jupiter.environment.BootEnvironmentReader;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanExpressionContext;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.context.expression.StandardBeanExpressionResolver;
import org.springframework.util.PropertyPlaceholderHelper;

import java.lang.reflect.Field;

/**
 * Value anntaion resolver.
 * @author cweijan
 * @since 2020/06/04 16:20
 */
public class ValueFieldResolver implements FieldResolver {

    private final PropertyPlaceholderHelper propertyPlaceholderHelper;
    private final BootEnvironmentReader bootwEnvironmentReader;
    private final StandardBeanExpressionResolver standardBeanExpressionResolver;
    private TypeConverter typeConverter;
    private BeanExpressionContext beanExpressionContext;

    public ValueFieldResolver(MockInstanceContext mockInstanceContext) {
        this.standardBeanExpressionResolver = new StandardBeanExpressionResolver();
        this.bootwEnvironmentReader = mockInstanceContext.getBootwEnvironmentReader();
        this.propertyPlaceholderHelper = new PropertyPlaceholderHelper("${", "}", ":", true);
        initConverter();
    }

    private void initConverter() {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        beanFactory.setConversionService(ApplicationConversionService.getSharedInstance());
        this.typeConverter = beanFactory.getTypeConverter();
        this.beanExpressionContext=new BeanExpressionContext(beanFactory,null);
    }

    @Override
    public Object resolve(Field field) {
        Value value = field.getDeclaredAnnotation(Value.class);
        if (value != null) {
            String resolvedText = propertyPlaceholderHelper.replacePlaceholders(value.value(), bootwEnvironmentReader::get);
            Object execute = execute(resolvedText);
            return typeConverter.convertIfNecessary(execute, field.getType(), field);
        }
        return null;
    }

    private Object execute(String expression) {
        return standardBeanExpressionResolver.evaluate(expression, beanExpressionContext);
    }

}
