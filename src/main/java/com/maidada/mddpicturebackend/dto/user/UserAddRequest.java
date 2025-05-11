package com.maidada.mddpicturebackend.dto.user;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * [用户]新增请求
 *
 * @author wulinxuan
 * @date 2025-05-11 01:30
 */
@Data
public class UserAddRequest {

    /**
     * 账号
     */
    @NotBlank(message = "账号不能为空")
    private String userAccount;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String userPassword;

    /**
     * 用户昵称
     */
    @NotBlank(message = "用户昵称不能为空")
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户角色：user/admin
     */
    @NotBlank(message = "用户角色不能为空")
    private String userRole;
}
