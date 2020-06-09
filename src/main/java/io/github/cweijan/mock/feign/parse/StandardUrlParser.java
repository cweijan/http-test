package io.github.cweijan.mock.feign.parse;

import io.github.cweijan.mock.context.HttpMockContext;

import java.util.ArrayList;
import java.util.List;

/**
 * @author cweijan
 * @since 2020/05/28 16:43
 */
public class StandardUrlParser extends AbstractParser {

    private final List<UrlParser> parserList;

    public StandardUrlParser() {
        this.parserList = new ArrayList<>(3);
        parserList.add(new RequestMappingParser());
        parserList.add(new FeignParser());
    }

    @Override
    public boolean supportParse(Class<?> controllerClass) {
        return true;
    }

    @Override
    public String parse(HttpMockContext httpMockContext, Class<?> controllerClass) {

        for (UrlParser urlParser : parserList) {
            if (urlParser.supportParse(controllerClass)) {
                return urlParser.parse(httpMockContext, controllerClass);
            }
        }

        return super.parse(httpMockContext, controllerClass);
    }

    @Override
    protected String getPath(HttpMockContext httpMockContext, Class<?> controllerClass) {
        return "/";
    }

}
