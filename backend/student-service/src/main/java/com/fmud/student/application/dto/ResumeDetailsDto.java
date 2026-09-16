package com.fmud.student.application.dto;

import java.time.LocalDate;
import java.util.List;

public record ResumeDetailsDto(
        PersonalInfoDto personal,
        SocioeconomicInfoDto socioeconomic,
        AcademicInfoDto academic,
        MotivationInfoDto motivation,
        AvailabilityInfoDto availability,
        FoundationKnowledgeInfoDto foundationKnowledge,
        AuthorizationsInfoDto authorizations,
        HealthInfoDto health,
        RiskFactorsInfoDto riskFactors,
        AcademicPerformanceInfoDto academicPerformance,
        ProgramKnowledgeInfoDto programKnowledge,
        InstitutionalCommitmentInfoDto institutionalCommitment,
        DeclarationInfoDto declaration
) {
    public record PersonalInfoDto(
            String documentType,
            String municipality,
            String department,
            String civilStatus,
            Boolean hasChildren,
            Integer childrenCount,
            String emergencyContactName,
            String emergencyContactPhone
    ) {
    }

    public record SocioeconomicInfoDto(
            String livesWith,
            Integer familyMembersCount,
            String mainHouseholdProvider,
            String monthlyFamilyIncome,
            String housingType,
            String healthSystemAffiliation,
            String healthRegime,
            Boolean currentlyWorks,
            String company,
            String position,
            String workSchedule,
            Boolean unemployedLastSixMonths,
            Boolean receivesGovernmentAid
    ) {
    }

    public record AcademicInfoDto(
            String lastApprovedLevel,
            String institution,
            Integer graduationYear,
            String higherStudies,
            String previousScholarships,
            String complementaryCertificates,
            Boolean internetAccess,
            Boolean studyDeviceAvailability
    ) {
    }

    public record MotivationInfoDto(
            String scholarshipReason,
            String programMotivation,
            String personalProfessionalGoals,
            String qualityOfLifeImpact,
            String expectedLearning,
            String knowledgeApplicationPlan
    ) {
    }

    public record AvailabilityInfoDto(
            Boolean availableForClasses,
            Boolean timeLimitations,
            String timeLimitationsDescription,
            Boolean acceptsInstitutionRules,
            Boolean participatesCommunityActivities,
            Boolean attendanceCommitment
    ) {
    }

    public record FoundationKnowledgeInfoDto(
            List<String> callSource,
            Boolean knewFoundationBefore,
            String knownSocialWork
    ) {
    }

    public record AuthorizationsInfoDto(
            Boolean personalDataProcessing,
            Boolean informationVerification,
            Boolean photoVideoUse
    ) {
    }

    public record HealthInfoDto(
            Boolean diagnosedDisease,
            String diagnosedDiseaseName,
            Boolean chronicDisease,
            String chronicDiseaseName,
            Boolean physicalLimitation,
            String physicalLimitationDescription,
            Boolean disability,
            String disabilityDescription,
            Boolean allergies,
            String allergiesSpecification,
            Boolean currentMedicalTreatment,
            String currentMedicalTreatmentName,
            Boolean permanentMedication,
            String permanentMedicationNames,
            Boolean hospitalizedLastTwoYears,
            String hospitalizationReason,
            Boolean completeVaccination,
            List<String> vaccines,
            Boolean visualDifficulties,
            Boolean usesGlasses,
            Boolean hearingDifficulties,
            Boolean accidentsWithSequelae,
            String accidentExplanation,
            Boolean activeHealthAffiliation,
            String eps,
            String medicalEmergencyContactName,
            String medicalEmergencyContactRelationship,
            String medicalEmergencyContactPhone
    ) {
    }

    public record RiskFactorsInfoDto(
            String tobaccoUse,
            String alcoholUse,
            Boolean psychoactiveSubstancesUse,
            String substanceUseTime,
            Boolean consumptionAffectedPerformance,
            Boolean preventionProgramParticipation,
            Boolean currentProfessionalSupport,
            Boolean preventionGuidanceInterest,
            Boolean healthyHabitsActivitiesWillingness,
            Boolean personalFamilySituationAffectsProcess,
            String personalFamilySituationDescription
    ) {
    }

    public record AcademicPerformanceInfoDto(
            String bestSubject,
            List<String> strengthAreas,
            List<String> academicSkills,
            Boolean academicRecognitions,
            String academicRecognitionsDetails,
            String subjectsToStrengthen,
            String studyHabits,
            String weeklyStudyHours,
            String taskResponsibility,
            String mainAcademicAchievement,
            String areasToStrengthen
    ) {
    }

    public record ProgramKnowledgeInfoDto(
            String nursingUnderstanding,
            String geriatricsUnderstanding,
            String gerontologyUnderstanding,
            String geriatricsGerontologyDifference,
            String studyReason,
            Boolean careExperience,
            String careExperienceDescription,
            String elderCareQualities,
            String humanDignityMeaning,
            String sadOlderAdultAction,
            String needsHelpNoStaffAction,
            String patienceImportance,
            String healthValues,
            String programExpectedLearning,
            String communityContribution,
            String desiredWorkplace,
            String humanizedServiceMeaning,
            Boolean practiceResponsibility,
            String mainTrainingChallenge,
            String refusesHelpReaction,
            String scholarshipMeritReason
    ) {
    }

    public record InstitutionalCommitmentInfoDto(
            Boolean foundationSocialWorkKnowledge,
            String foundationSocialWorkDescription,
            List<String> callSource,
            String foundationProgramReason,
            Boolean understandsScholarshipResponsibility,
            Boolean studentRulesCommitment,
            Boolean respectfulConductCommitment,
            Boolean punctualAttendanceCommitment,
            Boolean socialCommunityParticipation,
            Boolean resourceCareCommitment,
            Boolean understandsNonComplianceConsequences,
            Boolean trackingAuthorization
    ) {
    }

    public record DeclarationInfoDto(
            Boolean truthfulCompleteInformation,
            String applicantName,
            String identityDocument,
            LocalDate signatureDate
    ) {
    }
}
