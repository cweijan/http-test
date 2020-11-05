# HTTP-Test

该项目用于帮助Java开发人员快速测试SpringMvc接口

- [HTTP-Test](#http-test)
- [快速开始](#快速开始)
- [Why Junit5?](#why-junit5)
- [常用API](#常用api)
- [实现原理](#实现原理)

# 快速开始
1. 安装IDEA插件
- 从插件仓库[http-test](https://plugins.jetbrains.com/plugin/15316-http-test-support)
- 直接下载[jar包](https://github.com/cweijan/http-test-idea/releases/download/1.1.1/http-test-idea.jar)安装

2. 假设有以下controller, 启动springboot应用
    ```java
    @RestController
    @RequestMapping("/user")
    public class UserController {
    
        private final UserService userService;
    
        public UserController(UserService userService) {
            this.userService = userService;
        }
    
        @PostMapping("/save")
        public UserVo saveUser(@RequestBody SaveUserDTO saveuserDTO) {
            return userService.saveUser(saveuserDTO);
        }
    
        @PostMapping("/update")
        public UserVo updateUser(@RequestBody UpdateUserDTO updateuserDTO) {
            return userService.updateUser(updateuserDTO);
        }
      
        @GetMapping("/{userId}")
        public UserVO getUser(@PathVariable Integer userId) {
            return userService.getUser(userId);
        }
    
        @DeleteMapping("/{userId}")
        public void deleteByUserId(@PathVariable Integer userId) {
            userService.deleteByUserId(userId);
        }
    
    }
    ```
3.  在Controller类上面按下alt+enter, 点击**创建Http测试用例**, 首次使用时会自动**安装依赖**

![example](example.gif)

# 常用API
- Generator: 用于生成mock数据
- Asserter: 用于对数据进行断言验证
- Mocker: 可修改默认设置

# 实现原理
1. 扫描controller并使用ByteBuddy动态创建feign接口
2. 根据接口创建feign
3. 构建controller代理, 执行方法时实际调用feign
