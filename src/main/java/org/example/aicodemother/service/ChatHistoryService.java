package org.example.aicodemother.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import jakarta.servlet.http.HttpServletRequest;
import org.example.aicodemother.model.dto.chatHistory.ChatHistoryQueryRequest;
import org.example.aicodemother.model.entity.ChatHistoryEntity;
import org.example.aicodemother.model.vo.chatHistory.ChatHistoryVO;

/**
 * 对话历史 服务层。
 */
public interface ChatHistoryService extends IService<ChatHistoryEntity> {

    /**
     * 保存用户消息
     */
    ChatHistoryEntity addUserMessage(Long appId, Long userId, String message);

    /**
     * 保存 AI 消息
     */
    ChatHistoryEntity addAiMessage(Long appId, Long userId, Long parentId, String message);

    /**
     * 保存 AI 错误消息
     */
    ChatHistoryEntity addAiErrorMessage(Long appId, Long userId, Long parentId, String errorMessage);

    /**
     * 分页查询某个应用的对话历史
     */
    Page<ChatHistoryVO> listAppChatHistoryByPage(ChatHistoryQueryRequest chatHistoryQueryRequest, HttpServletRequest request);

    /**
     * 管理员分页查询所有对话历史
     */
    Page<ChatHistoryVO> listAllChatHistoryByPageForAdmin(ChatHistoryQueryRequest chatHistoryQueryRequest,HttpServletRequest request);

    /**
     * 根据应用 id 逻辑删除对话历史
     */
    Boolean deleteChatHistoryByAppId(Long appId);

    int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount);
}
