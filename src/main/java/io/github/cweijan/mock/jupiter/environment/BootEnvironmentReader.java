package io.github.cweijan.mock.jupiter.environment;

import io.github.cweijan.mock.jupiter.HttpTest;
import org.springframework.boot.context.config.ConfigFileApplicationListener;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.util.HashMap;
import java.util.Map;

/**
 * @author cweijan
 * @since 2020/06/03 22:36
 */
public class BootEnvironmentReader extends ConfigFileApplicationListener implements HttpMockContextReader {

    private static final String BOOTSTRAP_PROPERTY_SOURCE_NAME = "bootstrap";

    public ConfigurableEnvironment environment;


    public BootEnvironmentReader(HttpTest httpTest) {
        this.environment = new StandardEnvironment();
        environment.setActiveProfiles(httpTest.activeProfiles());
        ConfigurationPropertySources.attach(environment);
        this.addPropertySources(environment, null);
        this.environment.merge(getParentEnvironment(httpTest));
    }

    private StandardEnvironment getParentEnvironment(HttpTest httpTest) {
        StandardEnvironment parentEnvironment = new StandardEnvironment();
        parentEnvironment.setActiveProfiles(httpTest.activeProfiles());
        Map<String, Object> bootstrapMap = new HashMap<>();
        bootstrapMap.put("spring.config.name", BOOTSTRAP_PROPERTY_SOURCE_NAME);
        parentEnvironment.getPropertySources().addFirst(new MapPropertySource(BOOTSTRAP_PROPERTY_SOURCE_NAME, bootstrapMap));
        this.addPropertySources(parentEnvironment, null);
        return parentEnvironment;
    }

    /**
     * 读取spring boot环境变量
     *
     * @param key 变量名
     * @return 变量值
     */
    public String get(String key) {
        return environment.getProperty(key);
    }

    @Override
    public String getHost() {
        return environment.getProperty("server.address");
    }

    @Override
    public String getContextPath() {
        return environment.getProperty("server.servlet.context-path");
    }

    @Override
    public Integer getPort() {
        String property = environment.getProperty("server.port");
        if (property == null) return null;
        return Integer.parseInt(property);
    }

    @Override
    public String getScheme() {
        return null;
    }
}
