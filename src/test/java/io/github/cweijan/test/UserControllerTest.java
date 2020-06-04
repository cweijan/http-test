package io.github.cweijan.test;

import io.github.cweijan.mock.Asserter;
import io.github.cweijan.mock.boot.controller.UserMockController;
import io.github.cweijan.mock.boot.pojo.User;
import io.github.cweijan.mock.jupiter.HttpTest;
import io.github.cweijan.mock.request.Generator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.Resource;

/**
 * @author cweijan
 * @since 2020/06/03 19:45
 */
@HttpTest
public class UserControllerTest {

    @Resource
    private UserMockController userController;

    @Value("${server.port}")
    private Integer port;

    @Test
    void saveUser() {

        User user = Generator.request(User.class);
        Integer userId = user.getId();
        userController.saveUser(user);

        User savedUser = userController.getUser(userId);
        Asserter.assertSame(user,savedUser);

        userController.deleteUser(userId);

        User nowUser = userController.getUser(userId);
        Asserter.assertNull(nowUser);

    }


}
