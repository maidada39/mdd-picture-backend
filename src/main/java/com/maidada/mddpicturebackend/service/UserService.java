package com.maidada.mddpicturebackend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.maidada.mddpicturebackend.common.BaseRequest;
import com.maidada.mddpicturebackend.dto.user.*;
import com.maidada.mddpicturebackend.entity.User;
import com.maidada.mddpicturebackend.vo.UserDetailVO;
import com.maidada.mddpicturebackend.vo.UserListVO;
import com.maidada.mddpicturebackend.vo.UserLoginVO;

import javax.validation.Valid;

/**
 * [用户]服务接口
 *
 * @author wulinxuan
 * @date 2025-05-11 01:30
 */
public interface UserService extends IService<User> {

    long userRegister(UserRegisterRequest param);

    UserLoginVO userLogin(@Valid UserLoginRequest param);

    UserLoginVO getLoginUser();

    boolean logoutUser();

    void add(UserAddRequest param);

    void delete(BaseRequest param);

    void update(UserUpdateRequest param);

    UserDetailVO detail(BaseRequest param);

    IPage<UserListVO> list(UserListRequest param);
}
