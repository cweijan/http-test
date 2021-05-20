package io.github.cweijan.mock.feign;

import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.optionals.OptionalDecoder;
import io.github.cweijan.mock.feign.config.InternalConfig;
import io.github.cweijan.mock.openfeign.support.DefaultGzipDecoder;
import io.github.cweijan.mock.openfeign.support.ResponseEntityDecoder;
import io.github.cweijan.mock.openfeign.support.SpringDecoder;
import io.github.cweijan.mock.openfeign.support.SpringEncoder;
import io.github.cweijan.mock.util.JSON;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedList;

/**
 * @author cweijan
 * @since 2020/05/28 17:23
 */
public abstract class SpringCodecHolder {

    private static LinkedList<HttpMessageConverter<?>> httpMessageConverters;
    private static Encoder encoder;
    private static Decoder decoder;

    private static void init() {
        JSON.init(InternalConfig.PATTERN);
        httpMessageConverters = new LinkedList<>(new RestTemplate().getMessageConverters());
        for (HttpMessageConverter<?> httpMessageConverter : httpMessageConverters) {
            if (httpMessageConverter instanceof MappingJackson2HttpMessageConverter) {
                ((MappingJackson2HttpMessageConverter) httpMessageConverter).getObjectMapper()
                        .registerModule(JSON.getDateModule());
            }
        }
        ObjectFactory<HttpMessageConverters> httpMessageConvertersObjectFactory = () -> new HttpMessageConverters(false, httpMessageConverters);
        decoder = new OptionalDecoder(new ResponseEntityDecoder(
                new DefaultGzipDecoder(new SpringDecoder(httpMessageConvertersObjectFactory))
        ));
        encoder = new SpringEncoder(httpMessageConvertersObjectFactory);
    }

    public static synchronized Encoder getEncoder() {
        if (encoder == null) {
            init();
        }
        return encoder;
    }

    public static Decoder getDecoder() {
        return decoder;
    }

    public static void addHttpMessageConveter(HttpMessageConverter<?> httpMessageConverter) {
        httpMessageConverters.addFirst(httpMessageConverter);
    }

}
