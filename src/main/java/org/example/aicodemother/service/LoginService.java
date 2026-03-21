package org.example.aicodemother.service;

import com.mybatisflex.core.service.IService;
import jakarta.servlet.http.HttpServletRequest;
import org.example.aicodemother.model.dto.login.UserLoginRequest;
import org.example.aicodemother.model.dto.login.UserRegisterRequest;
import org.example.aicodemother.model.entity.UserEntity;
import org.example.aicodemother.model.vo.login.LoginUserVO;

/**
 * 用户 服务层。
 *
 * @author cmh
 */
public interface LoginService extends IService<UserEntity> {

    /**
     * 用户注册
     *
     * @param userRegisterRequest   用户注册请求
     * @return 新用户 id
     */
    Long userRegister(UserRegisterRequest userRegisterRequest);

    /**
     * 用户登录
     *
     * @param userLoginRequest  用户登录请求
     *
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(UserLoginRequest userLoginRequest, HttpServletRequest request);

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    UserEntity getLoginUser(HttpServletRequest request);

    /**
     * 用户注销
     *
     * @param request
     * @return 退出登录是否成功
     */
    Boolean userLogout(HttpServletRequest request);


    /**
     * 获取脱敏的已登录用户信息
     *
     * @return
     */
    LoginUserVO getLoginUserVO(UserEntity userEntity);

    /**
     * 加密
     *
     * @param userPassword 用户密码
     * @return 加密后的用户密码
     */
    String getEncryptPassword(String userPassword);

}
