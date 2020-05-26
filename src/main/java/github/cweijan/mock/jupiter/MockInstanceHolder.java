package github.cweijan.mock.jupiter;

import github.cweijan.mock.Mocker;
import github.cweijan.mock.context.HttpMockContext;

import java.util.HashMap;
import java.util.Map;

/**
 * @author cweijan
 * @since 2020/05/25 18:17
 */
public class MockInstanceHolder {

    private final HttpMockContext context;
    private final Map<String,Object> instanceMap;

    public MockInstanceHolder(String scheme, String host, Integer port) {
        this.context =new HttpMockContext(scheme, host, port);
        this.instanceMap=new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    public <T> T getInstance(Class<T> instanceClass){

        return (T) instanceMap.computeIfAbsent(
                instanceClass.getSimpleName()+"_"+ context.getScheme()+"_"+context.getHost()+"_"+context.getPort(),
                key -> Mocker.api(instanceClass, context)
        );
    }

}
