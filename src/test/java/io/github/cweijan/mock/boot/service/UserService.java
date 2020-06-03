package io.github.cweijan.mock.boot.service;

import io.github.cweijan.mock.boot.pojo.User;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author cweijan
 * @since 2020/06/03 19:35
 */
@Service
public class UserService {

    private final List<User> userList = new ArrayList<>();

    public User getUser(Integer userId) {
        return userList.stream().findAny().filter(user -> user.getId().equals(userId)).orElse(null);
    }

    public void saveUser(User user) {
        userList.add(user);
    }

    public void updateUser(Integer userId, User updateUser) {
        BeanUtils.copyProperties(userList.stream().findAny().filter(user -> user.getId().equals(userId))
                .orElseThrow(() -> new RuntimeException("用户不存在!")), updateUser);
    }

    public void deleteUser(Integer userId) {
        userList.remove(getUser(userId));
    }

    public List<User> listUser() {
        return userList;
    }
}


