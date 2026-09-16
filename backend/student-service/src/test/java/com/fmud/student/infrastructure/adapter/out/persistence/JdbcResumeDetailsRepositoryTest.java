package com.fmud.student.infrastructure.adapter.out.persistence;

import com.fmud.student.application.dto.ResumeDetailsDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class JdbcResumeDetailsRepositoryTest {
    @Autowired
    private JdbcResumeDetailsRepository repository;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void savesAndReadsNormalizedResumeDetails() {
        UUID studentId = UUID.randomUUID();
        jdbc.update("""
                insert into students.students
                    (id, first_name, last_name, document_number, birth_date, status, created_at, updated_at)
                values (?, ?, ?, ?, ?, ?, ?, ?)
                """, studentId, "Ana", "Perez", "12345678", LocalDate.of(2012, 1, 1), "ACTIVE", Instant.now(), Instant.now());

        repository.save(studentId, new ResumeDetailsDto(
                new ResumeDetailsDto.PersonalInfoDto("Cedula de ciudadania", "Bogota", "Cundinamarca", "Soltero(a)", true, 1, "Maria", "+57300"),
                new ResumeDetailsDto.SocioeconomicInfoDto("Familia", 4, "Madre", "1000000", "Familiar", "EPS", "Subsidiado", false, null, null, null, true, false),
                new ResumeDetailsDto.AcademicInfoDto("Bachiller", "Colegio Central", 2025, "Tecnico", null, "Primeros auxilios", true, true),
                new ResumeDetailsDto.MotivationInfoDto("Beca", "Programa", "Metas", "Impacto", "Aprendizaje", "Aplicacion"),
                new ResumeDetailsDto.AvailabilityInfoDto(true, false, null, true, true, true),
                new ResumeDetailsDto.FoundationKnowledgeInfoDto(List.of("Redes sociales", "Pagina web"), false, "Labor social"),
                new ResumeDetailsDto.AuthorizationsInfoDto(true, true, true),
                new ResumeDetailsDto.HealthInfoDto(false, null, false, null, false, null, false, null, true, "Polen", false, null, false, null, false, null, true, List.of("Tetanos", "Influenza"), false, true, false, false, null, true, "EPS", "Maria", "Madre", "+57301"),
                new ResumeDetailsDto.RiskFactorsInfoDto("No", "Ocasionalmente", false, null, false, false, false, true, true, false, null),
                new ResumeDetailsDto.AcademicPerformanceInfoDto("Matematicas", List.of("Matematicas"), List.of("Investigacion", "Trabajo en equipo"), true, "Olimpiadas", "Ingles", "Bueno", "Entre 5 y 10", "Cumple", "Graduacion", "Lectura"),
                new ResumeDetailsDto.ProgramKnowledgeInfoDto("Cuidado", "Adulto mayor", "Proceso", "Diferencia", "Vocacion", true, "Familia", "Paciencia", "Respeto", "Acompanamiento", "Ayudar", "Clave", "Empatia", "Practica", "Comunidad", "Hospital", "Trato digno", true, "Tiempo", "Dialoga", "Compromiso"),
                new ResumeDetailsDto.InstitutionalCommitmentInfoDto(true, "La conozco", List.of("Familiar o amigo"), "Apoyo", true, true, true, true, true, true, true, true),
                new ResumeDetailsDto.DeclarationInfoDto(true, "Ana Perez", "12345678", LocalDate.of(2026, 9, 9))
        ));

        ResumeDetailsDto found = repository.findByStudentId(studentId);

        assertThat(found.personal().hasChildren()).isTrue();
        assertThat(found.socioeconomic().familyMembersCount()).isEqualTo(4);
        assertThat(found.foundationKnowledge().callSource()).containsExactly("Pagina web", "Redes sociales");
        assertThat(found.health().vaccines()).containsExactly("Influenza", "Tetanos");
        assertThat(found.academicPerformance().academicSkills()).containsExactly("Investigacion", "Trabajo en equipo");
        assertThat(found.declaration().signatureDate()).isEqualTo(LocalDate.of(2026, 9, 9));
    }
}
