package com.maidada.mddpicturebackend.dto.user;

import com.maidada.mddpicturebackend.common.BasePageRequest;
import lombok.Data;

/**
 * [用户]列表请求
 *
 * @author wulinxuan
 * @date 2025-05-11 01:30
 */
@Data
public class UserListRequest extends BasePageRequest {

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
     * 用户角色：user/admin
     */
    private String userRole;
}
