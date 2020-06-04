package io.github.cweijan.mock.jupiter.inject;

import io.github.cweijan.mock.jupiter.MockInstanceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.annotation.Resource;
import java.lang.reflect.Field;

/**
 * @author cweijan
 * @since 2020/06/04 16:09
 */
public class FeignFieldResolver implements FieldResolver {

    private final MockInstanceContext mockInstanceContext;

    public FeignFieldResolver(MockInstanceContext mockInstanceContext) {
        this.mockInstanceContext = mockInstanceContext;
    }

    @Override
    public Object resolve(Field field) {

        if (field.getDeclaredAnnotation(Autowired.class) != null || field.getDeclaredAnnotation(Resource.class) != null
                || field.getDeclaredAnnotation(Qualifier.class) != null) {
            return mockInstanceContext.getInstance(field.getType());
        }
        return null;
    }

}
