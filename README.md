# 目录树

```
├─src
│  ├─main
│  │  ├─java
│  │  │  └─website
│  │  │      ├─config                    //Spring Security相关配置类
│  │  │      ├─controller                //业务接口
│  │  │      ├─detailsservice            //登陆验证
│  │  │      ├─mapper                    //MyBatis-Plus相关接口
│  │  │      └─pojo                      //业务相关类
│  │  └─resources
│  │      ├─static
│  │      ├─templates
│  │      └─application.yaml             //Spring Boot等框架相关参数设置
│  └─test
│      └─java
│          └─website
│              └─ApplicationTests.java   //测试文件（没用）
└─pom.xml                                //Maven依赖配置
```

# 缺陷

查询朋友列表时效率低下

删除评论的方式并非逻辑删除

登录验证时密码传递过程中不加密（明文传递）

上传视频效率极低

（其他不一一列出）