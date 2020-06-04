package io.github.cweijan.mock.jupiter.inject;

import java.lang.reflect.Field;

/**
 * @author cweijan
 * @since 2020/06/04 16:06
 */
public interface FieldResolver {

    /**
     * 对field进行解析
     * @param field 要解析的field
     * @return 返回非空表示解析成功
     */
    Object resolve(Field field);


}
