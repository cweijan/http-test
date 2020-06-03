package io.github.cweijan.mock.boot.controller;

import io.github.cweijan.mock.boot.pojo.User;
import io.github.cweijan.mock.boot.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author cweijan
 * @since 2020/06/03 19:34
 */
@RestController
@RequestMapping("/user")
public class UserMockController {

    private final UserService userService;

    public UserMockController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/list")
    public List<User> list() {
        return userService.listUser();
    }

    @GetMapping("/{userId}")
    public User getUser(@PathVariable Integer userId) {

        return userService.getUser(userId);
    }

    @GetMapping("/get")
    public User getUserByParam(Integer userId) {
        return userService.getUser(userId);
    }

    @PostMapping("/save")
    public void saveUser(@RequestBody User user) {
        userService.saveUser(user);
    }

    @PutMapping("/{userId}")
    public void updateUser(@RequestBody User user, @PathVariable Integer userId) {
        userService.updateUser(userId, user);
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable Integer userId) {
        userService.deleteUser(userId);
    }

}
