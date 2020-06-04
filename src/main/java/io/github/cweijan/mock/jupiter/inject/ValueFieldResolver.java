package io.github.cweijan.mock.jupiter.inject;

import io.github.cweijan.mock.jupiter.MockInstanceContext;
import io.github.cweijan.mock.jupiter.environment.BootEnvironmentReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.PropertyPlaceholderHelper;

import java.lang.reflect.Field;

/**
 * @author cweijan
 * @since 2020/06/04 16:20
 */
public class ValueFieldResolver implements FieldResolver {

    private final PropertyPlaceholderHelper propertyPlaceholderHelper;
    private final BootEnvironmentReader bootwEnvironmentReader;

    public ValueFieldResolver(MockInstanceContext mockInstanceContext) {
        bootwEnvironmentReader = mockInstanceContext.getBootwEnvironmentReader();
        propertyPlaceholderHelper = new PropertyPlaceholderHelper("${", "}", ":", true);
    }

    @Override
    public Object resolve(Field field) {

        Value value = field.getDeclaredAnnotation(Value.class);
        if (value != null) {
            String resolvedText = propertyPlaceholderHelper.replacePlaceholders(value.value(), bootwEnvironmentReader::get);
            return execute(resolvedText, field.getType());
        }

        return null;
    }

    private static final SpelExpressionParser parse = new SpelExpressionParser();

    private static <T> T execute(String expression, Class<T> clazz) {
        Expression expressionResult = parse.parseExpression(expression);
        return expressionResult.getValue(clazz);
    }

}
