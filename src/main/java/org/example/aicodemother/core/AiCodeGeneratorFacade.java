package org.example.aicodemother.core;

import cn.hutool.json.JSONUtil;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.ToolExecution;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.aicodemother.ai.AiCodeGeneratorService;
import org.example.aicodemother.ai.AiCodeGeneratorServiceFactory;
import org.example.aicodemother.ai.model.HtmlCodeResult;
import org.example.aicodemother.ai.model.MultiFileCodeResult;
import org.example.aicodemother.ai.model.message.AiResponseMessage;
import org.example.aicodemother.ai.model.message.ToolExecutedMessage;
import org.example.aicodemother.ai.model.message.ToolRequestMessage;
import org.example.aicodemother.core.parser.CodeParserExecutor;
import org.example.aicodemother.core.saver.CodeFileSaverExecutor;
import org.example.aicodemother.exception.BusinessException;
import org.example.aicodemother.exception.ErrorCode;
import org.example.aicodemother.model.enums.CodeGenTypeEnum;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;

/**
 * 代码生成门面类
 */
@Service
@Slf4j
public class AiCodeGeneratorFacade {

    @Resource
    private AiCodeGeneratorServiceFactory aiCodeGeneratorServiceFactory;

    /**
     * 统一入口: 根据生成类型生成代码并保存
     *
     * @param userMessage 用户提示词
     * @param codeGenTypeEnum 生成类型
     * @param appID 应用ID
     * @return 保存的目录
     */
    public File generateAndSaveAiCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appID) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "生成类型为空");
        }
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appID, codeGenTypeEnum);
        return switch (codeGenTypeEnum) {
            // 生成HTML模式的代码并保存
            case HTML -> {
                HtmlCodeResult result = aiCodeGeneratorService.generateHTMLCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(result, CodeGenTypeEnum.HTML, appID);
            }
            // 生成多文件模式的代码并保存
            case MULTI_FILE -> {
                MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(result, CodeGenTypeEnum.MULTI_FILE, appID);
            }
            default -> {
                String errorMessage = "不支持生成类型" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.PARAMS_ERROR, errorMessage);
            }
        };
    }

    /**
     * 统一入口: 根据生成类型生成代码并保存 (流式)
     *
     * @param userMessage 用户提示词
     * @param codeGenTypeEnum 生成类型
     * @param appID 应用ID
     * @return 保存的目录
     */
    public Flux<String> generateAndSaveAiCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appID) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "生成类型为空");
        }
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appID, codeGenTypeEnum);
        return switch (codeGenTypeEnum) {
            // 生成HTML模式的代码并保存(流式)
            case HTML -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateHTMLCodeStream(userMessage);
                yield processCodeStream(codeStream, CodeGenTypeEnum.HTML, appID);
            }
            // 生成多文件模式的代码并保存(流式)
            case MULTI_FILE -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
                yield processCodeStream(codeStream, CodeGenTypeEnum.MULTI_FILE, appID);
            }
            case VUE -> {
                TokenStream codeStream = aiCodeGeneratorService.generateVueCodeStream(userMessage, appID);
                yield processTokenStream(codeStream);
            }
            default -> {
                String errorMessage = "不支持生成类型" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.PARAMS_ERROR, errorMessage);
            }
        };
    }

    /**
     * 将 TokenStream 转换为 Flux<String>，并传递工具调用信息
     *
     * @param tokenStream TokenStream 对象
     * @return Flux<String> 流式响应
     */
    private Flux<String> processTokenStream(TokenStream tokenStream) {
        return Flux.create(sink -> {
            tokenStream.onPartialResponse((String partialResponse) -> {
                        AiResponseMessage aiResponseMessage = new AiResponseMessage(partialResponse);
                        sink.next(JSONUtil.toJsonStr(aiResponseMessage));
                    })
                    .onPartialToolExecutionRequest((index, toolExecutionRequest) -> {
                        ToolRequestMessage toolRequestMessage = new ToolRequestMessage(toolExecutionRequest);
                        sink.next(JSONUtil.toJsonStr(toolRequestMessage));
                    })
                    .onToolExecuted((ToolExecution toolExecution) -> {
                        ToolExecutedMessage toolExecutedMessage = new ToolExecutedMessage(toolExecution);
                        sink.next(JSONUtil.toJsonStr(toolExecutedMessage));
                    })
                    .onCompleteResponse((ChatResponse response) -> {
                        sink.complete();
                    })
                    .onError((Throwable error) -> {
                        error.fillInStackTrace();
                        sink.error(error);
                    })
                    .start();
        });
    }


    /**
     * 通用流式代码处理方法
     *
     * @param codeStream  代码流
     * @param codeGenType 代码生成类型
     * @param appID 应用ID
     * @return 流式响应
     */
    private Flux<String> processCodeStream(Flux<String> codeStream, CodeGenTypeEnum codeGenType, Long appID) {
        StringBuilder codeBuilder = new StringBuilder();
        // 实时收集代码片段
        return codeStream.doOnNext(codeBuilder::append).doOnComplete(() -> {
            // 流式返回完成后保存代码
            try {
                String completeCode = codeBuilder.toString();
                // 使用执行器解析代码
                Object parsedResult = CodeParserExecutor.executeParser(completeCode, codeGenType);
                // 使用执行器保存代码
                File savedDir = CodeFileSaverExecutor.executeSaver(parsedResult, codeGenType, appID);
                log.info("保存成功，路径为：{}", savedDir.getAbsolutePath());
            } catch (Exception e) {
                log.error("保存失败: {}", e.getMessage());
            }
        });
    }

}
