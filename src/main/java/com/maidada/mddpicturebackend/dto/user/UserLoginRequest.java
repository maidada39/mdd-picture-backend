package com.maidada.mddpicturebackend.dto.user;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * [用户]登录请求
 *
 * @author wulinxuan
 * @date 2025-05-11 01:30
 */
@Data
public class UserLoginRequest {

    /**
     * 账号
     */
    @NotBlank(message = "账号不能为空")
    @Size(min = 4, max = 16, message = "账号长度在4到16位之间")
    private String userAccount;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 16, message = "密码长度在6到16位之间")
    private String userPassword;

}
