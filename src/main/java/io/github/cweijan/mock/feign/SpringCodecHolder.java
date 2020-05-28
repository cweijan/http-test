package io.github.cweijan.mock.feign;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.codec.Decoder;
import feign.codec.Encoder;
import io.github.cweijan.mock.util.JSON;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.PageableSpringEncoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.http.converter.xml.SourceHttpMessageConverter;

import java.util.LinkedList;

/**
 * @author cweijan
 * @since 2020/05/28 17:23
 */
public class SpringCodecHolder {

    private static final LinkedList<HttpMessageConverter<?>> httpMessageConverters;
    private static final ObjectFactory<HttpMessageConverters> httpMessageConvertersObjectFactory;
    private static Encoder encoder;
    private static final Decoder decoder;

    static {
        httpMessageConverters = new LinkedList<>();
        httpMessageConverters.add(new ByteArrayHttpMessageConverter());
        httpMessageConverters.add(new StringHttpMessageConverter());
        httpMessageConverters.add(new ResourceHttpMessageConverter());
        httpMessageConverters.add(new SourceHttpMessageConverter<>());
        httpMessageConverters.add(initJacksonConverter());
        httpMessageConverters.add(new Jaxb2RootElementHttpMessageConverter());
        httpMessageConvertersObjectFactory = () -> new HttpMessageConverters(httpMessageConverters);
        decoder = new SpringDecoder(httpMessageConvertersObjectFactory);
        encoder = new SpringEncoder(httpMessageConvertersObjectFactory);
        try {
            Class.forName("org.springframework.data.domain.Pageable");
            encoder = new PageableSpringEncoder(encoder);
        } catch (ClassNotFoundException ignored) {
        }
    }

    public static Encoder getEncoder() {
        return encoder;
    }

    public static Decoder getDecoder() {
        return decoder;
    }

    public static void addHttpMessageConveter(HttpMessageConverter<?> httpMessageConverter) {
        httpMessageConverters.addFirst(httpMessageConverter);
    }

    private static MappingJackson2HttpMessageConverter initJacksonConverter() {
        MappingJackson2HttpMessageConverter jackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
        ObjectMapper objectMapper = jackson2HttpMessageConverter.getObjectMapper();
        objectMapper.registerModule(JSON.getDateModule());
        return jackson2HttpMessageConverter;
    }

}
