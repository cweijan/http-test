package io.github.cweijan.mock.request;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.RecursiveTask;

/**
 * @author cweijan
 * @since 2020/06/29 14:19
 */
public class Any {
    private static final Map<String, Object> methodMap = new HashMap<>();

    public static void put(StackTraceElement stackTraceElement) {
        methodMap.put(stackTraceElement.getMethodName(), true);
    }

    public static boolean get(String method) {
        Objects.requireNonNull(method);
        boolean usingAny = methodMap.containsKey(method);
        if(usingAny){
            methodMap.remove(method);
        }
        return usingAny;
    }

}
