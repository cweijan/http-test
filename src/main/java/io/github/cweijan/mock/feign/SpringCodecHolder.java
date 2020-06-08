package io.github.cweijan.mock.feign;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.codec.Decoder;
import feign.codec.Encoder;
import io.github.cweijan.mock.feign.config.DateConfig;
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
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.http.converter.xml.SourceHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedList;
import java.util.List;

/**
 * @author cweijan
 * @since 2020/05/28 17:23
 */
public class SpringCodecHolder {

    private static  LinkedList<HttpMessageConverter<?>> httpMessageConverters;
    private static  ObjectFactory<HttpMessageConverters> httpMessageConvertersObjectFactory;
    private static Encoder encoder;
    private static Decoder decoder;

    private static void init() {
        JSON.init(DateConfig.PATTERN);
        httpMessageConverters = new LinkedList<>(new RestTemplate().getMessageConverters());
        for (HttpMessageConverter<?> httpMessageConverter : httpMessageConverters) {
            if (httpMessageConverter instanceof MappingJackson2HttpMessageConverter) {
                ((MappingJackson2HttpMessageConverter) httpMessageConverter).getObjectMapper()
                        .registerModule(JSON.getDateModule());
            }
        }
        httpMessageConvertersObjectFactory = () -> new HttpMessageConverters(false, httpMessageConverters);
        decoder = new SpringDecoder(httpMessageConvertersObjectFactory);
        encoder = new SpringEncoder(httpMessageConvertersObjectFactory);
        try {
            Class.forName("org.springframework.data.domain.Pageable");
            encoder = new PageableSpringEncoder(encoder);
        } catch (ClassNotFoundException ignored) {
        }
    }

    public static synchronized Encoder getEncoder() {
        if(encoder==null){
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

    private static MappingJackson2HttpMessageConverter initJacksonConverter() {
        MappingJackson2HttpMessageConverter jackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
        ObjectMapper objectMapper = jackson2HttpMessageConverter.getObjectMapper();
        objectMapper.registerModule(JSON.getDateModule());
        return jackson2HttpMessageConverter;
    }

}
