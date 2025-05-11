package com.maidada.mddpicturebackend.controller;

import javax.validation.Valid;

import com.maidada.mddpicturebackend.annotation.AuthCheck;
import com.maidada.mddpicturebackend.common.BaseRequest;
import com.maidada.mddpicturebackend.common.BaseResponse;
import com.maidada.mddpicturebackend.common.ResultUtils;
import com.maidada.mddpicturebackend.dto.user.*;
import com.maidada.mddpicturebackend.enums.UserRoleEnum;
import com.maidada.mddpicturebackend.vo.UserLoginVO;
import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.maidada.mddpicturebackend.service.UserService;
import com.maidada.mddpicturebackend.vo.UserDetailVO;
import com.maidada.mddpicturebackend.vo.UserListVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * [用户]控制层
 *
 * @author wulinxuan
 * @date 2025-05-11 01:30
 */
@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public BaseResponse<Long> registerUser(@Valid @RequestBody UserRegisterRequest param) {
        long result = userService.userRegister(param);
        return ResultUtils.success(result);
    }

    @PostMapping("/login")
    public BaseResponse<UserLoginVO> loginUser(@Valid @RequestBody UserLoginRequest param) {
        UserLoginVO result = userService.userLogin(param);
        return ResultUtils.success(result);
    }

    @GetMapping("/get/login")
    public BaseResponse<UserLoginVO> getLoginUser() {
        UserLoginVO result = userService.getLoginUser();
        return ResultUtils.success(result);
    }

    @GetMapping("/logout")
    public BaseResponse<Boolean> logoutUser() {
        Boolean result = userService.logoutUser();
        return ResultUtils.success(result);
    }

    @AuthCheck(mustRole = UserRoleEnum.ADMIN)
    @PostMapping("/add")
    public BaseResponse<String> addUser(@Valid @RequestBody UserAddRequest param) {
        userService.add(param);
        return ResultUtils.success("新增成功");
    }

    @AuthCheck(mustRole = UserRoleEnum.ADMIN)
    @PostMapping("/delete")
    public BaseResponse<String> deleteUser(@Valid @RequestBody BaseRequest param) {
        userService.delete(param);
        return ResultUtils.success("删除成功");
    }

    @AuthCheck(mustRole = UserRoleEnum.ADMIN)
    @PostMapping("/update")
    public BaseResponse<String> updateUser(@Valid @RequestBody UserUpdateRequest param) {
        userService.update(param);
        return ResultUtils.success("更新成功");
    }

    @GetMapping("/detail")
    public BaseResponse<UserDetailVO> userDetail(@Valid BaseRequest param) {
        UserDetailVO result = userService.detail(param);
        return ResultUtils.success(result);
    }

    @AuthCheck(mustRole = UserRoleEnum.ADMIN)
    @GetMapping("/list")
    public BaseResponse<IPage<UserListVO>> listUser(UserListRequest param) {
        IPage<UserListVO> result = userService.list(param);
        return ResultUtils.success(result);
    }
}
