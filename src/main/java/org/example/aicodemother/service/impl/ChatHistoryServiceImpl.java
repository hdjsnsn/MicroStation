package org.example.aicodemother.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.util.UpdateEntity;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.example.aicodemother.constant.UserConstant;
import org.example.aicodemother.exception.BusinessException;
import org.example.aicodemother.exception.ErrorCode;
import org.example.aicodemother.exception.ThrowUtils;
import org.example.aicodemother.mapper.ChatHistoryMapper;
import org.example.aicodemother.model.dto.chatHistory.ChatHistoryQueryRequest;
import org.example.aicodemother.model.entity.AppEntity;
import org.example.aicodemother.model.entity.ChatHistoryEntity;
import org.example.aicodemother.model.entity.UserEntity;
import org.example.aicodemother.model.enums.MessageTypeEnum;
import org.example.aicodemother.model.vo.chatHistory.ChatHistoryVO;
import org.example.aicodemother.service.AppService;
import org.example.aicodemother.service.ChatHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.example.aicodemother.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 对话历史 服务层实现。
 */
@Slf4j
@Service
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistoryEntity> implements ChatHistoryService {

    @Resource
    private ChatHistoryMapper chatHistoryMapper;

    @Resource
    @Lazy
    private AppService appService;

    @Override
    public ChatHistoryEntity addUserMessage(Long appId, Long userId, String message) {
        return saveChatHistory(appId, userId, null, message, MessageTypeEnum.USER.getValue());
    }

    @Override
    public ChatHistoryEntity addAiMessage(Long appId, Long userId, Long parentId, String message) {
        return saveChatHistory(appId, userId, parentId, message, MessageTypeEnum.AI.getValue());
    }

    @Override
    public ChatHistoryEntity addAiErrorMessage(Long appId, Long userId, Long parentId, String errorMessage) {
        String errorContent = "AI 回复失败：" + StrUtil.blankToDefault(errorMessage, "未知错误");
        return saveChatHistory(appId, userId, parentId, errorContent, MessageTypeEnum.AI.getValue());
    }

    @Override
    public Page<ChatHistoryVO> listAppChatHistoryByPage(ChatHistoryQueryRequest chatHistoryQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(getLoginUser(request) == null, ErrorCode.NOT_LOGIN_ERROR);
        ThrowUtils.throwIf(chatHistoryQueryRequest == null, ErrorCode.PARAMS_ERROR);
        Long appId = chatHistoryQueryRequest.getAppId();
        int pageSize = chatHistoryQueryRequest.getPageSize();
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 id 不能为空");
        ThrowUtils.throwIf(pageSize <= 0 || pageSize >= 50, ErrorCode.PARAMS_ERROR, "页面大小必须在 1-50之间");

        // 校验当前用户是否有权查看该应用的历史记录
        UserEntity currentUser = getLoginUser(request);
        AppEntity appEntity = appService.getAppById(appId);
        ThrowUtils.throwIf(appEntity == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        boolean isOwner = Objects.equals(currentUser.getId(), appEntity.getUserId());
        boolean isAdmin = UserConstant.ADMIN_ROLE.equals(currentUser.getUserRole());
        ThrowUtils.throwIf(!isOwner && !isAdmin, ErrorCode.NO_AUTH_ERROR, "无权限查看该应用的对话历史");

        QueryWrapper queryWrapper = getQueryWrapper(chatHistoryQueryRequest);

        Page<ChatHistoryEntity> chatHistoryPage = this.page(Page.of(1, pageSize), queryWrapper);
        return buildChatHistoryVOPage(chatHistoryPage, 1, pageSize);
    }

    @Override
    public Page<ChatHistoryVO> listAllChatHistoryByPageForAdmin(ChatHistoryQueryRequest chatHistoryQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(!UserConstant.ADMIN_ROLE.equals(getLoginUser(request).getUserRole()), ErrorCode.NO_AUTH_ERROR, "无管理员权限");
        long pageNum = chatHistoryQueryRequest.getPageNum();
        long pageSize = chatHistoryQueryRequest.getPageSize();
        // 查询数据
        QueryWrapper queryWrapper = getQueryWrapper(chatHistoryQueryRequest);
        return buildChatHistoryVOPage(page(Page.of(pageNum, pageSize), queryWrapper), pageNum, pageSize);
    }

    @Override
    public Boolean deleteChatHistoryByAppId(Long appId) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 id 不能为空");

        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("appId", appId)
                .eq("isDelete", 0);

        ChatHistoryEntity updateEntity = UpdateEntity.of(ChatHistoryEntity.class);
        updateEntity.setIsDelete(1);
        chatHistoryMapper.updateByQuery(updateEntity, queryWrapper);
        return true;
    }

    @Override
    public int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount) {
        try {
            // 直接构造查询条件，起始点为 1 而不是 0，用于排除最新的用户消息
            QueryWrapper queryWrapper = QueryWrapper.create()
                    .eq(ChatHistoryEntity::getAppId, appId)
                    .orderBy(ChatHistoryEntity::getCreateTime, false)
                    .limit(1, maxCount);
            List<ChatHistoryEntity> historyList = this.list(queryWrapper);
            if (CollUtil.isEmpty(historyList)) {
                return 0;
            }
            // 反转列表，确保按时间正序（老的在前，新的在后）
            historyList = historyList.reversed();
            // 按时间顺序添加到记忆中
            int loadedCount = 0;
            // 先清理历史缓存，防止重复加载
            chatMemory.clear();
            for (ChatHistoryEntity history : historyList) {
                if (MessageTypeEnum.USER.getValue().equals(history.getMessageType())) {
                    chatMemory.add(UserMessage.from(history.getMessage()));
                    loadedCount++;
                } else if (MessageTypeEnum.AI.getValue().equals(history.getMessageType())) {
                    chatMemory.add(AiMessage.from(history.getMessage()));
                    loadedCount++;
                }
            }
            log.info("成功为 appId: {} 加载了 {} 条历史对话", appId, loadedCount);
            return loadedCount;
        } catch (Exception e) {
            log.error("加载历史对话失败，appId: {}, error: {}", appId, e.getMessage(), e);
            // 加载失败不影响系统运行，只是没有历史上下文
            return 0;
        }
    }


    /**
     * 保存对话历史
     */
    private ChatHistoryEntity saveChatHistory(Long appId, Long userId, Long parentId, String message, String messageType) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 id 不能为空");
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR, "用户 id 不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "消息不能为空");
        // 校验消息类型是否有效
        ThrowUtils.throwIf(MessageTypeEnum.getEnumByValue(messageType) == null, ErrorCode.PARAMS_ERROR, "消息类型错误");

        ChatHistoryEntity chatHistoryEntity = new ChatHistoryEntity();
        chatHistoryEntity.setParentId(parentId);
        chatHistoryEntity.setMessage(message);
        chatHistoryEntity.setMessageType(messageType);
        chatHistoryEntity.setAppId(appId);
        chatHistoryEntity.setUserId(userId);

        boolean result = this.save(chatHistoryEntity);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "保存对话历史失败");
        return chatHistoryEntity;
    }

    /**
     * 构造查询条件
     */
    private QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest) {
        if (chatHistoryQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long appId = chatHistoryQueryRequest.getAppId();
        Long userId = chatHistoryQueryRequest.getUserId();
        String messageType = chatHistoryQueryRequest.getMessageType();
        String message = chatHistoryQueryRequest.getMessage();
        LocalDateTime lastCreateTime = chatHistoryQueryRequest.getLastCreateTime();
        String sortField = chatHistoryQueryRequest.getSortField();
        String sortOrder = chatHistoryQueryRequest.getSortOrder();

        if (StrUtil.isNotBlank(messageType) && MessageTypeEnum.getEnumByValue(messageType) == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "消息类型错误");
        }

        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("appId", appId, appId != null && appId > 0)
                .eq("userId", userId, userId != null && userId > 0)
                .eq("messageType", messageType, StrUtil.isNotBlank(messageType))
                .eq("isDelete", 0)
                .like("message", message, StrUtil.isNotBlank(message));

        if (lastCreateTime != null) {
            queryWrapper.lt("createTime", lastCreateTime);
        }
        if (StrUtil.isNotBlank(sortField)){
            queryWrapper.orderBy(sortField, "ascend".equals(sortOrder));
        }
        else {
            queryWrapper.orderBy("createTime", false);
        }
        return queryWrapper;
    }

    /**
     * 构造分页结果
     */
    private Page<ChatHistoryVO> buildChatHistoryVOPage(Page<ChatHistoryEntity> chatHistoryPage, long pageNum, long pageSize) {
        List<ChatHistoryVO> chatHistoryVOList = new ArrayList<>();
        List<ChatHistoryEntity> chatHistoryEntityList = chatHistoryPage.getRecords();
        if (CollUtil.isNotEmpty(chatHistoryEntityList)) {
            chatHistoryVOList = chatHistoryEntityList.stream()
                    .map(chatHistoryEntity -> {
                        ChatHistoryVO chatHistoryVO = new ChatHistoryVO();
                        BeanUtil.copyProperties(chatHistoryEntity, chatHistoryVO);
                        return chatHistoryVO;
                    })
                    .collect(Collectors.toList());
        }
        Page<ChatHistoryVO> chatHistoryVOPage = new Page<>(pageNum, pageSize, chatHistoryPage.getTotalRow());
        chatHistoryVOPage.setRecords(chatHistoryVOList);
        return chatHistoryVOPage;
    }

    private UserEntity getLoginUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        UserEntity currentUser = (UserEntity) userObj;
        ThrowUtils.throwIf(currentUser == null || currentUser.getId() == null, ErrorCode.NOT_LOGIN_ERROR);
        return currentUser;
    }
}
