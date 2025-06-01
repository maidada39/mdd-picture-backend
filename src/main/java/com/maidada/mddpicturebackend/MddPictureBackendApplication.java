package com.maidada.mddpicturebackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.aop.framework.AopContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
public class MddPictureBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(MddPictureBackendApplication.class, args);
    }

}
