package com.maidada.mddpicturebackend.vo;

import lombok.Data;

import java.util.Date;

/**
 * [用户]登录视图
 *
 * @author wulinxuan
 * @date 2025-05-11 01:30
 */
@Data
public class UserLoginVO {

    /**
     * id
     */
    private Long id;

    /**
     * 账号
     */
    private String userAccount;

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

    /**
     * 编辑时间
     */
    private Date editTime;

    /**
     * 创建时间
     */
    private Date createTime;
}
