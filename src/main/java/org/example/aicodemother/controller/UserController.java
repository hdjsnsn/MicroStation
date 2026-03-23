package org.example.aicodemother.controller;

import com.mybatisflex.core.paginate.Page;
import jakarta.servlet.http.HttpServletRequest;
import org.example.aicodemother.annotation.AuthCheck;
import org.example.aicodemother.common.BaseResponse;
import org.example.aicodemother.common.DeleteRequest;
import org.example.aicodemother.common.ResultUtils;
import org.example.aicodemother.constant.UserConstant;
import org.example.aicodemother.exception.BusinessException;
import org.example.aicodemother.exception.ErrorCode;
import org.example.aicodemother.exception.ThrowUtils;
import org.example.aicodemother.model.dto.user.UserAddRequest;
import org.example.aicodemother.model.dto.user.UserQueryRequest;
import org.example.aicodemother.model.dto.user.UserUpdateRequest;
import org.example.aicodemother.model.entity.UserEntity;
import org.example.aicodemother.model.vo.user.UserVO;
import org.example.aicodemother.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 创建用户
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
        ThrowUtils.throwIf(userAddRequest == null, ErrorCode.PARAMS_ERROR);
        Long id = userService.addUser(userAddRequest);
        return ResultUtils.success(id);
    }

    /**
     * 根据 id 获取用户（仅管理员）
     */
    @GetMapping("/getByID")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<UserEntity> getUserById(Long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        UserEntity userEntity = userService.getUserById(id);
        return ResultUtils.success(userEntity);
    }

    /**
     * 根据 id 获取包装类(普通用户)
     */
    @GetMapping("/getByID/vo")
    public BaseResponse<UserVO> getUserVOById(Long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        UserVO userVO = userService.getUserVOById(id);
        return ResultUtils.success(userVO);
    }

    /**
     * 删除用户
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest, HttpServletRequest httpServletRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Boolean b = userService.deleteUser(deleteRequest,httpServletRequest);
        return ResultUtils.success(b);
    }

    /**
     * 更新用户
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.updateUser(userUpdateRequest);
        return ResultUtils.success(b);
    }

    /**
     * 分页获取用户封装列表（仅管理员）
     *
     * @param userQueryRequest 查询请求参数
     */
    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);
        Page<UserVO> result = userService.listUserVOByPage(userQueryRequest);
        return ResultUtils.success(result);
    }

}
