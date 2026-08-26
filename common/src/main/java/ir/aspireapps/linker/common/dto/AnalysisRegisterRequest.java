package ir.aspireapps.linker.common.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record AnalysisRegisterRequest(
        @NotEmpty @Size(min = 4, max = 10)
        String shorted_url
) {
}

