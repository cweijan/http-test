# HTTP-Test

该项目用于帮助Java开发人员快速测试SpringMvc接口

# 快速开始
1. 引入依赖
    - maven:
    ```xml
    <dependency>
        <groupId>io.github.cweijan</groupId>
        <artifactId>http-test</artifactId>
        <version>0.0.1</version>
        <scope>test</scope>
    </dependency>
    ```
    - gradle:
    ```groovy
    testCompile 'io.github.cweijan:http-test:0.0.1'
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
3. 创建测试用例, 开始测试
    ```java
    import io.github.cweijan.mock.asserter.Asserter;
    import io.github.cweijan.mock.jupiter.HttpTest;
    import io.github.cweijan.mock.request.Generator;
    import org.junit.jupiter.api.Test;
    
    import javax.annotation.Resource;
    
    @HttpTest(host = "localhost",port = 8080)
    public class UserControllerTest {
    
        @Resource
        private UserController userController;
    
        @Test
        void testAddToken(){
           //该拦截器对所有请求都生效
           Mocker.apiInterceptor((RequestTemplate requestTemplate) -> {
               requestTemplate.header("token","testToken");
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
   
# 实现原理
1. 扫描controller并使用ByteBuddy动态创建feign接口
2. 根据接口创建feign
3. 构建controller代理, 执行方法时实际调用feign