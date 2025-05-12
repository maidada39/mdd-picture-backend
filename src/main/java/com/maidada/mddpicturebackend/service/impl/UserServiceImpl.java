package com.maidada.mddpicturebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.maidada.mddpicturebackend.common.BaseRequest;
import com.maidada.mddpicturebackend.dto.user.*;
import com.maidada.mddpicturebackend.enums.UserRoleEnum;
import com.maidada.mddpicturebackend.exception.ErrorCode;
import com.maidada.mddpicturebackend.exception.ThrowUtils;
import com.maidada.mddpicturebackend.util.SpringContextUtils;
import com.maidada.mddpicturebackend.vo.user.UserLoginVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maidada.mddpicturebackend.mapper.UserMapper;
import com.maidada.mddpicturebackend.entity.User;
import com.maidada.mddpicturebackend.vo.user.UserDetailVO;
import com.maidada.mddpicturebackend.vo.user.UserListVO;
import com.maidada.mddpicturebackend.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * [用户]服务实现类
 * todo 大致实现功能即可，不是来学写这块逻辑的
 *
 * @author wulinxuan
 * @date 2025-05-11 01:30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private static final String SALT = "sijidazao";

    private final UserMapper userMapper;

    @Override
    public long userRegister(UserRegisterRequest param) {
        String userAccount = param.getUserAccount();
        String userPassword = param.getUserPassword();

        // 账号是否存在
        Long count = this.lambdaQuery().eq(User::getUserAccount, userAccount).count();
        ThrowUtils.throwIf(count > 0, ErrorCode.PARAMS_ERROR, "账号已存在");

        // 保存用户
        User entity = new User();

        String encryptedPassword = getEncryptedPassword(userPassword);
        entity.setUserAccount(userAccount);
        entity.setUserPassword(encryptedPassword);
        entity.setUserName("新用户");
        entity.setUserRole(UserRoleEnum.USER.getValue());
        boolean saveResult = this.save(entity);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");

        return entity.getId();
    }

    @Override
    public UserLoginVO userLogin(UserLoginRequest param) {
        String userAccount = param.getUserAccount();
        String userPassword = param.getUserPassword();

        // 查询账号是否存在
        User entity = this.lambdaQuery().eq(User::getUserAccount, userAccount).one();
        ThrowUtils.throwIf(Objects.isNull(entity), ErrorCode.PARAMS_ERROR, "账号不存在");

        // 密码校验
        String encryptedPassword = getEncryptedPassword(userPassword);
        ThrowUtils.throwIf(!encryptedPassword.equals(entity.getUserPassword()), ErrorCode.PARAMS_ERROR, "密码错误");

        // 保存登录态
        HttpServletRequest request = SpringContextUtils.getHttpServletRequest();
        UserLoginVO result = getLoginUserVO(entity);
        request.getSession().setAttribute("user_login_state", entity);

        // 返回用户信息
        return result;
    }

    @Override
    public UserLoginVO getLoginUser() {
        // 获取session中用户
        HttpServletRequest request = SpringContextUtils.getHttpServletRequest();
        User loginUser = (User) request.getSession().getAttribute("user_login_state");
        ThrowUtils.throwIf(Objects.isNull(loginUser), ErrorCode.NOT_LOGIN_ERROR);

        // 从数据库中查询，不追求性能的情况下
        Long id = loginUser.getId();
        User user = this.getById(id);
        // 可能存在session中存在，但用户呗删除的情况
        ThrowUtils.throwIf(Objects.isNull(user), ErrorCode.NOT_LOGIN_ERROR);

        return getLoginUserVO(user);
    }

    @Override
    public boolean logoutUser() {
        HttpServletRequest request = SpringContextUtils.getHttpServletRequest();
        // 判断是否登录
        User loginUser = (User) request.getSession().getAttribute("user_login_state");
        ThrowUtils.throwIf(Objects.isNull(loginUser), ErrorCode.NOT_LOGIN_ERROR);

        // 移除登录态
        request.getSession().removeAttribute("user_login_state");

        return true;
    }

    @Override
    public void add(UserAddRequest param) {
        // 校验
        UserRoleEnum userRoleEnum = UserRoleEnum.of(param.getUserRole());
        ThrowUtils.throwIf(Objects.isNull(userRoleEnum), ErrorCode.PARAMS_ERROR, "用户角色不存在");

        User entity = new User();
        BeanUtils.copyProperties(param, entity);
        entity.setUserPassword(getEncryptedPassword(param.getUserPassword()));

        save(entity);
    }

    @Override
    public void delete(BaseRequest param) {
        removeById(param.getId());
    }

    @Override
    public void update(UserUpdateRequest param) {
        User entity = getById(param.getId());
        ThrowUtils.throwIf(Objects.isNull(entity), ErrorCode.PARAMS_ERROR, "数据不存在");

        BeanUtils.copyProperties(param, entity);
    
        updateById(entity);
    }

    @Override
    public UserDetailVO detail(BaseRequest param) {
        User entity = getById(param.getId());
        ThrowUtils.throwIf(Objects.isNull(entity), ErrorCode.PARAMS_ERROR, "数据不存在");
    
        UserDetailVO result = new UserDetailVO();
        BeanUtils.copyProperties(entity, result);
    
        return result;
    }

    @Override
    public IPage<UserListVO> list(UserListRequest param) {
        IPage<User> page = new Page<>(param.getPageNo(), param.getPageSize());

        LambdaQueryWrapper<User> wrapper = Wrappers.lambdaQuery();

        IPage<User> pageResult = page(page, wrapper);

        return pageResult.convert(item -> {
            UserListVO vo = new UserListVO();
            BeanUtils.copyProperties(item, vo);
            return vo;
        });
    }

    /**
     * 获取加密密码
     *
     * @param password 密码
     * @return {@link String }
     */
    private String getEncryptedPassword(String password) {
        return DigestUtils.md5DigestAsHex((SALT + password).getBytes());
    }

    /**
     * 获取登录用户vo
     *
     * @param user 用户
     * @return {@link UserLoginVO }
     */
    private UserLoginVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserLoginVO result = new UserLoginVO();
        BeanUtils.copyProperties(user, result);
        return result;
    }
}
