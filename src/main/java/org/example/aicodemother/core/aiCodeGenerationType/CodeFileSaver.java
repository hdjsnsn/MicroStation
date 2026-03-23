package org.example.aicodemother.core.aiCodeGenerationType;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import org.example.aicodemother.ai.model.HtmlCodeResult;
import org.example.aicodemother.ai.model.MultiFileCodeResult;
import org.example.aicodemother.model.enums.CodeGenTypeEnum;

import java.io.File;

/**
 *  文件保存器
 */
public class CodeFileSaver {

    /**
     * 文件保存的目录
     */
    private final static String FILE_SAVE_ROOT_DIR = System.getProperty("user.dir") + "/tmp/code_output";

    /**
     * 保存HTML网页代码
     *
      * @param htmlCodeResult HTML 代码文件类型
     * @return 保存的文件夹
     */
    public static File saveHTMLCodeResult(HtmlCodeResult htmlCodeResult) {
        String baseDirPath = buildUniqueDir(CodeGenTypeEnum.HTML.getValue());
        writeToFile("index.html",baseDirPath,htmlCodeResult.getHtmlCode());
        return new File(baseDirPath);
    }


    /**
     * 保存多文件代码
     *
      * @param multiFileCodeResult 多文件类型
     * @return 保存的文件夹
     */
    public static File saveMultiFileCodeResult(MultiFileCodeResult multiFileCodeResult) {
        String baseDirPath = buildUniqueDir(CodeGenTypeEnum.HTML.getValue());
        writeToFile("index.html",baseDirPath,multiFileCodeResult.getHtmlCode());
        writeToFile("style.css",baseDirPath,multiFileCodeResult.getCssCode());
        writeToFile("script.js",baseDirPath,multiFileCodeResult.getJsCode());
        return new File(baseDirPath);
    }


    /**
     * 构建文件的唯一路径： tmp/code_output/bizType_雪花ID
     *
     * @param bizType 代码生成类型
     * @return
     */
    private static String buildUniqueDir(String bizType){
        String uniqueDir = FILE_SAVE_ROOT_DIR + File.separator + bizType + '_' + IdUtil.getSnowflakeNextIdStr();
        FileUtil.mkdir(uniqueDir);
        return uniqueDir;
    }

    /**
     *   保存单个文件
     */
    private static void writeToFile(String fileName,String dirPath , String content) {
        String filePath = dirPath + File.separator + fileName;
        FileUtil.writeUtf8String(content, filePath);
    }
}
