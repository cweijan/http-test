package io.github.cweijan.mock;

import io.github.cweijan.mock.feign.config.InternalConfig;

/**
 * @author cweijan
 * @since 2020/06/29 15:02
 */
public class MockerConfig {
    /**
     * 设置日期Date和LocalDateTime的序列化格式, 默认为"yyyy-MM-dd HH:mm:ss"
     *
     * @param patten 日期格式
     */
    public static void configDateFormat(String patten) {
        InternalConfig.PATTERN = patten;
    }

    /**
     * 是否打印美化返回体
     * @param pretty 是否启用
     */
    public static void prettyResponse(boolean pretty){
        InternalConfig.PRETTY_RESPONSE = pretty;
    }

    /**
     * 是否打印美化请求体
     * @param pretty 是否启用
     */
    public static void prettyRequest(boolean pretty){
        InternalConfig.PRETTY_REQUEST = pretty;
    }

}
