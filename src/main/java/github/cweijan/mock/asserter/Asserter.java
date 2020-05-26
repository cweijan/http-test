package github.cweijan.mock.asserter;

import github.cweijan.mock.util.JSON;
import org.json.JSONException;
import org.junit.jupiter.api.Assertions;
import org.skyscreamer.jsonassert.JSONAssert;

/**
 * @author cweijan
 * @since 2020/05/25 15:03
 */
public class Asserter extends Assertions{

    public static void assertSame(Object expected, Object actual) {
        assertSame(expected, actual, false);
    }

    /**
     * 对比两个对象的属性是否匹配
     *
     * @param expected 原始对象
     * @param actual   目标对象
     * @param strict   是否启用严格模式
     */
    public static void assertSame(Object expected, Object actual, boolean strict) {

        String expectedString = JSON.toJSON(expected);
        String actualString = JSON.toJSON(actual);

        try {
            JSONAssert.assertEquals(expectedString, actualString, strict);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

    }

    public static void assertNotSame(Object expected, Object actual) {
        assertNotSame(expected, actual, false);
    }

    /**
     * 对比两个对象的属性是否匹配
     *
     * @param expected 原始对象
     * @param actual   目标对象
     * @param strict   是否启用严格模式
     */
    public static void assertNotSame(Object expected, Object actual, boolean strict) {

        String expectedString = JSON.toJSON(expected);
        String actualString = JSON.toJSON(actual);

        try {
            JSONAssert.assertNotEquals(expectedString, actualString, strict);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

    }

}
