package com.fmud.student.infrastructure.adapter.out.persistence;

import com.fmud.student.application.dto.ResumeDetailsDto;
import com.fmud.student.application.port.out.ResumeDetailsRepositoryPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Component
public class JdbcResumeDetailsRepository implements ResumeDetailsRepositoryPort {
    private final JdbcTemplate jdbc;

    public JdbcResumeDetailsRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public ResumeDetailsDto findByStudentId(UUID studentId) {
        return new ResumeDetailsDto(
                personal(studentId),
                socioeconomic(studentId),
                academic(studentId),
                motivation(studentId),
                availability(studentId),
                foundationKnowledge(studentId),
                authorizations(studentId),
                health(studentId),
                riskFactors(studentId),
                academicPerformance(studentId),
                programKnowledge(studentId),
                institutionalCommitment(studentId),
                declaration(studentId)
        );
    }

    @Override
    @Transactional
    public void save(UUID studentId, ResumeDetailsDto details) {
        ResumeDetailsDto safe = details == null ? empty() : details;
        savePersonal(studentId, safe.personal());
        saveSocioeconomic(studentId, safe.socioeconomic());
        saveAcademic(studentId, safe.academic());
        saveMotivation(studentId, safe.motivation());
        saveAvailability(studentId, safe.availability());
        saveFoundationKnowledge(studentId, safe.foundationKnowledge());
        saveAuthorizations(studentId, safe.authorizations());
        saveHealth(studentId, safe.health());
        saveRiskFactors(studentId, safe.riskFactors());
        saveAcademicPerformance(studentId, safe.academicPerformance());
        saveProgramKnowledge(studentId, safe.programKnowledge());
        saveInstitutionalCommitment(studentId, safe.institutionalCommitment());
        saveDeclaration(studentId, safe.declaration());
    }

    private ResumeDetailsDto empty() {
        return new ResumeDetailsDto(null, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    private ResumeDetailsDto.PersonalInfoDto personal(UUID studentId) {
        return one("select * from students.resume_personal_info where student_id = ?", rs -> new ResumeDetailsDto.PersonalInfoDto(
                text(rs, "document_type"), text(rs, "municipality"), text(rs, "department"), text(rs, "civil_status"),
                bool(rs, "has_children"), integer(rs, "children_count"), text(rs, "emergency_contact_name"), text(rs, "emergency_contact_phone")
        ), emptyPersonal(), studentId);
    }

    private ResumeDetailsDto.SocioeconomicInfoDto socioeconomic(UUID studentId) {
        return one("select * from students.resume_socioeconomic_info where student_id = ?", rs -> new ResumeDetailsDto.SocioeconomicInfoDto(
                text(rs, "lives_with"), integer(rs, "family_members_count"), text(rs, "main_household_provider"),
                text(rs, "monthly_family_income"), text(rs, "housing_type"), text(rs, "health_system_affiliation"),
                text(rs, "health_regime"), bool(rs, "currently_works"), text(rs, "company"), text(rs, "position"),
                text(rs, "work_schedule"), bool(rs, "unemployed_last_six_months"), bool(rs, "receives_government_aid")
        ), emptySocioeconomic(), studentId);
    }

    private ResumeDetailsDto.AcademicInfoDto academic(UUID studentId) {
        return one("select * from students.resume_academic_info where student_id = ?", rs -> new ResumeDetailsDto.AcademicInfoDto(
                text(rs, "last_approved_level"), text(rs, "institution"), integer(rs, "graduation_year"),
                text(rs, "higher_studies"), text(rs, "previous_scholarships"), text(rs, "complementary_certificates"),
                bool(rs, "internet_access"), bool(rs, "study_device_availability")
        ), emptyAcademic(), studentId);
    }

    private ResumeDetailsDto.MotivationInfoDto motivation(UUID studentId) {
        return one("select * from students.resume_motivation_info where student_id = ?", rs -> new ResumeDetailsDto.MotivationInfoDto(
                text(rs, "scholarship_reason"), text(rs, "program_motivation"), text(rs, "personal_professional_goals"),
                text(rs, "quality_of_life_impact"), text(rs, "expected_learning"), text(rs, "knowledge_application_plan")
        ), emptyMotivation(), studentId);
    }

    private ResumeDetailsDto.AvailabilityInfoDto availability(UUID studentId) {
        return one("select * from students.resume_availability_info where student_id = ?", rs -> new ResumeDetailsDto.AvailabilityInfoDto(
                bool(rs, "available_for_classes"), bool(rs, "time_limitations"), text(rs, "time_limitations_description"),
                bool(rs, "accepts_institution_rules"), bool(rs, "participates_community_activities"), bool(rs, "attendance_commitment")
        ), emptyAvailability(), studentId);
    }

    private ResumeDetailsDto.FoundationKnowledgeInfoDto foundationKnowledge(UUID studentId) {
        return one("select * from students.resume_foundation_knowledge_info where student_id = ?", rs -> new ResumeDetailsDto.FoundationKnowledgeInfoDto(
                options("resume_foundation_call_sources", studentId), bool(rs, "knew_foundation_before"), text(rs, "known_social_work")
        ), new ResumeDetailsDto.FoundationKnowledgeInfoDto(List.of(), null, null), studentId);
    }

    private ResumeDetailsDto.AuthorizationsInfoDto authorizations(UUID studentId) {
        return one("select * from students.resume_authorizations_info where student_id = ?", rs -> new ResumeDetailsDto.AuthorizationsInfoDto(
                bool(rs, "personal_data_processing"), bool(rs, "information_verification"), bool(rs, "photo_video_use")
        ), emptyAuthorizations(), studentId);
    }

    private ResumeDetailsDto.HealthInfoDto health(UUID studentId) {
        return one("select * from students.resume_health_info where student_id = ?", rs -> new ResumeDetailsDto.HealthInfoDto(
                bool(rs, "diagnosed_disease"), text(rs, "diagnosed_disease_name"), bool(rs, "chronic_disease"), text(rs, "chronic_disease_name"),
                bool(rs, "physical_limitation"), text(rs, "physical_limitation_description"), bool(rs, "disability"), text(rs, "disability_description"),
                bool(rs, "allergies"), text(rs, "allergies_specification"), bool(rs, "current_medical_treatment"),
                text(rs, "current_medical_treatment_name"), bool(rs, "permanent_medication"), text(rs, "permanent_medication_names"),
                bool(rs, "hospitalized_last_two_years"), text(rs, "hospitalization_reason"), bool(rs, "complete_vaccination"),
                options("resume_health_vaccines", studentId), bool(rs, "visual_difficulties"), bool(rs, "uses_glasses"),
                bool(rs, "hearing_difficulties"), bool(rs, "accidents_with_sequelae"), text(rs, "accident_explanation"),
                bool(rs, "active_health_affiliation"), text(rs, "eps"), text(rs, "medical_emergency_contact_name"),
                text(rs, "medical_emergency_contact_relationship"), text(rs, "medical_emergency_contact_phone")
        ), emptyHealth(), studentId);
    }

    private ResumeDetailsDto.RiskFactorsInfoDto riskFactors(UUID studentId) {
        return one("select * from students.resume_risk_factors_info where student_id = ?", rs -> new ResumeDetailsDto.RiskFactorsInfoDto(
                text(rs, "tobacco_use"), text(rs, "alcohol_use"), bool(rs, "psychoactive_substances_use"), text(rs, "substance_use_time"),
                bool(rs, "consumption_affected_performance"), bool(rs, "prevention_program_participation"),
                bool(rs, "current_professional_support"), bool(rs, "prevention_guidance_interest"),
                bool(rs, "healthy_habits_activities_willingness"), bool(rs, "personal_family_situation_affects_process"),
                text(rs, "personal_family_situation_description")
        ), emptyRiskFactors(), studentId);
    }

    private ResumeDetailsDto.AcademicPerformanceInfoDto academicPerformance(UUID studentId) {
        return one("select * from students.resume_academic_performance_info where student_id = ?", rs -> new ResumeDetailsDto.AcademicPerformanceInfoDto(
                text(rs, "best_subject"), options("resume_academic_strength_areas", studentId),
                options("resume_academic_skills", studentId), bool(rs, "academic_recognitions"),
                text(rs, "academic_recognitions_details"), text(rs, "subjects_to_strengthen"), text(rs, "study_habits"),
                text(rs, "weekly_study_hours"), text(rs, "task_responsibility"), text(rs, "main_academic_achievement"),
                text(rs, "areas_to_strengthen")
        ), emptyAcademicPerformance(), studentId);
    }

    private ResumeDetailsDto.ProgramKnowledgeInfoDto programKnowledge(UUID studentId) {
        return one("select * from students.resume_program_knowledge_info where student_id = ?", rs -> new ResumeDetailsDto.ProgramKnowledgeInfoDto(
                text(rs, "nursing_understanding"), text(rs, "geriatrics_understanding"), text(rs, "gerontology_understanding"),
                text(rs, "geriatrics_gerontology_difference"), text(rs, "study_reason"), bool(rs, "care_experience"),
                text(rs, "care_experience_description"), text(rs, "elder_care_qualities"), text(rs, "human_dignity_meaning"),
                text(rs, "sad_older_adult_action"), text(rs, "needs_help_no_staff_action"), text(rs, "patience_importance"),
                text(rs, "health_values"), text(rs, "program_expected_learning"), text(rs, "community_contribution"),
                text(rs, "desired_workplace"), text(rs, "humanized_service_meaning"), bool(rs, "practice_responsibility"),
                text(rs, "main_training_challenge"), text(rs, "refuses_help_reaction"), text(rs, "scholarship_merit_reason")
        ), emptyProgramKnowledge(), studentId);
    }

    private ResumeDetailsDto.InstitutionalCommitmentInfoDto institutionalCommitment(UUID studentId) {
        return one("select * from students.resume_institutional_commitment_info where student_id = ?", rs -> new ResumeDetailsDto.InstitutionalCommitmentInfoDto(
                bool(rs, "foundation_social_work_knowledge"), text(rs, "foundation_social_work_description"),
                options("resume_institutional_call_sources", studentId), text(rs, "foundation_program_reason"),
                bool(rs, "understands_scholarship_responsibility"), bool(rs, "student_rules_commitment"),
                bool(rs, "respectful_conduct_commitment"), bool(rs, "punctual_attendance_commitment"),
                bool(rs, "social_community_participation"), bool(rs, "resource_care_commitment"),
                bool(rs, "understands_non_compliance_consequences"), bool(rs, "tracking_authorization")
        ), emptyInstitutionalCommitment(), studentId);
    }

    private ResumeDetailsDto.DeclarationInfoDto declaration(UUID studentId) {
        return one("select * from students.resume_declaration_info where student_id = ?", rs -> new ResumeDetailsDto.DeclarationInfoDto(
                bool(rs, "truthful_complete_information"), text(rs, "applicant_name"), text(rs, "identity_document"),
                text(rs, "signature_management_space"), date(rs, "signature_date")
        ), emptyDeclaration(), studentId);
    }

    private void savePersonal(UUID studentId, ResumeDetailsDto.PersonalInfoDto value) {
        var v = value == null ? emptyPersonal() : value;
        upsert("resume_personal_info", "document_type, municipality, department, civil_status, has_children, children_count, emergency_contact_name, emergency_contact_phone",
                "?, ?, ?, ?, ?, ?, ?, ?", "document_type = excluded.document_type, municipality = excluded.municipality, department = excluded.department, civil_status = excluded.civil_status, has_children = excluded.has_children, children_count = excluded.children_count, emergency_contact_name = excluded.emergency_contact_name, emergency_contact_phone = excluded.emergency_contact_phone",
                studentId, clean(v.documentType()), clean(v.municipality()), clean(v.department()), clean(v.civilStatus()), v.hasChildren(), v.childrenCount(), clean(v.emergencyContactName()), clean(v.emergencyContactPhone()));
    }

    private void saveSocioeconomic(UUID studentId, ResumeDetailsDto.SocioeconomicInfoDto value) {
        var v = value == null ? emptySocioeconomic() : value;
        upsert("resume_socioeconomic_info", "lives_with, family_members_count, main_household_provider, monthly_family_income, housing_type, health_system_affiliation, health_regime, currently_works, company, position, work_schedule, unemployed_last_six_months, receives_government_aid",
                "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?", "lives_with = excluded.lives_with, family_members_count = excluded.family_members_count, main_household_provider = excluded.main_household_provider, monthly_family_income = excluded.monthly_family_income, housing_type = excluded.housing_type, health_system_affiliation = excluded.health_system_affiliation, health_regime = excluded.health_regime, currently_works = excluded.currently_works, company = excluded.company, position = excluded.position, work_schedule = excluded.work_schedule, unemployed_last_six_months = excluded.unemployed_last_six_months, receives_government_aid = excluded.receives_government_aid",
                studentId, clean(v.livesWith()), v.familyMembersCount(), clean(v.mainHouseholdProvider()), clean(v.monthlyFamilyIncome()), clean(v.housingType()), clean(v.healthSystemAffiliation()), clean(v.healthRegime()), v.currentlyWorks(), clean(v.company()), clean(v.position()), clean(v.workSchedule()), v.unemployedLastSixMonths(), v.receivesGovernmentAid());
    }

    private void saveAcademic(UUID studentId, ResumeDetailsDto.AcademicInfoDto value) {
        var v = value == null ? emptyAcademic() : value;
        upsert("resume_academic_info", "last_approved_level, institution, graduation_year, higher_studies, previous_scholarships, complementary_certificates, internet_access, study_device_availability",
                "?, ?, ?, ?, ?, ?, ?, ?", "last_approved_level = excluded.last_approved_level, institution = excluded.institution, graduation_year = excluded.graduation_year, higher_studies = excluded.higher_studies, previous_scholarships = excluded.previous_scholarships, complementary_certificates = excluded.complementary_certificates, internet_access = excluded.internet_access, study_device_availability = excluded.study_device_availability",
                studentId, clean(v.lastApprovedLevel()), clean(v.institution()), v.graduationYear(), clean(v.higherStudies()), clean(v.previousScholarships()), clean(v.complementaryCertificates()), v.internetAccess(), v.studyDeviceAvailability());
    }

    private void saveMotivation(UUID studentId, ResumeDetailsDto.MotivationInfoDto value) {
        var v = value == null ? emptyMotivation() : value;
        upsert("resume_motivation_info", "scholarship_reason, program_motivation, personal_professional_goals, quality_of_life_impact, expected_learning, knowledge_application_plan",
                "?, ?, ?, ?, ?, ?", "scholarship_reason = excluded.scholarship_reason, program_motivation = excluded.program_motivation, personal_professional_goals = excluded.personal_professional_goals, quality_of_life_impact = excluded.quality_of_life_impact, expected_learning = excluded.expected_learning, knowledge_application_plan = excluded.knowledge_application_plan",
                studentId, clean(v.scholarshipReason()), clean(v.programMotivation()), clean(v.personalProfessionalGoals()), clean(v.qualityOfLifeImpact()), clean(v.expectedLearning()), clean(v.knowledgeApplicationPlan()));
    }

    private void saveAvailability(UUID studentId, ResumeDetailsDto.AvailabilityInfoDto value) {
        var v = value == null ? emptyAvailability() : value;
        upsert("resume_availability_info", "available_for_classes, time_limitations, time_limitations_description, accepts_institution_rules, participates_community_activities, attendance_commitment",
                "?, ?, ?, ?, ?, ?", "available_for_classes = excluded.available_for_classes, time_limitations = excluded.time_limitations, time_limitations_description = excluded.time_limitations_description, accepts_institution_rules = excluded.accepts_institution_rules, participates_community_activities = excluded.participates_community_activities, attendance_commitment = excluded.attendance_commitment",
                studentId, v.availableForClasses(), v.timeLimitations(), clean(v.timeLimitationsDescription()), v.acceptsInstitutionRules(), v.participatesCommunityActivities(), v.attendanceCommitment());
    }

    private void saveFoundationKnowledge(UUID studentId, ResumeDetailsDto.FoundationKnowledgeInfoDto value) {
        var v = value == null ? new ResumeDetailsDto.FoundationKnowledgeInfoDto(List.of(), null, null) : value;
        upsert("resume_foundation_knowledge_info", "knew_foundation_before, known_social_work", "?, ?",
                "knew_foundation_before = excluded.knew_foundation_before, known_social_work = excluded.known_social_work",
                studentId, v.knewFoundationBefore(), clean(v.knownSocialWork()));
        replaceOptions("resume_foundation_call_sources", studentId, v.callSource());
    }

    private void saveAuthorizations(UUID studentId, ResumeDetailsDto.AuthorizationsInfoDto value) {
        var v = value == null ? emptyAuthorizations() : value;
        upsert("resume_authorizations_info", "personal_data_processing, information_verification, photo_video_use", "?, ?, ?",
                "personal_data_processing = excluded.personal_data_processing, information_verification = excluded.information_verification, photo_video_use = excluded.photo_video_use",
                studentId, v.personalDataProcessing(), v.informationVerification(), v.photoVideoUse());
    }

    private void saveHealth(UUID studentId, ResumeDetailsDto.HealthInfoDto value) {
        var v = value == null ? emptyHealth() : value;
        upsert("resume_health_info", "diagnosed_disease, diagnosed_disease_name, chronic_disease, chronic_disease_name, physical_limitation, physical_limitation_description, disability, disability_description, allergies, allergies_specification, current_medical_treatment, current_medical_treatment_name, permanent_medication, permanent_medication_names, hospitalized_last_two_years, hospitalization_reason, complete_vaccination, visual_difficulties, uses_glasses, hearing_difficulties, accidents_with_sequelae, accident_explanation, active_health_affiliation, eps, medical_emergency_contact_name, medical_emergency_contact_relationship, medical_emergency_contact_phone",
                "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?",
                "diagnosed_disease = excluded.diagnosed_disease, diagnosed_disease_name = excluded.diagnosed_disease_name, chronic_disease = excluded.chronic_disease, chronic_disease_name = excluded.chronic_disease_name, physical_limitation = excluded.physical_limitation, physical_limitation_description = excluded.physical_limitation_description, disability = excluded.disability, disability_description = excluded.disability_description, allergies = excluded.allergies, allergies_specification = excluded.allergies_specification, current_medical_treatment = excluded.current_medical_treatment, current_medical_treatment_name = excluded.current_medical_treatment_name, permanent_medication = excluded.permanent_medication, permanent_medication_names = excluded.permanent_medication_names, hospitalized_last_two_years = excluded.hospitalized_last_two_years, hospitalization_reason = excluded.hospitalization_reason, complete_vaccination = excluded.complete_vaccination, visual_difficulties = excluded.visual_difficulties, uses_glasses = excluded.uses_glasses, hearing_difficulties = excluded.hearing_difficulties, accidents_with_sequelae = excluded.accidents_with_sequelae, accident_explanation = excluded.accident_explanation, active_health_affiliation = excluded.active_health_affiliation, eps = excluded.eps, medical_emergency_contact_name = excluded.medical_emergency_contact_name, medical_emergency_contact_relationship = excluded.medical_emergency_contact_relationship, medical_emergency_contact_phone = excluded.medical_emergency_contact_phone",
                studentId, v.diagnosedDisease(), clean(v.diagnosedDiseaseName()), v.chronicDisease(), clean(v.chronicDiseaseName()), v.physicalLimitation(), clean(v.physicalLimitationDescription()), v.disability(), clean(v.disabilityDescription()), v.allergies(), clean(v.allergiesSpecification()), v.currentMedicalTreatment(), clean(v.currentMedicalTreatmentName()), v.permanentMedication(), clean(v.permanentMedicationNames()), v.hospitalizedLastTwoYears(), clean(v.hospitalizationReason()), v.completeVaccination(), v.visualDifficulties(), v.usesGlasses(), v.hearingDifficulties(), v.accidentsWithSequelae(), clean(v.accidentExplanation()), v.activeHealthAffiliation(), clean(v.eps()), clean(v.medicalEmergencyContactName()), clean(v.medicalEmergencyContactRelationship()), clean(v.medicalEmergencyContactPhone()));
        replaceOptions("resume_health_vaccines", studentId, v.vaccines());
    }

    private void saveRiskFactors(UUID studentId, ResumeDetailsDto.RiskFactorsInfoDto value) {
        var v = value == null ? emptyRiskFactors() : value;
        upsert("resume_risk_factors_info", "tobacco_use, alcohol_use, psychoactive_substances_use, substance_use_time, consumption_affected_performance, prevention_program_participation, current_professional_support, prevention_guidance_interest, healthy_habits_activities_willingness, personal_family_situation_affects_process, personal_family_situation_description",
                "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?", "tobacco_use = excluded.tobacco_use, alcohol_use = excluded.alcohol_use, psychoactive_substances_use = excluded.psychoactive_substances_use, substance_use_time = excluded.substance_use_time, consumption_affected_performance = excluded.consumption_affected_performance, prevention_program_participation = excluded.prevention_program_participation, current_professional_support = excluded.current_professional_support, prevention_guidance_interest = excluded.prevention_guidance_interest, healthy_habits_activities_willingness = excluded.healthy_habits_activities_willingness, personal_family_situation_affects_process = excluded.personal_family_situation_affects_process, personal_family_situation_description = excluded.personal_family_situation_description",
                studentId, clean(v.tobaccoUse()), clean(v.alcoholUse()), v.psychoactiveSubstancesUse(), clean(v.substanceUseTime()), v.consumptionAffectedPerformance(), v.preventionProgramParticipation(), v.currentProfessionalSupport(), v.preventionGuidanceInterest(), v.healthyHabitsActivitiesWillingness(), v.personalFamilySituationAffectsProcess(), clean(v.personalFamilySituationDescription()));
    }

    private void saveAcademicPerformance(UUID studentId, ResumeDetailsDto.AcademicPerformanceInfoDto value) {
        var v = value == null ? emptyAcademicPerformance() : value;
        upsert("resume_academic_performance_info", "best_subject, academic_recognitions, academic_recognitions_details, subjects_to_strengthen, study_habits, weekly_study_hours, task_responsibility, main_academic_achievement, areas_to_strengthen",
                "?, ?, ?, ?, ?, ?, ?, ?, ?", "best_subject = excluded.best_subject, academic_recognitions = excluded.academic_recognitions, academic_recognitions_details = excluded.academic_recognitions_details, subjects_to_strengthen = excluded.subjects_to_strengthen, study_habits = excluded.study_habits, weekly_study_hours = excluded.weekly_study_hours, task_responsibility = excluded.task_responsibility, main_academic_achievement = excluded.main_academic_achievement, areas_to_strengthen = excluded.areas_to_strengthen",
                studentId, clean(v.bestSubject()), v.academicRecognitions(), clean(v.academicRecognitionsDetails()), clean(v.subjectsToStrengthen()), clean(v.studyHabits()), clean(v.weeklyStudyHours()), clean(v.taskResponsibility()), clean(v.mainAcademicAchievement()), clean(v.areasToStrengthen()));
        replaceOptions("resume_academic_strength_areas", studentId, v.strengthAreas());
        replaceOptions("resume_academic_skills", studentId, v.academicSkills());
    }

    private void saveProgramKnowledge(UUID studentId, ResumeDetailsDto.ProgramKnowledgeInfoDto value) {
        var v = value == null ? emptyProgramKnowledge() : value;
        upsert("resume_program_knowledge_info", "nursing_understanding, geriatrics_understanding, gerontology_understanding, geriatrics_gerontology_difference, study_reason, care_experience, care_experience_description, elder_care_qualities, human_dignity_meaning, sad_older_adult_action, needs_help_no_staff_action, patience_importance, health_values, program_expected_learning, community_contribution, desired_workplace, humanized_service_meaning, practice_responsibility, main_training_challenge, refuses_help_reaction, scholarship_merit_reason",
                "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?", "nursing_understanding = excluded.nursing_understanding, geriatrics_understanding = excluded.geriatrics_understanding, gerontology_understanding = excluded.gerontology_understanding, geriatrics_gerontology_difference = excluded.geriatrics_gerontology_difference, study_reason = excluded.study_reason, care_experience = excluded.care_experience, care_experience_description = excluded.care_experience_description, elder_care_qualities = excluded.elder_care_qualities, human_dignity_meaning = excluded.human_dignity_meaning, sad_older_adult_action = excluded.sad_older_adult_action, needs_help_no_staff_action = excluded.needs_help_no_staff_action, patience_importance = excluded.patience_importance, health_values = excluded.health_values, program_expected_learning = excluded.program_expected_learning, community_contribution = excluded.community_contribution, desired_workplace = excluded.desired_workplace, humanized_service_meaning = excluded.humanized_service_meaning, practice_responsibility = excluded.practice_responsibility, main_training_challenge = excluded.main_training_challenge, refuses_help_reaction = excluded.refuses_help_reaction, scholarship_merit_reason = excluded.scholarship_merit_reason",
                studentId, clean(v.nursingUnderstanding()), clean(v.geriatricsUnderstanding()), clean(v.gerontologyUnderstanding()), clean(v.geriatricsGerontologyDifference()), clean(v.studyReason()), v.careExperience(), clean(v.careExperienceDescription()), clean(v.elderCareQualities()), clean(v.humanDignityMeaning()), clean(v.sadOlderAdultAction()), clean(v.needsHelpNoStaffAction()), clean(v.patienceImportance()), clean(v.healthValues()), clean(v.programExpectedLearning()), clean(v.communityContribution()), clean(v.desiredWorkplace()), clean(v.humanizedServiceMeaning()), v.practiceResponsibility(), clean(v.mainTrainingChallenge()), clean(v.refusesHelpReaction()), clean(v.scholarshipMeritReason()));
    }

    private void saveInstitutionalCommitment(UUID studentId, ResumeDetailsDto.InstitutionalCommitmentInfoDto value) {
        var v = value == null ? emptyInstitutionalCommitment() : value;
        upsert("resume_institutional_commitment_info", "foundation_social_work_knowledge, foundation_social_work_description, foundation_program_reason, understands_scholarship_responsibility, student_rules_commitment, respectful_conduct_commitment, punctual_attendance_commitment, social_community_participation, resource_care_commitment, understands_non_compliance_consequences, tracking_authorization",
                "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?", "foundation_social_work_knowledge = excluded.foundation_social_work_knowledge, foundation_social_work_description = excluded.foundation_social_work_description, foundation_program_reason = excluded.foundation_program_reason, understands_scholarship_responsibility = excluded.understands_scholarship_responsibility, student_rules_commitment = excluded.student_rules_commitment, respectful_conduct_commitment = excluded.respectful_conduct_commitment, punctual_attendance_commitment = excluded.punctual_attendance_commitment, social_community_participation = excluded.social_community_participation, resource_care_commitment = excluded.resource_care_commitment, understands_non_compliance_consequences = excluded.understands_non_compliance_consequences, tracking_authorization = excluded.tracking_authorization",
                studentId, v.foundationSocialWorkKnowledge(), clean(v.foundationSocialWorkDescription()), clean(v.foundationProgramReason()), v.understandsScholarshipResponsibility(), v.studentRulesCommitment(), v.respectfulConductCommitment(), v.punctualAttendanceCommitment(), v.socialCommunityParticipation(), v.resourceCareCommitment(), v.understandsNonComplianceConsequences(), v.trackingAuthorization());
        replaceOptions("resume_institutional_call_sources", studentId, v.callSource());
    }

    private void saveDeclaration(UUID studentId, ResumeDetailsDto.DeclarationInfoDto value) {
        var v = value == null ? emptyDeclaration() : value;
        upsert("resume_declaration_info", "truthful_complete_information, applicant_name, identity_document, signature_management_space, signature_date",
                "?, ?, ?, ?, ?", "truthful_complete_information = excluded.truthful_complete_information, applicant_name = excluded.applicant_name, identity_document = excluded.identity_document, signature_management_space = excluded.signature_management_space, signature_date = excluded.signature_date",
                studentId, v.truthfulCompleteInformation(), clean(v.applicantName()), clean(v.identityDocument()), clean(v.signatureManagementSpace()), v.signatureDate());
    }

    private void upsert(String table, String columns, String placeholders, String updates, UUID studentId, Object... values) {
        Object[] args = new Object[values.length + 1];
        args[0] = studentId;
        System.arraycopy(values, 0, args, 1, values.length);
        jdbc.update("delete from students." + table + " where student_id = ?", studentId);
        jdbc.update("insert into students.%s (student_id, %s, updated_at) values (?, %s, now())".formatted(table, columns, placeholders), args);
    }

    private void replaceOptions(String table, UUID studentId, List<String> values) {
        jdbc.update("delete from students." + table + " where student_id = ?", studentId);
        for (String value : values == null ? List.<String>of() : values) {
            String option = clean(value);
            if (option != null) {
                jdbc.update("insert into students." + table + " (student_id, option_value) values (?, ?) on conflict do nothing", studentId, option);
            }
        }
    }

    private List<String> options(String table, UUID studentId) {
        return jdbc.queryForList("select option_value from students." + table + " where student_id = ? order by option_value", String.class, studentId);
    }

    private <T> T one(String sql, RowMapper<T> mapper, T empty, UUID studentId) {
        List<T> rows = jdbc.query(sql, (rs, rowNum) -> mapper.map(rs), studentId);
        return rows.isEmpty() ? empty : rows.getFirst();
    }

    private String text(ResultSet rs, String column) throws SQLException {
        return rs.getString(column);
    }

    private Boolean bool(ResultSet rs, String column) throws SQLException {
        boolean value = rs.getBoolean(column);
        return rs.wasNull() ? null : value;
    }

    private Integer integer(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }

    private LocalDate date(ResultSet rs, String column) throws SQLException {
        var value = rs.getDate(column);
        return value == null ? null : value.toLocalDate();
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private ResumeDetailsDto.PersonalInfoDto emptyPersonal() {
        return new ResumeDetailsDto.PersonalInfoDto(null, null, null, null, null, null, null, null);
    }

    private ResumeDetailsDto.SocioeconomicInfoDto emptySocioeconomic() {
        return new ResumeDetailsDto.SocioeconomicInfoDto(null, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    private ResumeDetailsDto.AcademicInfoDto emptyAcademic() {
        return new ResumeDetailsDto.AcademicInfoDto(null, null, null, null, null, null, null, null);
    }

    private ResumeDetailsDto.MotivationInfoDto emptyMotivation() {
        return new ResumeDetailsDto.MotivationInfoDto(null, null, null, null, null, null);
    }

    private ResumeDetailsDto.AvailabilityInfoDto emptyAvailability() {
        return new ResumeDetailsDto.AvailabilityInfoDto(null, null, null, null, null, null);
    }

    private ResumeDetailsDto.AuthorizationsInfoDto emptyAuthorizations() {
        return new ResumeDetailsDto.AuthorizationsInfoDto(null, null, null);
    }

    private ResumeDetailsDto.HealthInfoDto emptyHealth() {
        return new ResumeDetailsDto.HealthInfoDto(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, List.of(), null, null, null, null, null, null, null, null, null, null);
    }

    private ResumeDetailsDto.RiskFactorsInfoDto emptyRiskFactors() {
        return new ResumeDetailsDto.RiskFactorsInfoDto(null, null, null, null, null, null, null, null, null, null, null);
    }

    private ResumeDetailsDto.AcademicPerformanceInfoDto emptyAcademicPerformance() {
        return new ResumeDetailsDto.AcademicPerformanceInfoDto(null, List.of(), List.of(), null, null, null, null, null, null, null, null);
    }

    private ResumeDetailsDto.ProgramKnowledgeInfoDto emptyProgramKnowledge() {
        return new ResumeDetailsDto.ProgramKnowledgeInfoDto(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    private ResumeDetailsDto.InstitutionalCommitmentInfoDto emptyInstitutionalCommitment() {
        return new ResumeDetailsDto.InstitutionalCommitmentInfoDto(null, null, List.of(), null, null, null, null, null, null, null, null, null);
    }

    private ResumeDetailsDto.DeclarationInfoDto emptyDeclaration() {
        return new ResumeDetailsDto.DeclarationInfoDto(null, null, null, null, null);
    }

    @FunctionalInterface
    private interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }
}
