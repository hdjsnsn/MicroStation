package org.example.aicodemother.model.dto.app;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.example.aicodemother.Utils.ToLongDeserializer;

import java.io.Serial;
import java.io.Serializable;

/**
 * 应用更新请求（管理员）
 */
@Data
public class AppAdminUpdateRequest implements Serializable {

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
     * 应用封面
     */
    private String cover;

    /**
     * 优先级
     */
    private Integer priority;

}
