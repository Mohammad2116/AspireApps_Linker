package ir.aspireapps.linker.analysisservice.controller;

import ir.aspireapps.linker.analysisservice.service.AnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("ir/aspireapps/linker/analysis/api/v1")
@RequiredArgsConstructor
public class AnalysisController {
    private final AnalysisService analysisService;

}
