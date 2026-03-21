package org.example.aicodemother.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.util.UpdateEntity;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.example.aicodemother.common.DeleteRequest;
import org.example.aicodemother.exception.BusinessException;
import org.example.aicodemother.exception.ErrorCode;
import org.example.aicodemother.exception.ThrowUtils;
import org.example.aicodemother.mapper.UserMapper;
import org.example.aicodemother.model.dto.user.UserAddRequest;
import org.example.aicodemother.model.dto.user.UserQueryRequest;
import org.example.aicodemother.model.dto.user.UserUpdateRequest;
import org.example.aicodemother.model.entity.UserEntity;
import org.example.aicodemother.model.vo.user.UserVO;
import org.example.aicodemother.service.LoginService;
import org.example.aicodemother.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.example.aicodemother.constant.UserConstant.USER_LOGIN_STATE;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserEntity> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private LoginService loginService;

    @Override
    public Long addUser(UserAddRequest userAddRequest) {
        // 默认密码 12345678
        final String DEFAULT_PASSWORD = "12345678";
        // 加密
        String encryptPassword = loginService.getEncryptPassword(DEFAULT_PASSWORD);

        // 请求类转换为实体类
        UserEntity user = new UserEntity();
        user.setUserName(userAddRequest.getUserName());
        user.setUserAccount(userAddRequest.getUserAccount());
        user.setUserAvatar(userAddRequest.getUserAvatar());
        user.setUserProfile(userAddRequest.getUserProfile());
        user.setUserRole(userAddRequest.getUserRole());
        // 判断是否有密码,没有则默认密码
        if(!StrUtil.isEmpty(userAddRequest.getPassword())){
            encryptPassword = loginService.getEncryptPassword(userAddRequest.getPassword());
        }
        user.setUserPassword(encryptPassword);

        // 存入数据库
        boolean result = this.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);

        return user.getId();
    }

    @Override
    public UserEntity getUserById(Long id) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("id", id);
        queryWrapper.eq("isDelete", 0);
        UserEntity userEntity = userMapper.selectOneByQuery(queryWrapper);
        ThrowUtils.throwIf(userEntity == null, ErrorCode.NOT_FOUND_ERROR);
        return userEntity;
    }

    @Override
    public UserVO getUserVOById(Long id){
        return getUserVO(getUserById(id));
    }

    @Override
    public Boolean deleteUser(DeleteRequest deleteRequest) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("id", deleteRequest.getId());
        queryWrapper.eq("isDelete", 0);

        UserEntity userEntity = UpdateEntity.of(UserEntity.class);
        userEntity.setIsDelete(1);
        int i = userMapper.updateByQuery(userEntity,queryWrapper);
        if (i <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        return true;
    }

    @Override
    public Boolean updateUser(UserUpdateRequest userUpdateRequest) {

        UserEntity user = new UserEntity();
        user.setId(userUpdateRequest.getId());
        user.setUserName(userUpdateRequest.getUserName());
        user.setUserAvatar(userUpdateRequest.getUserAvatar());
        user.setUserProfile(userUpdateRequest.getUserProfile());
        user.setUserRole(userUpdateRequest.getUserRole());

        boolean result = this.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return true;
    }

    @Override
    public Page<UserVO> listUserVOByPage(UserQueryRequest userQueryRequest, HttpServletRequest httpServletRequest) {
        // 获取当前登录用户的ID
        Object userObj = httpServletRequest.getSession().getAttribute(USER_LOGIN_STATE);
        UserEntity currentUser = (UserEntity) userObj;

        long pageNum = userQueryRequest.getPageNum();
        long pageSize = userQueryRequest.getPageSize();
        Page<UserEntity> userPage = this.page(Page.of(pageNum, pageSize),
                this.getQueryWrapper(userQueryRequest));
        // 数据脱敏
        Page<UserVO> userVOPage = new Page<>(pageNum, pageSize, userPage.getTotalRow());
        List<UserVO> userVOList = this.getUserVOList(userPage.getRecords(),currentUser.getId());
        userVOPage.setRecords(userVOList);
        return userVOPage;
    }

    /**
     * 根据查询条件构造数据查询参数
     *
     */
    public QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        return QueryWrapper.create()
                .eq("id", id) // where id = ${id}
                .eq("userRole", userRole) // and userRole = ${userRole}
                .eq("isDelete", 0)
                .like("userAccount", userAccount)
                .like("userName", userName)
                .like("userProfile", userProfile)
                .orderBy(sortField, "ascend".equals(sortOrder));
    }

    /**
     * 获取脱敏后的用户信息
     *
     * @param userEntity 用户信息
     * @return 脱敏后的用户信息
     */
    public UserVO getUserVO(UserEntity userEntity) {
        if (userEntity == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(userEntity, userVO);
        return userVO;
    }

    /**
     * 获取脱敏后的用户信息（分页）
     *
     * @param userEntityList 用户列表
     * @return 自定义工具栏…
     */
    public List<UserVO> getUserVOList(List<UserEntity> userEntityList,Long userId) {
        if (CollUtil.isEmpty(userEntityList)) {
            return new ArrayList<>();
        }
        return userEntityList.stream()
                .filter(userEntity -> !Objects.equals(userEntity.getId(), userId))
                .map(this::getUserVO)
                .collect(Collectors.toList());
    }
}
