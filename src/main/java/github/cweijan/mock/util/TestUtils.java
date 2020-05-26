package github.cweijan.mock.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestUtils {

    private static final Logger logger = LoggerFactory.getLogger(TestUtils.class);

    public interface TestUnit {
        void test() throws Exception;
    }

    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();


    public static void test(TestUnit testUnit) {
        begin();
        try {
            testUnit.test();
        } catch (Exception e) {
            e.printStackTrace();
        }
        end();
    }

    public static void loopTest(TestUnit testUnit, int count) {
        for (int i = 0; i < count; i++) {
            try {
                testUnit.test();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void begin() {
        threadLocal.set(System.currentTimeMillis());
    }

    public static void end() {

        logger.debug("time:" + (System.currentTimeMillis() - threadLocal.get()) + " millSecond");
        threadLocal.remove();

    }

}
