package org.example.aicodemother.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.util.UpdateEntity;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.example.aicodemother.common.DeleteRequest;
import org.example.aicodemother.constant.AppConstant;
import org.example.aicodemother.constant.UserConstant;
import org.example.aicodemother.core.AiCodeGeneratorFacade;
import org.example.aicodemother.exception.BusinessException;
import org.example.aicodemother.exception.ErrorCode;
import org.example.aicodemother.exception.ThrowUtils;
import org.example.aicodemother.mapper.AppMapper;
import org.example.aicodemother.model.dto.app.AppAddRequest;
import org.example.aicodemother.model.dto.app.AppAdminUpdateRequest;
import org.example.aicodemother.model.dto.app.AppQueryRequest;
import org.example.aicodemother.model.dto.app.AppUpdateRequest;
import org.example.aicodemother.model.entity.AppEntity;
import org.example.aicodemother.model.entity.ChatHistoryEntity;
import org.example.aicodemother.model.entity.UserEntity;
import org.example.aicodemother.model.enums.CodeGenTypeEnum;
import org.example.aicodemother.model.vo.app.AppVO;
import org.example.aicodemother.model.vo.user.UserVO;
import org.example.aicodemother.service.AppService;
import org.example.aicodemother.service.ChatHistoryService;
import org.example.aicodemother.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.example.aicodemother.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 应用 服务层实现。
 */
@Slf4j
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, AppEntity> implements AppService {

    @Autowired
    private AppMapper appMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Autowired
    private ChatHistoryService chatHistoryService;

    @Override
    public Long addApp(AppAddRequest appAddRequest, HttpServletRequest request) {
        String appName = appAddRequest.getAppName();
        String initPrompt = appAddRequest.getInitPrompt();

        // 参数校验
        ThrowUtils.throwIf(StrUtil.isBlank(appName), ErrorCode.PARAMS_ERROR, "应用名称不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(initPrompt), ErrorCode.PARAMS_ERROR, "初始Prompt不能为空");
        ThrowUtils.throwIf(appName.length() > 256, ErrorCode.PARAMS_ERROR, "应用名称过长");

        // 获取当前登录用户
        UserEntity loginUser = getLoginUser(request);

        // 创建应用
        AppEntity app = new AppEntity();
        app.setAppName(appName);
        app.setInitPrompt(initPrompt);
        app.setCodeGenType(CodeGenTypeEnum.MULTI_FILE.getValue());
        app.setUserId(loginUser.getId());

        boolean result = this.save(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);

        return app.getId();
    }

    @Override
    public AppEntity getAppById(Long id) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);

        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("id", id);
        queryWrapper.eq("isDelete", 0);
        AppEntity appEntity = appMapper.selectOneByQuery(queryWrapper);
        ThrowUtils.throwIf(appEntity == null, ErrorCode.NOT_FOUND_ERROR);

        return appEntity;
    }

    @Override
    public AppVO getAppVOById(Long id) {
        return getAppVO(getAppById(id));
    }

    @Override
    public Boolean updateApp(AppUpdateRequest appUpdateRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appUpdateRequest == null || appUpdateRequest.getId() == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(StrUtil.isBlank(appUpdateRequest.getAppName()), ErrorCode.PARAMS_ERROR, "应用名称不能为空");

        // 获取当前登录用户
        UserEntity loginUser = getLoginUser(request);
        // 查询应用
        AppEntity app = getAppById(appUpdateRequest.getId());
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 校验是否为应用创建者
        if (!Objects.equals(loginUser.getId(), app.getUserId())){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR,"无权限");
        }

        // 更新应用名称
        AppEntity updateApp = UpdateEntity.of(AppEntity.class);
        updateApp.setAppName(appUpdateRequest.getAppName());
        updateApp.setId(appUpdateRequest.getId());
        updateApp.setEditTime(LocalDateTime.now());
        boolean result = this.updateById(updateApp);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);

        return true;
    }

    @Override
    public Boolean deleteApp(DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);

        // 获取当前登录用户
        UserEntity loginUser = getLoginUser(request);
        // 查询应用
        AppEntity app = getAppById(deleteRequest.getId());
        // 校验是否为应用创建者
        ThrowUtils.throwIf(!Objects.equals(loginUser.getId(), app.getUserId()), ErrorCode.NO_AUTH_ERROR, "只能删除自己的应用");
        if (!Objects.equals(loginUser.getId(), app.getUserId())){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR,"无权限");
        }

        // 逻辑删除
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("id", deleteRequest.getId());
        queryWrapper.eq("isDelete", 0);

        AppEntity updateApp = UpdateEntity.of(AppEntity.class);
        updateApp.setIsDelete(1);
        int i = appMapper.updateByQuery(updateApp, queryWrapper);
        ThrowUtils.throwIf(i <= 0, ErrorCode.OPERATION_ERROR);
        try {
            chatHistoryService.deleteChatHistoryByAppId(deleteRequest.getId());
        } catch (Exception e) {
            log.error("用户删除应用关联的对话历史消息失败: {}", e.getMessage());
        }

        return true;
    }

    @Override
    public Page<AppVO> listMyAppVOByPage(AppQueryRequest appQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);

        // 获取当前登录用户
        UserEntity loginUser = getLoginUser(request);
        // 限制每页最多 20 个
        long pageSize = appQueryRequest.getPageSize();
        if (pageSize > 20) {
            pageSize = 20;
        }

        // 只查询当前用户的、未删除的应用
        appQueryRequest.setUserId(loginUser.getId());
        QueryWrapper queryWrapper = getQueryWrapper(appQueryRequest);

        long pageNum = appQueryRequest.getPageNum();
        Page<AppEntity> appPage = this.page(Page.of(pageNum, pageSize), queryWrapper);

        return buildAppVOPage(appPage, pageNum, pageSize);
    }

    @Override
    public Page<AppVO> listFeaturedAppVOByPage(AppQueryRequest appQueryRequest) {
        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);

        // 限制每页最多 20 个
        long pageSize = appQueryRequest.getPageSize();
        if (pageSize > 20) {
            pageSize = 20;
        }

        appQueryRequest.setPriority(AppConstant.GOOD_APP_PRIORITY);
        QueryWrapper queryWrapper = getQueryWrapper(appQueryRequest);

        long pageNum = appQueryRequest.getPageNum();
        Page<AppEntity> appPage = this.page(Page.of(pageNum, pageSize), queryWrapper);

        return buildAppVOPage(appPage, pageNum, pageSize);
    }

    @Override
    public Boolean adminDeleteApp(DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);

        // 逻辑删除
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("id", deleteRequest.getId());
        queryWrapper.eq("isDelete", 0);

        AppEntity updateApp = UpdateEntity.of(AppEntity.class);
        updateApp.setIsDelete(1);
        int i = appMapper.updateByQuery(updateApp, queryWrapper);
        ThrowUtils.throwIf(i <= 0, ErrorCode.OPERATION_ERROR);
        try {
            chatHistoryService.deleteChatHistoryByAppId(deleteRequest.getId());
        } catch (Exception e) {
            log.error("管理员删除应用关联的对话历史消息失败: {}", e.getMessage());
        }

        return true;
    }

    @Override
    public Boolean adminUpdateApp(AppAdminUpdateRequest appAdminUpdateRequest) {
        ThrowUtils.throwIf(appAdminUpdateRequest == null || appAdminUpdateRequest.getId() == null, ErrorCode.PARAMS_ERROR);

        // 查询应用是否存在
        getAppById(appAdminUpdateRequest.getId());

        // 更新字段
        AppEntity updateApp = UpdateEntity.of(AppEntity.class);
        updateApp.setId(appAdminUpdateRequest.getId());

        String appName = appAdminUpdateRequest.getAppName();
        String cover = appAdminUpdateRequest.getCover();
        Integer priority = appAdminUpdateRequest.getPriority();

        if (StrUtil.isNotBlank(appName)) {
            updateApp.setAppName(appName);
        }
        if (StrUtil.isNotBlank(cover)) {
            updateApp.setCover(cover);
        }
        if (priority != null) {
            updateApp.setPriority(priority);
        }

        boolean result = this.updateById(updateApp);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);

        return true;
    }

    @Override
    public Page<AppVO> adminListAppVOByPage(AppQueryRequest appQueryRequest) {
        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);

        // 管理员每页数量不限
        QueryWrapper queryWrapper = getQueryWrapper(appQueryRequest);

        long pageNum = appQueryRequest.getPageNum();
        long pageSize = appQueryRequest.getPageSize();
        Page<AppEntity> appPage = this.page(Page.of(pageNum, pageSize), queryWrapper);

        return buildAppVOPage(appPage, pageNum, pageSize);
    }

    @Override
    public Flux<String> chatToGenCode(Long appId, String message, HttpServletRequest request) {
        // 获取当前登录用户
        UserEntity loginUser = getLoginUser(request);
        // 1. 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "用户消息不能为空");
        // 2. 查询应用信息
        AppEntity app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 3. 验证用户是否有权限访问该应用，仅本人可以生成代码
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限访问该应用");
        }
        // 4. 获取应用的代码生成类型
        String codeGenTypeStr = app.getCodeGenType();
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenTypeStr);
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型");
        }
        // 5. 先保存用户消息，再保存 AI 回复或错误信息
        ChatHistoryEntity userChatHistory = chatHistoryService.addUserMessage(appId, loginUser.getId(), message);
        StringBuilder aiMessageBuilder = new StringBuilder();
        return aiCodeGeneratorFacade.generateAndSaveAiCodeStream(message, codeGenTypeEnum, appId)
                .doOnNext(aiMessageBuilder::append)
                .doOnComplete(() -> {
                    String aiMessage = StrUtil.blankToDefault(aiMessageBuilder.toString(), "AI 未返回内容");
                    chatHistoryService.addAiMessage(appId, loginUser.getId(), userChatHistory.getId(), aiMessage);
                })
                .doOnError(e -> chatHistoryService.addAiErrorMessage(appId, loginUser.getId(), userChatHistory.getId(), e.getMessage()));
    }

    @Override
    public String deployApp(Long appId, HttpServletRequest request) {
        // 获取当前登录用户
        UserEntity loginUser = getLoginUser(request);
        // 1. 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        // 2. 查询应用信息
        AppEntity app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 3. 验证用户是否有权限部署该应用，仅本人可以部署
        if (!app.getUserId().equals(loginUser.getId()) && !loginUser.getUserRole().equals(UserConstant.ADMIN_ROLE)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限部署该应用");
        }
        // 4. 检查是否已有 deployKey
        String deployKey = app.getDeployKey();
        // 没有则生成 6 位 deployKey（大小写字母 + 数字）
        if (StrUtil.isBlank(deployKey)) {
            deployKey = RandomUtil.randomString(6);
        }
        // 5. 获取代码生成类型，构建源目录路径
        String codeGenType = app.getCodeGenType();
        String sourceDirName = codeGenType + "_" + appId;
        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;
        // 6. 检查源目录是否存在
        File sourceDir = new File(sourceDirPath);
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用代码不存在，请先生成代码");
        }
        // 7. 复制文件到部署目录
        String deployDirPath = AppConstant.CODE_DEPLOY_ROOT_DIR + File.separator + deployKey;
        try {
            FileUtil.copyContent(sourceDir, new File(deployDirPath), true);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "部署失败：" + e.getMessage());
        }
        // 8. 更新应用的 deployKey 和部署时间
        AppEntity updateApp = new AppEntity();
        updateApp.setId(appId);
        updateApp.setDeployKey(deployKey);
        updateApp.setDeployedTime(LocalDateTime.now());
        boolean updateResult = this.updateById(updateApp);
        ThrowUtils.throwIf(!updateResult, ErrorCode.OPERATION_ERROR, "更新应用部署信息失败");
        // 9. 返回可访问的 URL
        return String.format("%s/%s/", AppConstant.CODE_DEPLOY_HOST, deployKey);
    }


    /**
     * 根据查询条件构造数据查询参数
     */
    private QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = appQueryRequest.getId();
        String appName = appQueryRequest.getAppName();
        Long userId = appQueryRequest.getUserId();
        String codeGenType = appQueryRequest.getCodeGenType();
        Integer priority = appQueryRequest.getPriority();
        String deployKey = appQueryRequest.getDeployKey();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();

        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("id", id,  id != null && id > 0)
                .eq("userId", userId, userId != null && userId > 0)
                .eq("codeGenType", codeGenType, StrUtil.isNotBlank(codeGenType))
                .eq("priority", priority, priority != null && priority > 0)
                .eq("deployKey", deployKey, StrUtil.isNotBlank(deployKey))
                .eq("isDelete", 0)
                .like("appName", appName, StrUtil.isNotBlank(appName));

        if (StrUtil.isNotBlank(sortField)) {
            queryWrapper.orderBy(sortField, "ascend".equals(sortOrder));
        }

        return queryWrapper;
    }

    /**
     * 构建 AppVO 分页对象
     */
    private Page<AppVO> buildAppVOPage(Page<AppEntity> appPage, long pageNum, long pageSize) {
        Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
        List<AppVO> appVOList = getAppVOList(appPage.getRecords());
        appVOPage.setRecords(appVOList);
        return appVOPage;
    }

    /**
     * 获取脱敏后的应用信息
     */
    private AppVO getAppVO(AppEntity appEntity) {
        if (appEntity == null) {
            return null;
        }
        AppVO appVO = new AppVO();
        BeanUtil.copyProperties(appEntity, appVO);
        appVO.setUser(userService.getUserVOById(appEntity.getUserId()));
        return appVO;
    }

    /**
     * 获取脱敏后的应用信息列表
     */
    public List<AppVO> getAppVOList(List<AppEntity> appList) {
        if (CollUtil.isEmpty(appList)) {
            return new ArrayList<>();
        }
        // 批量获取用户信息，避免 N+1 查询问题
        Set<Long> userIds = appList.stream()
                .map(AppEntity::getUserId)
                .collect(Collectors.toSet());
        Map<Long, UserVO> userVOMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(UserEntity::getId, userService::getUserVO));
        return appList.stream().map(app -> {
            AppVO appVO = getAppVO(app);
            UserVO userVO = userVOMap.get(app.getUserId());
            appVO.setUser(userVO);
            return appVO;
        }).collect(Collectors.toList());
    }


    /**
     * 从 session 获取当前登录用户
     */
    private UserEntity getLoginUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        UserEntity currentUser = (UserEntity) userObj;
        ThrowUtils.throwIf(currentUser == null || currentUser.getId() == null, ErrorCode.NOT_LOGIN_ERROR);
        return currentUser;
    }

}
