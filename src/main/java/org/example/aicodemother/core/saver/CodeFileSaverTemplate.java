package org.example.aicodemother.core.saver;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import org.example.aicodemother.constant.AppConstant;
import org.example.aicodemother.exception.BusinessException;
import org.example.aicodemother.exception.ErrorCode;
import org.example.aicodemother.model.enums.CodeGenTypeEnum;

import java.io.File;

public abstract class CodeFileSaverTemplate<T> {

    /**
     * 模板方法：保存代码的标准流程
     *
     * @param appID 应用ID
     * @param result 代码结果对象
     * @return 保存的目录
     */
    public final File saveCode(T result, Long appID) {
        // 1.验证输入
        validateInput(result);
        // 2. 构建唯一目录
        String baseDirPath = buildUniqueDir(appID);
        // 3. 保存文件(具体实现由子类提供)
        saveFiles(result, baseDirPath);
        // 4。 返回文件目录对象
        return new File(baseDirPath);
    }

    /**
     *  验证输入参数(可由子类覆盖)
     */
    protected void validateInput(T result) {
        if(result == null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "代码结果不能为空");
        }
    }

    /**
     * 构建文件的唯一路径： tmp/code_output/bizType_雪花ID
     *
     * @param appID 应用ID
     * @return 目录路径
     */
    private String buildUniqueDir(Long appID){
        if (appID == null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用ID不能为空");
        }
        String codeType = getCodeType().getValue();
        String uniqueDir = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + codeType + '_' + appID;
        FileUtil.mkdir(uniqueDir);
        return uniqueDir;
    }

    /**
     *   保存单个文件
     */
    public final void writeToFile(String dirPath ,String fileName ,String content) {
        if(StrUtil.isNotBlank(content)){
            String filePath = dirPath + File.separator + fileName;
            FileUtil.writeUtf8String(content, filePath);
        }
    }

    /**
     * 获取代码生成类型(由子类实现)
     *
     * @return 代码生成类型
     */
    protected abstract CodeGenTypeEnum getCodeType();

    /**
     *  保存文件(由子类实现)
     *
     *  @param result 代码结果对象
     *  @param baseDirPath 基础目录路径
     */
    protected abstract void saveFiles(T result, String baseDirPath);

}
