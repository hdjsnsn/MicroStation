package org.example.aicodemother.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import jakarta.servlet.http.HttpServletRequest;
import org.example.aicodemother.common.DeleteRequest;
import org.example.aicodemother.model.dto.app.AppAddRequest;
import org.example.aicodemother.model.dto.app.AppAdminUpdateRequest;
import org.example.aicodemother.model.dto.app.AppQueryRequest;
import org.example.aicodemother.model.dto.app.AppUpdateRequest;
import org.example.aicodemother.model.entity.AppEntity;
import org.example.aicodemother.model.vo.app.AppVO;
import reactor.core.publisher.Flux;

/**
 * 应用 服务层。
 */
public interface AppService extends IService<AppEntity> {

    /**
     * 创建应用
     */
    Long addApp(AppAddRequest appAddRequest, HttpServletRequest request);

    /**
     * 根据 id 获取应用
     */
    AppEntity getAppById(Long id);

    /**
     * 根据 id 获取包装类
     */
    AppVO getAppVOById(Long id);

    /**
     * 修改自己的应用
     */
    Boolean updateApp(AppUpdateRequest appUpdateRequest, HttpServletRequest request);

    /**
     * 删除自己的应用
     */
    Boolean deleteApp(DeleteRequest deleteRequest, HttpServletRequest request);

    /**
     * 分页查询自己的应用列表
     */
    Page<AppVO> listMyAppVOByPage(AppQueryRequest appQueryRequest, HttpServletRequest request);

    /**
     * 分页查询精选的应用列表
     */
    Page<AppVO> listFeaturedAppVOByPage(AppQueryRequest appQueryRequest);

    /**
     * 管理员删除任意应用
     */
    Boolean adminDeleteApp(DeleteRequest deleteRequest);

    /**
     * 管理员更新任意应用
     */
    Boolean adminUpdateApp(AppAdminUpdateRequest appAdminUpdateRequest);

    /**
     * 管理员分页查询应用列表
     */
    Page<AppVO> adminListAppVOByPage(AppQueryRequest appQueryRequest);

    /**
     * 对话生成应用流式代码
     * @param appID 应用ID
     * @param userMessage 提示词
     * @return 流式响应
     */
    Flux<String> chatToGenCode(Long appID, String userMessage, HttpServletRequest request);

    /**
     *  应用部署
     */
    String deployApp(Long appID, HttpServletRequest request);

    /**
     * 从 session 获取当前登录用户
     */

}
