package com.fmud.student.infrastructure.adapter.out.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fmud.student.application.dto.ResumeDetailsDto;
import com.fmud.student.application.port.out.ResumeDetailsRepositoryPort;
import com.fmud.student.infrastructure.exception.BadRequestException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class JdbcResumeDetailsRepository implements ResumeDetailsRepositoryPort {
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper = new ObjectMapper();

    public JdbcResumeDetailsRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public ResumeDetailsDto findByStudentId(UUID studentId) {
        return new ResumeDetailsDto(
                read("resume_personal_info", studentId),
                read("resume_socioeconomic_info", studentId),
                read("resume_academic_info", studentId),
                read("resume_motivation_info", studentId),
                read("resume_availability_info", studentId),
                read("resume_foundation_knowledge_info", studentId),
                read("resume_authorizations_info", studentId),
                read("resume_health_info", studentId),
                read("resume_risk_factors_info", studentId),
                read("resume_academic_performance_info", studentId),
                read("resume_program_knowledge_info", studentId),
                read("resume_institutional_commitment_info", studentId),
                read("resume_declaration_info", studentId)
        );
    }

    @Override
    public void save(UUID studentId, ResumeDetailsDto details) {
        upsert("resume_personal_info", studentId, details.personal());
        upsert("resume_socioeconomic_info", studentId, details.socioeconomic());
        upsert("resume_academic_info", studentId, details.academic());
        upsert("resume_motivation_info", studentId, details.motivation());
        upsert("resume_availability_info", studentId, details.availability());
        upsert("resume_foundation_knowledge_info", studentId, details.foundationKnowledge());
        upsert("resume_authorizations_info", studentId, details.authorizations());
        upsert("resume_health_info", studentId, details.health());
        upsert("resume_risk_factors_info", studentId, details.riskFactors());
        upsert("resume_academic_performance_info", studentId, details.academicPerformance());
        upsert("resume_program_knowledge_info", studentId, details.programKnowledge());
        upsert("resume_institutional_commitment_info", studentId, details.institutionalCommitment());
        upsert("resume_declaration_info", studentId, details.declaration());
    }

    private Map<String, Object> read(String table, UUID studentId) {
        String sql = "select payload from students." + table + " where student_id = ?";
        return jdbc.query(sql, rs -> rs.next() ? fromJson(rs.getString("payload")) : new LinkedHashMap<>(), studentId);
    }

    private void upsert(String table, UUID studentId, Map<String, Object> payload) {
        String json = toJson(payload == null ? Map.of() : payload);
        String sql = """
                insert into students.%s (student_id, payload, updated_at)
                values (?, ?, now())
                on conflict (student_id)
                do update set payload = excluded.payload, updated_at = now()
                """.formatted(table);
        jdbc.update(sql, studentId, json);
    }

    private Map<String, Object> fromJson(String json) {
        try {
            return json == null || json.isBlank() ? new LinkedHashMap<>() : mapper.readValue(json, MAP_TYPE);
        } catch (Exception ex) {
            throw new BadRequestException("No fue posible leer el detalle de la hoja de vida.");
        }
    }

    private String toJson(Map<String, Object> payload) {
        try {
            return mapper.writeValueAsString(payload);
        } catch (Exception ex) {
            throw new BadRequestException("No fue posible guardar el detalle de la hoja de vida.");
        }
    }
}
