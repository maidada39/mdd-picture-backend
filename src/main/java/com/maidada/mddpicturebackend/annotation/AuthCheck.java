package com.maidada.mddpicturebackend.annotation;

import com.maidada.mddpicturebackend.enums.UserRoleEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 身份验证检查
 *
 * @author wulinxuan
 * @date 2025/05/11 18:43
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthCheck {

    /**
     * 必须有的角色
     */
    UserRoleEnum mustRole();
}
