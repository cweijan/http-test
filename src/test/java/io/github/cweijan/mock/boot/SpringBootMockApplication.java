package io.github.cweijan.mock.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;

/**
 * @author cweijan
 * @since 2020/06/03 19:31
 */
@SpringBootApplication(exclude = {FeignAutoConfiguration.class})
public class SpringBootMockApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootMockApplication.class,args);
    }


}
