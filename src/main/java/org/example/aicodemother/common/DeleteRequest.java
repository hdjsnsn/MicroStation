package org.example.aicodemother.common;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.example.aicodemother.Utils.ToLongDeserializer;

import java.io.Serial;
import java.io.Serializable;

@Data
public class DeleteRequest implements Serializable {

    /**
     * id
     */
    @Schema(type = "string")
    @JsonDeserialize(using = ToLongDeserializer.class)
    private Long id;

    @Serial
    private static final long serialVersionUID = 1L;
}
