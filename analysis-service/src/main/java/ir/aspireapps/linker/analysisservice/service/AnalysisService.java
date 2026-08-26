package ir.aspireapps.linker.analysisservice.service;

import ir.aspireapps.linker.analysisservice.model.AnalyzeData;
import ir.aspireapps.linker.analysisservice.repository.AnalysisRepository;
import ir.aspireapps.linker.common.dto.AnalysisRegisterRequest;
import ir.aspireapps.linker.common.dto.AnalysisResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AnalysisService {
    private final AnalysisRepository analysisRepository;

    @Transactional
    public AnalysisResponse register(AnalysisRegisterRequest request) {
        if (analysisRepository.existsByShortedUrl(request.shorted_url()))
            throw new RuntimeException("duplicate shorted url");
        AnalyzeData analyzeData = AnalyzeData.builder()
                .shorted_url(request.shorted_url())
                .build();
        analyzeData = analysisRepository.save(analyzeData);
        return AnalysisResponse.builder()
                .shortedUrl(analyzeData.getShorted_url())
                .linkHitState(analyzeData.getLinkHitState())
                .build();
    }
}
