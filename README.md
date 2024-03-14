# 完成情况

按照给定的接口文档完成17个接口的功能，其中登录功能由于使用安全框架配置，故未在Controller中显式声明接口。

# 接口文档链接

[https://](https://apifox.com/apidoc/shared-ac6f6959-ff47-4bdd-a1c4-f35ae44d9c04)


# 目录树

```
├─src
│  ├─main
│  │  ├─java
│  │  │  └─website
│  │  │      ├─config                    //Spring Security相关配置类
│  │  │      ├─controller                //业务接口
│  │  │      ├─detailsservice            //登录验证接口
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
│
├─Dockerfile                             //Docker构建文件
├─pom.xml                                //Maven依赖配置
└─user-0.0.1-SNAPSHOT.jar                //Maven打包后生成的jar包
```

# 缺陷

查询好友列表时效率低下

删除评论的方式并非逻辑删除

上传视频奇慢

同一账号可多次登录，新账号登录会取代旧帐号。

部分接口必须传入登录成功后返回的token。且若某次传入错误的token，则必须重新登录后使用新的token才能获得预期结果。

点赞/取消点赞操作前不会检查用户是否已经取消点赞/点赞。

缺少“增加点击量”和“退出账号”的接口。

（其他不一一列出）
