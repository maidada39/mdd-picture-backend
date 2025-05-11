package com.maidada.mddpicturebackend.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 用户角色枚举
 *
 * @author wulinxuan
 * @date 2025/05/11 16:59
 */
@Getter
public enum UserRoleEnum {

    /**
     * 枚举
     */
    USER("user", "普通用户"),
    ADMIN("admin", "管理员");

    private final String value;

    private final String text;

    private static final Map<String, UserRoleEnum> cache;

    UserRoleEnum(String value, String text) {
        this.value = value;
        this.text = text;
    }

    static {
        cache = Arrays.stream(UserRoleEnum.values()).collect(Collectors.toMap(UserRoleEnum::getValue, Function.identity()));
    }

    public static UserRoleEnum of(String value) {
        return cache.get(value);
    }

//    public static UserRoleEnum of(String value) {
//        for (UserRoleEnum roleEnum : UserRoleEnum.values()) {
//            if (roleEnum.value.equals(value)) {
//                return roleEnum;
//            }
//        }
//        return null;
//    }
}
