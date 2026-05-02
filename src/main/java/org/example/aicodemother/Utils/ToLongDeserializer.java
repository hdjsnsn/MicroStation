package org.example.aicodemother.Utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.example.aicodemother.exception.BusinessException;
import org.example.aicodemother.exception.ErrorCode;

import java.io.IOException;

public class ToLongDeserializer extends JsonDeserializer<Long> {
    @Override
    public Long deserialize(JsonParser p, DeserializationContext context) throws IOException {
        String value = p.getText();
        try {
            return value != null ? Long.parseLong(value) : null;
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数格式错误");
        }
    }
}
