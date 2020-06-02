package io.github.cweijan.mock.feign;

import feign.Client;
import feign.Request;
import feign.Response;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StreamUtils;
import sun.misc.Unsafe;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

/**
 * @author cweijan
 * @since 2020/06/02 14:32
 */
public class AutoOutputClient extends Client.Default {

    private static final Unsafe unsafe = BeanUtils.instantiateClass(Unsafe.class);

    public AutoOutputClient(SSLSocketFactory sslContextFactory, HostnameVerifier hostnameVerifier) {
        super(sslContextFactory, hostnameVerifier);
    }

    @Override
    public Response execute(Request request, Request.Options options) throws IOException {
        Response response = super.execute(request, options);
        ouputResponse(response);
        return response;
    }

    private void ouputResponse(Response response) throws IOException {

        Response.Body body = response.body();
        Field field = null;
        try {
            field = body.getClass().getDeclaredField("inputStream");
        } catch (NoSuchFieldException e) {
            unsafe.throwException(e);
        }


        byte[] bytes = StreamUtils.copyToByteArray(body.asInputStream());
        System.out.println("Response Body: " + new String(bytes, StandardCharsets.UTF_8));
        System.out.println("-----------------------------------------------------------------------------------------------------------------------");

        unsafe.putObject(body,unsafe.objectFieldOffset(field),new ByteArrayInputStream(bytes));

    }
}
