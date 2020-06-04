package io.github.cweijan.mock.jupiter;

import io.github.cweijan.mock.Mocker;
import io.github.cweijan.mock.context.HttpMockContext;
import io.github.cweijan.mock.jupiter.environment.BootEnvironmentReader;
import io.github.cweijan.mock.jupiter.environment.HttpMockContextParser;
import io.github.cweijan.mock.jupiter.inject.FeignFieldResolver;
import io.github.cweijan.mock.jupiter.inject.FieldResolver;
import io.github.cweijan.mock.jupiter.inject.ValueFieldResolver;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author cweijan
 * @since 2020/05/25 18:17
 */
public class MockInstanceContext {

    private final HttpMockContext context;
    private final Map<String, Object> instanceMap = new HashMap<>();
    private final BootEnvironmentReader bootwEnvironmentReader;
    private final List<FieldResolver> fieldResolverList;

    public MockInstanceContext(HttpTest httpTest) {
        HttpMockContextParser httpMockContextParser = new HttpMockContextParser(httpTest);
        this.bootwEnvironmentReader = httpMockContextParser.getBootEnvironmentReader();
        this.context = httpMockContextParser.parse();
        this.fieldResolverList = Arrays.asList(new FeignFieldResolver(this), new ValueFieldResolver(this));
    }

    @SuppressWarnings("unchecked")
    public <T> T getInstance(Class<T> instanceClass) {

        return (T) instanceMap.computeIfAbsent(
                instanceClass.getSimpleName() + "_" + context.getScheme() + "_" + context.getHost() + "_" + context.getPort(),
                key -> Mocker.api(instanceClass, context)
        );
    }

    public String checkBootRunning() {
        try (Socket ignored = new Socket(context.getHost(), context.getPort())) {
        } catch (IOException ioException) {
            return "connect fail -> " + context.getHost() + ":" + context.getPort();
        }
        return null;
    }

    public Object resolveField(Field field) {

        for (FieldResolver fieldResolver : this.fieldResolverList) {
            Object resolve = fieldResolver.resolve(field);
            if (resolve != null) return resolve;
        }

        return null;
    }

    public BootEnvironmentReader getBootwEnvironmentReader() {
        return bootwEnvironmentReader;
    }

}
