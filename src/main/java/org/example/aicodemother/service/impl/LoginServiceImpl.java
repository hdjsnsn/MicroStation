package org.example.aicodemother.service.impl;

import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.example.aicodemother.exception.BusinessException;
import org.example.aicodemother.exception.ErrorCode;
import org.example.aicodemother.model.dto.login.UserLoginRequest;
import org.example.aicodemother.model.dto.login.UserRegisterRequest;
import org.example.aicodemother.model.entity.UserEntity;
import org.example.aicodemother.mapper.UserMapper;
import org.example.aicodemother.model.enums.UserRoleEnum;
import org.example.aicodemother.model.vo.login.LoginUserVO;
import org.example.aicodemother.service.LoginService;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

import static org.example.aicodemother.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户 服务层实现。
 *
 * @author cmh
 */
@Service
public class LoginServiceImpl extends ServiceImpl<UserMapper, UserEntity>  implements LoginService {

    @Override
    public Long userRegister(UserRegisterRequest userRegisterRequest) {

        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();


        // 1. 校验参数
        if (StrUtil.hasBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号长度不小于4位数");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度不小于8位数");
        }
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        // 2. 查询用户是否已存在
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("isDelete", 0);
        long count = this.mapper.selectCountByQuery(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号已存在");
        }
        // 3. 加密密码
        String encryptPassword = getEncryptPassword(userPassword);
        // 4. 创建用户，插入数据库
        UserEntity userEntity = new UserEntity();
        userEntity.setUserAccount(userAccount);
        userEntity.setUserPassword(encryptPassword);
        userEntity.setUserRole(UserRoleEnum.USER.getValue());
        boolean saveResult = this.save(userEntity);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "注册失败，数据库错误");
        }
        return userEntity.getId();
    }

    @Override
    public LoginUserVO userLogin(UserLoginRequest userLoginRequest, HttpServletRequest request) {

        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();

        // 1. 校验参数
        if (StrUtil.hasBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号长度不小于4");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度不小于8");
        }
        // 2. 加密
        String encryptPassword = getEncryptPassword(userPassword);
        // 3. 查询用户是否存在
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        queryWrapper.eq("isDelete", 0);
        UserEntity userEntity = this.mapper.selectOneByQuery(queryWrapper);
        if (userEntity == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        // 4. 如果用户存在，记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, userEntity);
        // 5. 返回脱敏的用户信息
        return this.getLoginUserVO(userEntity);
    }

    @Override
    public UserEntity getLoginUser(HttpServletRequest request) {
        // 先判断用户是否登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        UserEntity currentUserEntity = (UserEntity) userObj;
        if (currentUserEntity == null || currentUserEntity.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从数据库查询当前用户信息
        long userId = currentUserEntity.getId();
        currentUserEntity = this.getById(userId);
        if (currentUserEntity == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUserEntity;
    }

    @Override
    public Boolean userLogout(HttpServletRequest request) {
        // 先判断用户是否登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userObj == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户未登录");
        }
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    @Override
    public LoginUserVO getLoginUserVO(UserEntity userEntity) {
        if (userEntity == null) {
            return null;
        }

        LoginUserVO loginUserVO = new LoginUserVO();
        loginUserVO.setId(userEntity.getId());
        loginUserVO.setUserAccount(userEntity.getUserAccount());
        loginUserVO.setUserName(userEntity.getUserName());
        loginUserVO.setUserAvatar(userEntity.getUserAvatar());
        loginUserVO.setUserProfile(userEntity.getUserProfile());
        loginUserVO.setUserRole(userEntity.getUserRole());
        loginUserVO.setCreateTime(userEntity.getCreateTime());
        loginUserVO.setUpdateTime(userEntity.getUpdateTime());

        return loginUserVO;
    }

    @Override
    public String getEncryptPassword(String userPassword) {
        // 盐值，混淆密码
        final String SALT = "cmh";
        return DigestUtils.md5DigestAsHex((userPassword + SALT).getBytes(StandardCharsets.UTF_8));
    }

}
