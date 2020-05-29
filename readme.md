# HTTP-Test

该项目用于帮助Java开发人员快速测试SpringMvc接口

- [HTTP-Test](#http-test)
- [快速开始](#快速开始)
- [Why Junit5?](#why-junit5)
- [常用API](#常用api)
- [实现原理](#实现原理)

# 快速开始
1. 引入依赖
    - maven:
    ```xml
    <dependency>
        <groupId>io.github.cweijan</groupId>
        <artifactId>http-test</artifactId>
        <version>0.0.3</version>
        <scope>test</scope>
    </dependency>
    ```
    - gradle:
    ```groovy
    testCompile 'io.github.cweijan:http-test:0.0.3'
    ```
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
3. 创建Junit5测试用例(IDEA可通过generate菜单快速生成), 开始测试
    ```java
    import io.github.cweijan.mock.Asserter;
    import io.github.cweijan.mock.jupiter.HttpTest;
    import io.github.cweijan.mock.request.Generator;
    import org.junit.jupiter.api.Test;
    import org.junit.jupiter.api.BeforeAll;
    
    import javax.annotation.Resource;
    
    @HttpTest(host = "localhost",port = 8080)
    public class UserControllerTest {
    
        //仅支持注入controller和feignClient
        @Resource
        private UserController userController;
    
        @BeforeAll
        public static void addToken(){
            //配置全局拦截器
            Mocker.addRequestInterceptor(template -> {
                template.header("token","c2f678d4873c472c8f99940e8cf39fe4");
            });
        }
   
        // 注意, 必须使用junit5: org.junit.jupiter.api.Test
        @Test
        void saveUser() {
    
            //创建mock数据
            SaveUserDTO saveUserDTO = Generator.request(SaveUserDTO.class);
            // 当调用方法时会直接发送http请求
            UserVo userVo=userController.saveUser(saveUserDTO);
    
            UserVO user = userController.getUser(userVo.getId());
    
            Asserter.assertSame(userVo,user);
    
            userController.deleteByUserId(userVo.getId());
            
        }       
    
    }
    ```
# Why Junit5?
该工具必须使用Junit5, 可能会让你困惑于junit4和junit5的异同, 这里简单介绍下
1. Junit5提供了统一接口方便用于扩展测试, 而junit4想扩展必须管理所有测试的生命周期
2. Junit5许多有用的新功能, 比如参数化测试、重复测试, 详情可查阅[文档](https://junit.org/junit5/docs/current/user-guide/#writing-tests)
3. spring-boot-test自2.2.0(2019/10/18)后默认使用junit5, 毫无疑问junit5将逐步取代junit4


# 常用API
- Generator: 用于生成mock数据
- Asserter: 用于对数据进行断言验证
- Mocker: 用于创建controller代理以及设置拦截器

# 实现原理
1. 扫描controller并使用ByteBuddy动态创建feign接口
2. 根据接口创建feign
3. 构建controller代理, 执行方法时实际调用feign