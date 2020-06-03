package io.github.cweijan.mock.jupiter;

import io.github.cweijan.mock.Mocker;
import io.github.cweijan.mock.context.HttpMockContext;

import java.util.HashMap;
import java.util.Map;

/**
 * @author cweijan
 * @since 2020/05/25 18:17
 */
public class MockInstanceHolder {

    private final HttpMockContext context;
    private final Map<String, Object> instanceMap = new HashMap<>();

    public MockInstanceHolder(HttpMockContext context) {
        this.context = context;
    }

    @SuppressWarnings("unchecked")
    public <T> T getInstance(Class<T> instanceClass) {

        return (T) instanceMap.computeIfAbsent(
                instanceClass.getSimpleName() + "_" + context.getScheme() + "_" + context.getHost() + "_" + context.getPort(),
                key -> Mocker.api(instanceClass, context)
        );
    }

}
