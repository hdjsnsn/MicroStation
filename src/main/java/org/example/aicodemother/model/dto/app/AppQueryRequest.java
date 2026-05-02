package org.example.aicodemother.model.dto.app;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.aicodemother.Utils.ToLongDeserializer;
import org.example.aicodemother.common.PageRequest;

import java.io.Serial;
import java.io.Serializable;

/**
 * 应用查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AppQueryRequest extends PageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @Schema(type = "string")
    @JsonDeserialize(using = ToLongDeserializer.class)
    private Long id;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 创建用户id
     */
    @Schema(type = "string")
    @JsonDeserialize(using = ToLongDeserializer.class)
    private Long userId;

    /**
     * 代码生成类型
     */
    private String codeGenType;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 部署标识
     */
    private String deployKey;

}
