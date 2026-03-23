package org.example.aicodemother.ai;

import dev.langchain4j.service.SystemMessage;
import org.example.aicodemother.ai.model.HtmlCodeResult;
import org.example.aicodemother.ai.model.MultiFileCodeResult;

public interface AiCodeGeneratorService {

    /**
     * 生成HTML代码
     *
     * @param userMessage 用户提示词
     * @return AI的输出结果
     */
    @SystemMessage(fromResource = "prompt/codegen-html-system-prompt.txt")
    HtmlCodeResult generateHTMLCode(String userMessage);

    /**
     * 生成多文件代码
     *
     * @param userMessage 用户提示词
     * @return AI的输出结果
     */
    @SystemMessage(fromResource = "prompt/codegen-multi-file-system-prompt.txt")
    MultiFileCodeResult generateMultiFileCode(String userMessage);
}
