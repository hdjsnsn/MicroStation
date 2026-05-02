package org.example.aicodemother.constant;

/**
 * 应用常量类
 */
public class AppConstant {

    /**
     * 精选应用的优先级
     */
    public static final Integer GOOD_APP_PRIORITY = 99;

    /**
     * 默认应用优先级
     */
    public static final Integer DEFAULT_APP_PRIORITY = 0;

    /**
     * 应用生成目录
     */
    public static final String CODE_OUTPUT_ROOT_DIR = System.getProperty("user.dir") + "/tmp/code_output";

    /**
     * 应用部署目录
     */
    public static final String CODE_DEPLOY_ROOT_DIR = System.getProperty("user.dir") + "/tmp/code_deploy";

    /**
     * 应用部署域名
     */
    public static final String CODE_DEPLOY_HOST = "http://localhost";

}
