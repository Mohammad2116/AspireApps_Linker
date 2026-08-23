package ir.aspireapps.linker.common.dto;

import lombok.Builder;

import java.time.Instant;
import java.util.Map;

@Builder
public record ApiExceptionInfo(
        Instant timestamp,
        int status,
        String code,
        String error,
        String message,
        String path,
        Map<String, String> errors
) {
}
