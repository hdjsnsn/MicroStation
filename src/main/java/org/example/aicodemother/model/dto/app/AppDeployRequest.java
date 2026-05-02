package org.example.aicodemother.model.dto.app;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.example.aicodemother.Utils.ToLongDeserializer;

import java.io.Serial;
import java.io.Serializable;

/**
 * 应用部署请求
 */
@Data
public class AppDeployRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 应用ID
     */
    @Schema(type = "string")
    @JsonDeserialize(using = ToLongDeserializer.class)
    private Long Appid;

}
