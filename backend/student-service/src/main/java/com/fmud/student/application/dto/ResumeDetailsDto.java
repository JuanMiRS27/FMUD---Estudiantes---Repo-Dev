package com.fmud.student.application.dto;

import java.util.Map;

public record ResumeDetailsDto(
        Map<String, Object> personal,
        Map<String, Object> socioeconomic,
        Map<String, Object> academic,
        Map<String, Object> motivation,
        Map<String, Object> availability,
        Map<String, Object> foundationKnowledge,
        Map<String, Object> authorizations,
        Map<String, Object> health,
        Map<String, Object> riskFactors,
        Map<String, Object> academicPerformance,
        Map<String, Object> programKnowledge,
        Map<String, Object> institutionalCommitment,
        Map<String, Object> declaration
) {
}
