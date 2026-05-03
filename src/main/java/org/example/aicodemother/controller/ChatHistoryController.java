package org.example.aicodemother.controller;

import cn.hutool.core.convert.Convert;
import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.example.aicodemother.annotation.AuthCheck;
import org.example.aicodemother.common.BaseResponse;
import org.example.aicodemother.common.ResultUtils;
import org.example.aicodemother.constant.UserConstant;
import org.example.aicodemother.exception.ErrorCode;
import org.example.aicodemother.exception.ThrowUtils;
import org.example.aicodemother.model.dto.chatHistory.ChatHistoryQueryRequest;
import org.example.aicodemother.model.vo.chatHistory.ChatHistoryVO;
import org.example.aicodemother.service.ChatHistoryService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 对话历史 控制层。
 */
@RestController
@RequestMapping("/chatHistory")
public class ChatHistoryController {

    @Resource
    private ChatHistoryService chatHistoryService;

    /**
     * 分页查询某个应用的对话历史（游标查询）
     *
     * @param appIdStr          应用ID
     * @param pageSize       页面大小
     * @param lastCreateTime 最后一条记录的创建时间
     * @param request        请求
     * @return 对话历史分页
     */
    @GetMapping("/app/{appIdStr}")
    public BaseResponse<Page<ChatHistoryVO>> listAppChatHistoryByPage(@PathVariable String appIdStr,
                                                                      @RequestParam(defaultValue = "10") int pageSize,
                                                                      @RequestParam(required = false) LocalDateTime lastCreateTime,
                                                                      HttpServletRequest request) {
        Long appId = Convert.toLong(appIdStr);
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR);
        ChatHistoryQueryRequest chatHistoryQueryRequest = new ChatHistoryQueryRequest();
        chatHistoryQueryRequest.setAppId(appId);
        chatHistoryQueryRequest.setPageSize(pageSize);
        chatHistoryQueryRequest.setLastCreateTime(lastCreateTime);
        Page<ChatHistoryVO> result = chatHistoryService.listAppChatHistoryByPage(chatHistoryQueryRequest, request);
        return ResultUtils.success(result);
    }

    /**
     * 管理员分页查询所有对话历史
     *
     * @param chatHistoryQueryRequest 查询请求
     * @return 对话历史分页
     */
    @PostMapping("/admin/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<ChatHistoryVO>> listAllChatHistoryByPageForAdmin(@RequestBody ChatHistoryQueryRequest chatHistoryQueryRequest,HttpServletRequest request) {
        ThrowUtils.throwIf(chatHistoryQueryRequest == null, ErrorCode.PARAMS_ERROR);
        Page<ChatHistoryVO> result = chatHistoryService.listAllChatHistoryByPageForAdmin(chatHistoryQueryRequest, request);
        return ResultUtils.success(result);
    }


}
