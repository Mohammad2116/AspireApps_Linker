package ir.aspireapps.linker.analysisservice.service;

import ir.aspireapps.linker.analysisservice.model.AnalyzeData;
import ir.aspireapps.linker.analysisservice.repository.AnalysisRepository;
import ir.aspireapps.linker.common.dto.AnalysisResponse;
import ir.aspireapps.linker.common.model.HitState;
import ir.aspireapps.linker.common.payload.LinkClickedPayload;
import ir.aspireapps.linker.common.payload.LinkRegisteredPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AnalysisService {
    private final AnalysisRepository analysisRepository;

    @Transactional
    public void register(LinkRegisteredPayload request) {
        if (analysisRepository.existsByShortedUrl(request.shortedUrl()))
        ////// TODO: send kafka error
            throw new RuntimeException("duplicate shorted url");
        AnalyzeData analyzeData = AnalyzeData.builder()
                .shortedUrl(request.shortedUrl())
                .build();
        analyzeData = analysisRepository.save(analyzeData);
        AnalysisResponse.builder()
                .shortedUrl(analyzeData.getShortedUrl())
                .linkHitState(analyzeData.getLinkHitState())
                .build();
    }

    @Transactional
    public void clicked(LinkClickedPayload payload) {
        if (analysisRepository.existsByShortedUrl(payload.shortedUrl())) {
            AnalyzeData analyzeData = analysisRepository.findByShortedUrl(payload.shortedUrl());
            analyzeData.incClickCount();
            HitState currentState = payload.currentHitState();
            HitState newState = calculateNewState(analyzeData.getHitCount());

            if (Duration.between(
                    analyzeData.getCounterResetAt(),
                    Instant.now()).toSeconds() >= 60) {
                analyzeData.setHitCount(0);
                analyzeData.setCounterResetAt(Instant.now());
                analyzeData.setLinkHitState(newState);
                //////////// TODO: send update message to links-service using kafka
                return;
            } else if (hitStateImproved(currentState, newState)) {
                analyzeData.setLinkHitState(newState);
                return;
                ////////// TODO: send update message to links-service using kafka
            }
        } else {
            return;
            ////////////// TODO: send an error message to kafka error listener
        }
    }

    private boolean hitStateImproved(HitState currentState, HitState newSate) {
        if (currentState.equals(HitState.VERY_HIGH)) return false;

        if (currentState.equals(HitState.LOW) && (!newSate.equals(HitState.LOW)))
            return true;

        if (currentState.equals(HitState.NORMAL) && (newSate.equals(HitState.HIGH) || newSate.equals(HitState.VERY_HIGH)))
            return true;

        return currentState.equals(HitState.HIGH) && (newSate.equals(HitState.VERY_HIGH));
    }

    private HitState calculateNewState(long hitCount) {
        HitState hitState = HitState.LOW;
        if (hitCount > 100) hitState = HitState.VERY_HIGH;
        else if (hitCount > 50) hitState = HitState.HIGH;
        else if (hitCount > 25) hitState = HitState.NORMAL;
        return hitState;
    }
}
