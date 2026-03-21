package org.example.aicodemother.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.aicodemother.common.BaseResponse;
import org.example.aicodemother.common.ResultUtils;
import org.example.aicodemother.exception.ErrorCode;
import org.example.aicodemother.exception.ThrowUtils;
import org.example.aicodemother.model.dto.login.UserLoginRequest;
import org.example.aicodemother.model.dto.login.UserRegisterRequest;
import org.example.aicodemother.model.entity.UserEntity;
import org.example.aicodemother.model.vo.login.LoginUserVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.example.aicodemother.service.LoginService;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户登录 controller
 *
 * @author cmh
 */
@RestController
public class LoginController {

    @Autowired
    private LoginService loginService;

    /**
     * 用户注册
     *
     * @param userRegisterRequest 用户注册请求
     * @return 注册结果
     */
    @PostMapping("/register")
    public BaseResponse<Long> register(@RequestBody UserRegisterRequest userRegisterRequest) {
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);
        long result = loginService.userRegister(userRegisterRequest);
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest 用户登录请求
     * @param request          请求对象
     * @return 脱敏后的用户登录信息
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> Login(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
        LoginUserVO loginUserVO = loginService.userLogin(userLoginRequest, request);
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 获取当前登录用户的信息
     */
    @GetMapping("/get/loginUser")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        UserEntity loginUserEntity = loginService.getLoginUser(request);
        return ResultUtils.success(loginService.getLoginUserVO(loginUserEntity));
    }

    /**
     * 用户注销
     *
     * @param request 请求对象
     * @return
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> Logout(HttpServletRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        boolean result = loginService.userLogout(request);
        return ResultUtils.success(result);
    }

}
