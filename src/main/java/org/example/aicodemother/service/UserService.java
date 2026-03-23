package org.example.aicodemother.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import jakarta.servlet.http.HttpServletRequest;
import org.example.aicodemother.common.DeleteRequest;
import org.example.aicodemother.model.dto.user.UserAddRequest;
import org.example.aicodemother.model.dto.user.UserQueryRequest;
import org.example.aicodemother.model.dto.user.UserUpdateRequest;
import org.example.aicodemother.model.entity.UserEntity;
import org.example.aicodemother.model.vo.user.UserVO;

public interface UserService extends IService<UserEntity> {

    /**
     * 创建用户（仅管理员）
     */
    Long addUser(UserAddRequest userAddRequest);

    /**
     * 根据 id 获取用户（仅管理员）
     */
    UserEntity getUserById(Long id);

    /**
     * 根据 id 获取包装类
     */
    UserVO getUserVOById(Long id);

    /**
     * 删除用户（仅管理员）
     */
    Boolean deleteUser(DeleteRequest deleteRequest, HttpServletRequest httpServletRequest);

    /**
     * 更新用户（仅管理员）
     */
    Boolean updateUser(UserUpdateRequest userUpdateRequest);

    /**
     * 分页获取用户封装列表（仅管理员）
     */
    Page<UserVO> listUserVOByPage(UserQueryRequest userQueryRequest);
}
