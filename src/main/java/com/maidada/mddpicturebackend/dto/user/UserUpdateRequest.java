package com.maidada.mddpicturebackend.dto.user;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * [用户]更新请求
 *
 * @author wulinxuan
 * @date 2025-05-11 01:30
 */
@Data
public class UserUpdateRequest {

    /**
     * id
     */
    @NotNull(message = "id不能为空")
    private Long id;

    /**
     * 密码
     */
    private String userPassword;

    /**
     * 用户昵称
     */
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
    private String userRole;
}
