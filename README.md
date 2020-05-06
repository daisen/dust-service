# dust-service

## 简介
基于Spring Boot的服务开发组件，用于提高开发效率，降低重复工作

## 依赖

- Spring Boot 2.0
- Spring Cloud Finchley
- dust-commons
- dust-db

## 功能

1. 请求验证和数据验证

2. 数据访问模块封装，不依赖于SpringJPA

3. 常用的类型转换

4. 请求报文的格式化

5. 请求统一的异常处理

6. 统一的异步处理单元

7. 基本的SpringStream对接

## 使用

通过Maven的Pom文件进行引入。

```$xml
<dependency>
    <groupId>com.gitee.gtman</groupId>
    <artifactId>dust-service</artifactId>    
    <version>2.0-SNAPSHOT</version>
</dependency>

```

## 说明

请求服务的返回结构：
```json
{
  "status": "200",
  "data": {},
  "message": "",
  "error": "",
  "exception": ""  
}
```

status:
* 200 成功
* 5000 服务器异常，未经处理
* 5001 参数不符合要求
* 6001 业务异常
* 6100 数据库错误
* 6200 Dust组件错误

messge: 可读的提示信息  
error：错误分类  
exception: 部分堆栈信息

## License

[MIT](./LICENSE)

