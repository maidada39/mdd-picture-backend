package com.maidada.mddpicturebackend.aop;

import com.maidada.mddpicturebackend.annotation.AuthCheck;
import com.maidada.mddpicturebackend.enums.UserRoleEnum;
import com.maidada.mddpicturebackend.exception.ErrorCode;
import com.maidada.mddpicturebackend.exception.ThrowUtils;
import com.maidada.mddpicturebackend.service.UserService;
import com.maidada.mddpicturebackend.vo.UserLoginVO;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author wulinxuan
 * @date 2025/5/11 18:43
 */
@Aspect
@Component
public class AuthInterceptor {

    @Resource
    private UserService userService;

    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        // 获取需要的权限和当前用户
        UserRoleEnum mustRole = authCheck.mustRole();
        UserLoginVO loginUser = userService.getLoginUser();
        UserRoleEnum userRole = UserRoleEnum.of(loginUser.getUserRole());

        // 用户无权限，抛出异常
        ThrowUtils.throwIf(userRole == null, ErrorCode.NO_AUTH_ERROR);
        // 必须的角色为null，放行
        if (mustRole == null) {
            return joinPoint.proceed();
        }
        // 必须的角色不为null，进行判断
        ThrowUtils.throwIf(userRole != mustRole, ErrorCode.NO_AUTH_ERROR);

        return joinPoint.proceed();
    }
}
