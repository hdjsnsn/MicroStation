package org.example.aicodemother.core;

import org.example.aicodemother.ai.AiCodeGeneratorService;
import org.example.aicodemother.ai.model.HtmlCodeResult;
import org.example.aicodemother.ai.model.MultiFileCodeResult;
import org.example.aicodemother.core.aiCodeGenerationType.CodeFileSaver;
import org.example.aicodemother.exception.BusinessException;
import org.example.aicodemother.exception.ErrorCode;
import org.example.aicodemother.model.enums.CodeGenTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * 代码生成门面类
 */
@Service
public class AiCodeGeneratorFacade {

    @Autowired
    private AiCodeGeneratorService aiCodeGeneratorService;

    /**
     * AI代码生成门面类
     *
     * @param userMessage 用户提示词
     * @param codeGenTypeEnum 生成类型
     * @return 保存的目录
     */
    public File generateAndSaveAiCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "生成类型为空");
        }
        return switch (codeGenTypeEnum) {
            case HTML -> generateAndSaveHtmlCode(userMessage);
            case MULTI_FILE -> generateAndSaveMultiFile(userMessage);
            default -> {
                String errorMessage = "不支持生成类型" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.PARAMS_ERROR, errorMessage);
            }
        };
    }

    /**
     *  生成HTML模式的代码并保存
     *
     * @param userMessage 用户提示词
     * @return  保存的目录
     */
    private File generateAndSaveHtmlCode(String userMessage) {
        HtmlCodeResult htmlCodeResult = aiCodeGeneratorService.generateHTMLCode(userMessage);
        return CodeFileSaver.saveHTMLCodeResult(htmlCodeResult);
    }

    /**
     *  生成多文件模式的代码并保存
     *
     * @param userMessage 用户提示词
     * @return  保存的目录
     */
    private File generateAndSaveMultiFile(String userMessage) {
        MultiFileCodeResult multiFileCodeResult = aiCodeGeneratorService.generateMultiFileCode(userMessage);
        return CodeFileSaver.saveMultiFileCodeResult(multiFileCodeResult);
    }
}
