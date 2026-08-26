package ir.aspireapps.linker.analysisservice.controller;

import ir.aspireapps.linker.analysisservice.service.AnalysisService;
import ir.aspireapps.linker.common.dto.AnalysisRegisterRequest;
import ir.aspireapps.linker.common.dto.AnalysisResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("ir/aspireapps/linker/analysis/api/v1")
@RequiredArgsConstructor
public class AnalysisController {
    private final AnalysisService analysisService;

    public ResponseEntity<AnalysisResponse> register(
            @Valid @RequestBody AnalysisRegisterRequest request) {

        return ResponseEntity.
                status(HttpStatus.OK)
                .body(analysisService.register(request));
    }
}
