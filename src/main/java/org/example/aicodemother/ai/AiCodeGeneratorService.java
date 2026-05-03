package org.example.aicodemother.ai;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import org.example.aicodemother.ai.model.HtmlCodeResult;
import org.example.aicodemother.ai.model.MultiFileCodeResult;
import reactor.core.publisher.Flux;

public interface AiCodeGeneratorService {

    /**
     * 生成HTML代码
     *
     * @param userMessage 用户提示词
     * @return AI的输出结果
     */
    @SystemMessage(fromResource = "prompt/codegen-html-system-prompt.md")
    HtmlCodeResult generateHTMLCode(String userMessage);

    /**
     * 生成多文件代码
     *
     * @param userMessage 用户提示词
     * @return AI的输出结果
     */
    @SystemMessage(fromResource = "prompt/codegen-multi-file-system-prompt.md")
    MultiFileCodeResult generateMultiFileCode(String userMessage);

    /**
     * 生成HTML代码 (反应式编程)
     *
     * @param userMessage 用户提示词
     * @return AI的输出结果流
     */
    @SystemMessage(fromResource = "prompt/codegen-html-system-prompt.md")
    Flux<String> generateHTMLCodeStream(String userMessage);

    /**
     * 生成多文件代码 (反应式编程)
     *
     * @param userMessage 用户提示词
     * @return AI的输出结果流
     */
    @SystemMessage(fromResource = "prompt/codegen-multi-file-system-prompt.md")
    Flux<String> generateMultiFileCodeStream(String userMessage);

    /**
     * 生成vue代码 (反应式编程)
     *
     * @param userMessage 用户提示词
     * @return AI的输出结果流
     */
    @SystemMessage(fromResource = "prompt/codegen-vue-system-prompt.md")
    Flux<String> generateVueCodeStream(@UserMessage String userMessage, @MemoryId Long appId);
}
