CREATE TABLE IF NOT EXISTS students.resume_foundation_call_sources (
    student_id UUID NOT NULL REFERENCES students.students(id) ON DELETE RESTRICT,
    option_value VARCHAR(80) NOT NULL,
    PRIMARY KEY (student_id, option_value)
);

CREATE TABLE IF NOT EXISTS students.resume_health_vaccines (
    student_id UUID NOT NULL REFERENCES students.students(id) ON DELETE RESTRICT,
    option_value VARCHAR(80) NOT NULL,
    PRIMARY KEY (student_id, option_value)
);

CREATE TABLE IF NOT EXISTS students.resume_academic_strength_areas (
    student_id UUID NOT NULL REFERENCES students.students(id) ON DELETE RESTRICT,
    option_value VARCHAR(80) NOT NULL,
    PRIMARY KEY (student_id, option_value)
);

CREATE TABLE IF NOT EXISTS students.resume_academic_skills (
    student_id UUID NOT NULL REFERENCES students.students(id) ON DELETE RESTRICT,
    option_value VARCHAR(120) NOT NULL,
    PRIMARY KEY (student_id, option_value)
);

CREATE TABLE IF NOT EXISTS students.resume_institutional_call_sources (
    student_id UUID NOT NULL REFERENCES students.students(id) ON DELETE RESTRICT,
    option_value VARCHAR(80) NOT NULL,
    PRIMARY KEY (student_id, option_value)
);

DO $$
DECLARE
    invalid_count BIGINT;
BEGIN
    SELECT count(*) INTO invalid_count
    FROM students.resume_personal_info r, jsonb_object_keys(r.payload::jsonb) AS keys(key)
    WHERE key NOT IN ('documentType', 'municipality', 'department', 'civilStatus', 'hasChildren', 'childrenCount', 'emergencyContactName', 'emergencyContactPhone');
    IF invalid_count > 0 THEN
        RAISE EXCEPTION 'Cannot drop resume_personal_info.payload: % unsupported JSON keys found', invalid_count;
    END IF;

    SELECT count(*) INTO invalid_count
    FROM students.resume_socioeconomic_info r, jsonb_object_keys(r.payload::jsonb) AS keys(key)
    WHERE key NOT IN ('livesWith', 'familyMembersCount', 'mainHouseholdProvider', 'monthlyFamilyIncome', 'housingType', 'healthSystemAffiliation', 'healthRegime', 'currentlyWorks', 'company', 'position', 'workSchedule', 'unemployedLastSixMonths', 'receivesGovernmentAid');
    IF invalid_count > 0 THEN
        RAISE EXCEPTION 'Cannot drop resume_socioeconomic_info.payload: % unsupported JSON keys found', invalid_count;
    END IF;

    SELECT count(*) INTO invalid_count
    FROM students.resume_academic_info r, jsonb_object_keys(r.payload::jsonb) AS keys(key)
    WHERE key NOT IN ('lastApprovedLevel', 'institution', 'graduationYear', 'higherStudies', 'previousScholarships', 'complementaryCertificates', 'internetAccess', 'studyDeviceAvailability');
    IF invalid_count > 0 THEN
        RAISE EXCEPTION 'Cannot drop resume_academic_info.payload: % unsupported JSON keys found', invalid_count;
    END IF;

    SELECT count(*) INTO invalid_count
    FROM students.resume_motivation_info r, jsonb_object_keys(r.payload::jsonb) AS keys(key)
    WHERE key NOT IN ('scholarshipReason', 'programMotivation', 'personalProfessionalGoals', 'qualityOfLifeImpact', 'expectedLearning', 'knowledgeApplicationPlan');
    IF invalid_count > 0 THEN
        RAISE EXCEPTION 'Cannot drop resume_motivation_info.payload: % unsupported JSON keys found', invalid_count;
    END IF;

    SELECT count(*) INTO invalid_count
    FROM students.resume_availability_info r, jsonb_object_keys(r.payload::jsonb) AS keys(key)
    WHERE key NOT IN ('availableForClasses', 'timeLimitations', 'timeLimitationsDescription', 'acceptsInstitutionRules', 'participatesCommunityActivities', 'attendanceCommitment');
    IF invalid_count > 0 THEN
        RAISE EXCEPTION 'Cannot drop resume_availability_info.payload: % unsupported JSON keys found', invalid_count;
    END IF;

    SELECT count(*) INTO invalid_count
    FROM students.resume_foundation_knowledge_info r, jsonb_object_keys(r.payload::jsonb) AS keys(key)
    WHERE key NOT IN ('callSource', 'knewFoundationBefore', 'knownSocialWork');
    IF invalid_count > 0 THEN
        RAISE EXCEPTION 'Cannot drop resume_foundation_knowledge_info.payload: % unsupported JSON keys found', invalid_count;
    END IF;

    SELECT count(*) INTO invalid_count
    FROM students.resume_authorizations_info r, jsonb_object_keys(r.payload::jsonb) AS keys(key)
    WHERE key NOT IN ('personalDataProcessing', 'informationVerification', 'photoVideoUse');
    IF invalid_count > 0 THEN
        RAISE EXCEPTION 'Cannot drop resume_authorizations_info.payload: % unsupported JSON keys found', invalid_count;
    END IF;

    SELECT count(*) INTO invalid_count
    FROM students.resume_health_info r, jsonb_object_keys(r.payload::jsonb) AS keys(key)
    WHERE key NOT IN ('diagnosedDisease', 'diagnosedDiseaseName', 'chronicDisease', 'chronicDiseaseName', 'physicalLimitation', 'physicalLimitationDescription', 'disability', 'disabilityDescription', 'allergies', 'allergiesSpecification', 'currentMedicalTreatment', 'currentMedicalTreatmentName', 'permanentMedication', 'permanentMedicationNames', 'hospitalizedLastTwoYears', 'hospitalizationReason', 'completeVaccination', 'vaccines', 'visualDifficulties', 'usesGlasses', 'hearingDifficulties', 'accidentsWithSequelae', 'accidentExplanation', 'activeHealthAffiliation', 'eps', 'medicalEmergencyContactName', 'medicalEmergencyContactRelationship', 'medicalEmergencyContactPhone');
    IF invalid_count > 0 THEN
        RAISE EXCEPTION 'Cannot drop resume_health_info.payload: % unsupported JSON keys found', invalid_count;
    END IF;

    SELECT count(*) INTO invalid_count
    FROM students.resume_risk_factors_info r, jsonb_object_keys(r.payload::jsonb) AS keys(key)
    WHERE key NOT IN ('tobaccoUse', 'alcoholUse', 'psychoactiveSubstancesUse', 'substanceUseTime', 'consumptionAffectedPerformance', 'preventionProgramParticipation', 'currentProfessionalSupport', 'preventionGuidanceInterest', 'healthyHabitsActivitiesWillingness', 'personalFamilySituationAffectsProcess', 'personalFamilySituationDescription');
    IF invalid_count > 0 THEN
        RAISE EXCEPTION 'Cannot drop resume_risk_factors_info.payload: % unsupported JSON keys found', invalid_count;
    END IF;

    SELECT count(*) INTO invalid_count
    FROM students.resume_academic_performance_info r, jsonb_object_keys(r.payload::jsonb) AS keys(key)
    WHERE key NOT IN ('bestSubject', 'strengthAreas', 'academicSkills', 'academicRecognitions', 'academicRecognitionsDetails', 'subjectsToStrengthen', 'studyHabits', 'weeklyStudyHours', 'taskResponsibility', 'mainAcademicAchievement', 'areasToStrengthen');
    IF invalid_count > 0 THEN
        RAISE EXCEPTION 'Cannot drop resume_academic_performance_info.payload: % unsupported JSON keys found', invalid_count;
    END IF;

    SELECT count(*) INTO invalid_count
    FROM students.resume_program_knowledge_info r, jsonb_object_keys(r.payload::jsonb) AS keys(key)
    WHERE key NOT IN ('nursingUnderstanding', 'geriatricsUnderstanding', 'gerontologyUnderstanding', 'geriatricsGerontologyDifference', 'studyReason', 'careExperience', 'careExperienceDescription', 'elderCareQualities', 'humanDignityMeaning', 'sadOlderAdultAction', 'needsHelpNoStaffAction', 'patienceImportance', 'healthValues', 'programExpectedLearning', 'communityContribution', 'desiredWorkplace', 'humanizedServiceMeaning', 'practiceResponsibility', 'mainTrainingChallenge', 'refusesHelpReaction', 'scholarshipMeritReason');
    IF invalid_count > 0 THEN
        RAISE EXCEPTION 'Cannot drop resume_program_knowledge_info.payload: % unsupported JSON keys found', invalid_count;
    END IF;

    SELECT count(*) INTO invalid_count
    FROM students.resume_institutional_commitment_info r, jsonb_object_keys(r.payload::jsonb) AS keys(key)
    WHERE key NOT IN ('foundationSocialWorkKnowledge', 'foundationSocialWorkDescription', 'callSource', 'foundationProgramReason', 'understandsScholarshipResponsibility', 'studentRulesCommitment', 'respectfulConductCommitment', 'punctualAttendanceCommitment', 'socialCommunityParticipation', 'resourceCareCommitment', 'understandsNonComplianceConsequences', 'trackingAuthorization');
    IF invalid_count > 0 THEN
        RAISE EXCEPTION 'Cannot drop resume_institutional_commitment_info.payload: % unsupported JSON keys found', invalid_count;
    END IF;

    SELECT count(*) INTO invalid_count
    FROM students.resume_declaration_info r, jsonb_object_keys(r.payload::jsonb) AS keys(key)
    WHERE key NOT IN ('truthfulCompleteInformation', 'applicantName', 'identityDocument', 'signatureManagementSpace', 'signatureDate');
    IF invalid_count > 0 THEN
        RAISE EXCEPTION 'Cannot drop resume_declaration_info.payload: % unsupported JSON keys found', invalid_count;
    END IF;

    SELECT count(*) INTO invalid_count
    FROM (
        SELECT payload::jsonb -> 'callSource' value FROM students.resume_foundation_knowledge_info WHERE payload::jsonb ? 'callSource'
        UNION ALL SELECT payload::jsonb -> 'vaccines' FROM students.resume_health_info WHERE payload::jsonb ? 'vaccines'
        UNION ALL SELECT payload::jsonb -> 'strengthAreas' FROM students.resume_academic_performance_info WHERE payload::jsonb ? 'strengthAreas'
        UNION ALL SELECT payload::jsonb -> 'academicSkills' FROM students.resume_academic_performance_info WHERE payload::jsonb ? 'academicSkills'
        UNION ALL SELECT payload::jsonb -> 'callSource' FROM students.resume_institutional_commitment_info WHERE payload::jsonb ? 'callSource'
    ) checkbox_values
    WHERE jsonb_typeof(value) <> 'array';
    IF invalid_count > 0 THEN
        RAISE EXCEPTION 'Cannot normalize resume checkbox fields: % values are not JSON arrays', invalid_count;
    END IF;
END $$;

ALTER TABLE students.resume_personal_info
    ADD COLUMN IF NOT EXISTS document_type VARCHAR(40),
    ADD COLUMN IF NOT EXISTS municipality VARCHAR(120),
    ADD COLUMN IF NOT EXISTS department VARCHAR(120),
    ADD COLUMN IF NOT EXISTS civil_status VARCHAR(40),
    ADD COLUMN IF NOT EXISTS has_children BOOLEAN,
    ADD COLUMN IF NOT EXISTS children_count INTEGER,
    ADD COLUMN IF NOT EXISTS emergency_contact_name VARCHAR(160),
    ADD COLUMN IF NOT EXISTS emergency_contact_phone VARCHAR(30);

UPDATE students.resume_personal_info SET
    document_type = NULLIF(payload::jsonb ->> 'documentType', ''),
    municipality = NULLIF(payload::jsonb ->> 'municipality', ''),
    department = NULLIF(payload::jsonb ->> 'department', ''),
    civil_status = NULLIF(payload::jsonb ->> 'civilStatus', ''),
    has_children = CASE payload::jsonb ->> 'hasChildren' WHEN 'Si' THEN TRUE WHEN 'No' THEN FALSE ELSE NULL END,
    children_count = CASE WHEN (payload::jsonb ->> 'childrenCount') ~ '^[0-9]+$' THEN (payload::jsonb ->> 'childrenCount')::INTEGER ELSE NULL END,
    emergency_contact_name = NULLIF(payload::jsonb ->> 'emergencyContactName', ''),
    emergency_contact_phone = NULLIF(payload::jsonb ->> 'emergencyContactPhone', '');

ALTER TABLE students.resume_socioeconomic_info
    ADD COLUMN IF NOT EXISTS lives_with VARCHAR(180),
    ADD COLUMN IF NOT EXISTS family_members_count INTEGER,
    ADD COLUMN IF NOT EXISTS main_household_provider VARCHAR(160),
    ADD COLUMN IF NOT EXISTS monthly_family_income VARCHAR(80),
    ADD COLUMN IF NOT EXISTS housing_type VARCHAR(40),
    ADD COLUMN IF NOT EXISTS health_system_affiliation VARCHAR(160),
    ADD COLUMN IF NOT EXISTS health_regime VARCHAR(40),
    ADD COLUMN IF NOT EXISTS currently_works BOOLEAN,
    ADD COLUMN IF NOT EXISTS company VARCHAR(160),
    ADD COLUMN IF NOT EXISTS position VARCHAR(120),
    ADD COLUMN IF NOT EXISTS work_schedule VARCHAR(120),
    ADD COLUMN IF NOT EXISTS unemployed_last_six_months BOOLEAN,
    ADD COLUMN IF NOT EXISTS receives_government_aid BOOLEAN;

UPDATE students.resume_socioeconomic_info SET
    lives_with = NULLIF(payload::jsonb ->> 'livesWith', ''),
    family_members_count = CASE WHEN (payload::jsonb ->> 'familyMembersCount') ~ '^[0-9]+$' THEN (payload::jsonb ->> 'familyMembersCount')::INTEGER ELSE NULL END,
    main_household_provider = NULLIF(payload::jsonb ->> 'mainHouseholdProvider', ''),
    monthly_family_income = NULLIF(payload::jsonb ->> 'monthlyFamilyIncome', ''),
    housing_type = NULLIF(payload::jsonb ->> 'housingType', ''),
    health_system_affiliation = NULLIF(payload::jsonb ->> 'healthSystemAffiliation', ''),
    health_regime = NULLIF(payload::jsonb ->> 'healthRegime', ''),
    currently_works = CASE payload::jsonb ->> 'currentlyWorks' WHEN 'Si' THEN TRUE WHEN 'No' THEN FALSE ELSE NULL END,
    company = NULLIF(payload::jsonb ->> 'company', ''),
    position = NULLIF(payload::jsonb ->> 'position', ''),
    work_schedule = NULLIF(payload::jsonb ->> 'workSchedule', ''),
    unemployed_last_six_months = CASE payload::jsonb ->> 'unemployedLastSixMonths' WHEN 'Si' THEN TRUE WHEN 'No' THEN FALSE ELSE NULL END,
    receives_government_aid = CASE payload::jsonb ->> 'receivesGovernmentAid' WHEN 'Si' THEN TRUE WHEN 'No' THEN FALSE ELSE NULL END;

ALTER TABLE students.resume_academic_info
    ADD COLUMN IF NOT EXISTS last_approved_level VARCHAR(160),
    ADD COLUMN IF NOT EXISTS institution VARCHAR(180),
    ADD COLUMN IF NOT EXISTS graduation_year INTEGER,
    ADD COLUMN IF NOT EXISTS higher_studies TEXT,
    ADD COLUMN IF NOT EXISTS previous_scholarships TEXT,
    ADD COLUMN IF NOT EXISTS complementary_certificates TEXT,
    ADD COLUMN IF NOT EXISTS internet_access BOOLEAN,
    ADD COLUMN IF NOT EXISTS study_device_availability BOOLEAN;

UPDATE students.resume_academic_info SET
    last_approved_level = NULLIF(payload::jsonb ->> 'lastApprovedLevel', ''),
    institution = NULLIF(payload::jsonb ->> 'institution', ''),
    graduation_year = CASE WHEN (payload::jsonb ->> 'graduationYear') ~ '^[0-9]{4}$' THEN (payload::jsonb ->> 'graduationYear')::INTEGER ELSE NULL END,
    higher_studies = NULLIF(payload::jsonb ->> 'higherStudies', ''),
    previous_scholarships = NULLIF(payload::jsonb ->> 'previousScholarships', ''),
    complementary_certificates = NULLIF(payload::jsonb ->> 'complementaryCertificates', ''),
    internet_access = CASE payload::jsonb ->> 'internetAccess' WHEN 'Si' THEN TRUE WHEN 'No' THEN FALSE ELSE NULL END,
    study_device_availability = CASE payload::jsonb ->> 'studyDeviceAvailability' WHEN 'Si' THEN TRUE WHEN 'No' THEN FALSE ELSE NULL END;

ALTER TABLE students.resume_motivation_info
    ADD COLUMN IF NOT EXISTS scholarship_reason TEXT,
    ADD COLUMN IF NOT EXISTS program_motivation TEXT,
    ADD COLUMN IF NOT EXISTS personal_professional_goals TEXT,
    ADD COLUMN IF NOT EXISTS quality_of_life_impact TEXT,
    ADD COLUMN IF NOT EXISTS expected_learning TEXT,
    ADD COLUMN IF NOT EXISTS knowledge_application_plan TEXT;

UPDATE students.resume_motivation_info SET
    scholarship_reason = NULLIF(payload::jsonb ->> 'scholarshipReason', ''),
    program_motivation = NULLIF(payload::jsonb ->> 'programMotivation', ''),
    personal_professional_goals = NULLIF(payload::jsonb ->> 'personalProfessionalGoals', ''),
    quality_of_life_impact = NULLIF(payload::jsonb ->> 'qualityOfLifeImpact', ''),
    expected_learning = NULLIF(payload::jsonb ->> 'expectedLearning', ''),
    knowledge_application_plan = NULLIF(payload::jsonb ->> 'knowledgeApplicationPlan', '');

ALTER TABLE students.resume_availability_info
    ADD COLUMN IF NOT EXISTS available_for_classes BOOLEAN,
    ADD COLUMN IF NOT EXISTS time_limitations BOOLEAN,
    ADD COLUMN IF NOT EXISTS time_limitations_description TEXT,
    ADD COLUMN IF NOT EXISTS accepts_institution_rules BOOLEAN,
    ADD COLUMN IF NOT EXISTS participates_community_activities BOOLEAN,
    ADD COLUMN IF NOT EXISTS attendance_commitment BOOLEAN;

UPDATE students.resume_availability_info SET
    available_for_classes = CASE payload::jsonb ->> 'availableForClasses' WHEN 'Si' THEN TRUE WHEN 'No' THEN FALSE ELSE NULL END,
    time_limitations = CASE payload::jsonb ->> 'timeLimitations' WHEN 'Si' THEN TRUE WHEN 'No' THEN FALSE ELSE NULL END,
    time_limitations_description = NULLIF(payload::jsonb ->> 'timeLimitationsDescription', ''),
    accepts_institution_rules = CASE payload::jsonb ->> 'acceptsInstitutionRules' WHEN 'Si' THEN TRUE WHEN 'No' THEN FALSE ELSE NULL END,
    participates_community_activities = CASE payload::jsonb ->> 'participatesCommunityActivities' WHEN 'Si' THEN TRUE WHEN 'No' THEN FALSE ELSE NULL END,
    attendance_commitment = CASE payload::jsonb ->> 'attendanceCommitment' WHEN 'Si' THEN TRUE WHEN 'No' THEN FALSE ELSE NULL END;

ALTER TABLE students.resume_foundation_knowledge_info
    ADD COLUMN IF NOT EXISTS knew_foundation_before BOOLEAN,
    ADD COLUMN IF NOT EXISTS known_social_work TEXT;

UPDATE students.resume_foundation_knowledge_info SET
    knew_foundation_before = CASE payload::jsonb ->> 'knewFoundationBefore' WHEN 'Si' THEN TRUE WHEN 'No' THEN FALSE ELSE NULL END,
    known_social_work = NULLIF(payload::jsonb ->> 'knownSocialWork', '');

INSERT INTO students.resume_foundation_call_sources (student_id, option_value)
SELECT r.student_id, value
FROM students.resume_foundation_knowledge_info r
CROSS JOIN LATERAL jsonb_array_elements_text(COALESCE(r.payload::jsonb -> 'callSource', '[]'::jsonb)) value
WHERE NULLIF(value, '') IS NOT NULL
ON CONFLICT DO NOTHING;

ALTER TABLE students.resume_authorizations_info
    ADD COLUMN IF NOT EXISTS personal_data_processing BOOLEAN,
    ADD COLUMN IF NOT EXISTS information_verification BOOLEAN,
    ADD COLUMN IF NOT EXISTS photo_video_use BOOLEAN;

UPDATE students.resume_authorizations_info SET
    personal_data_processing = CASE payload::jsonb ->> 'personalDataProcessing' WHEN 'Si' THEN TRUE WHEN 'No' THEN FALSE ELSE NULL END,
    information_verification = CASE payload::jsonb ->> 'informationVerification' WHEN 'Si' THEN TRUE WHEN 'No' THEN FALSE ELSE NULL END,
    photo_video_use = CASE payload::jsonb ->> 'photoVideoUse' WHEN 'Si' THEN TRUE WHEN 'No' THEN FALSE ELSE NULL END;

ALTER TABLE students.resume_health_info
    ADD COLUMN IF NOT EXISTS diagnosed_disease BOOLEAN,
    ADD COLUMN IF NOT EXISTS diagnosed_disease_name VARCHAR(180),
    ADD COLUMN IF NOT EXISTS chronic_disease BOOLEAN,
    ADD COLUMN IF NOT EXISTS chronic_disease_name VARCHAR(180),
    ADD COLUMN IF NOT EXISTS physical_limitation BOOLEAN,
    ADD COLUMN IF NOT EXISTS physical_limitation_description TEXT,
    ADD COLUMN IF NOT EXISTS disability BOOLEAN,
    ADD COLUMN IF NOT EXISTS disability_description TEXT,
    ADD COLUMN IF NOT EXISTS allergies BOOLEAN,
    ADD COLUMN IF NOT EXISTS allergies_specification TEXT,
    ADD COLUMN IF NOT EXISTS current_medical_treatment BOOLEAN,
    ADD COLUMN IF NOT EXISTS current_medical_treatment_name TEXT,
    ADD COLUMN IF NOT EXISTS permanent_medication BOOLEAN,
    ADD COLUMN IF NOT EXISTS permanent_medication_names TEXT,
    ADD COLUMN IF NOT EXISTS hospitalized_last_two_years BOOLEAN,
    ADD COLUMN IF NOT EXISTS hospitalization_reason TEXT,
    ADD COLUMN IF NOT EXISTS complete_vaccination BOOLEAN,
    ADD COLUMN IF NOT EXISTS visual_difficulties BOOLEAN,
    ADD COLUMN IF NOT EXISTS uses_glasses BOOLEAN,
    ADD COLUMN IF NOT EXISTS hearing_difficulties BOOLEAN,
    ADD COLUMN IF NOT EXISTS accidents_with_sequelae BOOLEAN,
    ADD COLUMN IF NOT EXISTS accident_explanation TEXT,
    ADD COLUMN IF NOT EXISTS active_health_affiliation BOOLEAN,
    ADD COLUMN IF NOT EXISTS eps VARCHAR(160),
    ADD COLUMN IF NOT EXISTS medical_emergency_contact_name VARCHAR(160),
    ADD COLUMN IF NOT EXISTS medical_emergency_contact_relationship VARCHAR(80),
    ADD COLUMN IF NOT EXISTS medical_emergency_contact_phone VARCHAR(30);

UPDATE students.resume_health_info SET
    diagnosed_disease = CASE payload::jsonb ->> 'diagnosedDisease' WHEN 'Si' THEN TRUE WHEN 'No' THEN FALSE ELSE NULL END,
    diagnosed_disease_name = NULLIF(payload::jsonb ->> 'diagnosedDiseaseName', ''),
    chronic_disease = CASE payload::jsonb ->> 'chronicDisease' WHEN 'Si' THEN TRUE WHEN 'No' THEN FALSE ELSE NULL END,
    chronic_disease_name = NULLIF(payload::jsonb ->> 'chronicDiseaseName', ''),
    physical_limitation = CASE payload::jsonb ->> 'physicalLimitation' WHEN 'Si' THEN TRUE WHEN 'No' THEN FALSE ELSE NULL END,
    physical_limitation_description = NULLIF(payload::jsonb ->> 'physicalLimitationDescription', ''),
    disability = CASE payload::jsonb ->> 'disability' WHEN 'Si' THEN TRUE WHEN 'No' THEN FALSE ELSE NULL END,
    disability_description = NULLIF(payload::jsonb ->> 'disabilityDescription', ''),
    allergies = CASE payload::jsonb ->> 'allergies' WHEN 'Si' THEN TRUE WHEN 'No' THEN FALSE ELSE NULL END,
    allergies_specification = NULLIF(payload::jsonb ->> 'allergiesSpecification', ''),
    current_medical_treatment = CASE payload::jsonb ->> 'currentMedicalTreatment' WHEN 'Si' THEN TRUE WHEN 'No' THEN FALSE ELSE NULL END,
    current_medical_treatment_name = NULLIF(payload::jsonb ->> 'currentMedicalTreatmentName', ''),
    permanent_medication = CASE payload::jsonb ->> 'permanentMedication' WHEN 'Si' THEN TRUE WHEN 'No' THEN FALSE ELSE NULL END,
    permanent_medication_names = NULLIF(payload::jsonb ->> 'permanentMedicationNames', ''),
    hospitalized_last_two_years = CASE payload::jsonb ->> 'hospitalizedLastTwoYears' WHEN 'Si' THEN TRUE WHEN 'No' THEN FALSE ELSE NULL END,
    hospitalization_reason = NULLIF(payload::jsonb ->> 'hospitalizationReason', ''),
    complete_vaccination = CASE payload::jsonb ->> 'completeVaccination' WHEN 'Si' THEN TRUE WHEN 'No' THEN FALSE ELSE NULL END,
    visual_difficulties = CASE payload::jsonb ->> 'visualDifficulties' WHEN 'Si' THEN TRUE WHEN 'No' THEN FALSE ELSE NULL END,
    uses_glasses = CASE payload::jsonb ->> 'usesGlasses' WHEN 'Si' THEN TRUE WHEN 'No' THEN FALSE ELSE NULL END,
    hearing_difficulties = CASE payload::jsonb ->> 'hearingDifficulties' WHEN 'Si' THEN TRUE WHEN 'No' THEN FALSE ELSE NULL END,
    accidents_with_sequelae = CASE payload::jsonb ->> 'accidentsWithSequelae' WHEN 'Si' THEN TRUE WHEN 'No' THEN FALSE ELSE NULL END,
    accident_explanation = NULLIF(payload::jsonb ->> 'accidentExplanation', ''),
    active_health_affiliation = CASE payload::jsonb ->> 'activeHealthAffiliation' WHEN 'Si' THEN TRUE WHEN 'No' THEN FALSE ELSE NULL END,
    eps = NULLIF(payload::jsonb ->> 'eps', ''),
    medical_emergency_contact_name = NULLIF(payload::jsonb ->> 'medicalEmergencyContactName', ''),
    medical_emergency_contact_relationship = NULLIF(payload::jsonb ->> 'medicalEmergencyContactRelationship', ''),
    medical_emergency_contact_phone = NULLIF(payload::jsonb ->> 'medicalEmergencyContactPhone', '');

INSERT INTO students.resume_health_vaccines (student_id, option_value)
SELECT r.student_id, value
FROM students.resume_health_info r
CROSS JOIN LATERAL jsonb_array_elements_text(COALESCE(r.payload::jsonb -> 'vaccines', '[]'::jsonb)) value
WHERE NULLIF(value, '') IS NOT NULL
ON CONFLICT DO NOTHING;

ALTER TABLE students.resume_risk_factors_info
    ADD COLUMN IF NOT EXISTS tobacco_use VARCHAR(20),
    ADD COLUMN IF NOT EXISTS alcohol_use VARCHAR(20),
    ADD COLUMN IF NOT EXISTS psychoactive_substances_use BOOLEAN,
    ADD COLUMN IF NOT EXISTS substance_use_time VARCHAR(40),
    ADD COLUMN IF NOT EXISTS consumption_affected_performance BOOLEAN,
    ADD COLUMN IF NOT EXISTS prevention_program_participation BOOLEAN,
    ADD COLUMN IF NOT EXISTS current_professional_support BOOLEAN,
    ADD COLUMN IF NOT EXISTS prevention_guidance_interest BOOLEAN,
    ADD COLUMN IF NOT EXISTS healthy_habits_activities_willingness BOOLEAN,
    ADD COLUMN IF NOT EXISTS personal_family_situation_affects_process BOOLEAN,
    ADD COLUMN IF NOT EXISTS personal_family_situation_description TEXT;

UPDATE students.resume_risk_factors_info SET
    tobacco_use = NULLIF(payload::jsonb ->> 'tobaccoUse', ''),
    alcohol_use = NULLIF(payload::jsonb ->> 'alcoholUse', ''),
    psychoactive_substances_use = CASE payload::jsonb ->> 'psychoactiveSubstancesUse' WHEN 'Si' THEN TRUE WHEN 'No' THEN FALSE ELSE NULL END,
    substance_use_time = NULLIF(payload::jsonb ->> 'substanceUseTime', ''),
    consumption_affected_performance = CASE payload::jsonb ->> 'consumptionAffectedPerformance' WHEN 'Si' THEN TRUE WHEN 'No' THEN FALSE ELSE NULL END,
    prevention_program_participation = CASE payload::jsonb ->> 'preventionProgramParticipation' WHEN 'Si' THEN TRUE WHEN 'No' THEN FALSE ELSE NULL END,
    current_professional_support = CASE payload::jsonb ->> 'currentProfessionalSupport' WHEN 'Si' THEN TRUE WHEN 'No' THEN FALSE ELSE NULL END,
    prevention_guidance_interest = CASE payload::jsonb ->> 'preventionGuidanceInterest' WHEN 'Si' THEN TRUE WHEN 'No' THEN FALSE ELSE NULL END,
    healthy_habits_activities_willingness = CASE payload::jsonb ->> 'healthyHabitsActivitiesWillingness' WHEN 'Si' THEN TRUE WHEN 'No' THEN FALSE ELSE NULL END,
    personal_family_situation_affects_process = CASE payload::jsonb ->> 'personalFamilySituationAffectsProcess' WHEN 'Si' THEN TRUE WHEN 'No' THEN FALSE ELSE NULL END,
    personal_family_situation_description = NULLIF(payload::jsonb ->> 'personalFamilySituationDescription', '');

ALTER TABLE students.resume_academic_performance_info
    ADD COLUMN IF NOT EXISTS best_subject VARCHAR(120),
    ADD COLUMN IF NOT EXISTS academic_recognitions BOOLEAN,
    ADD COLUMN IF NOT EXISTS academic_recognitions_details TEXT,
    ADD COLUMN IF NOT EXISTS subjects_to_strengthen TEXT,
    ADD COLUMN IF NOT EXISTS study_habits VARCHAR(40),
    ADD COLUMN IF NOT EXISTS weekly_study_hours VARCHAR(40),
    ADD COLUMN IF NOT EXISTS task_responsibility TEXT,
    ADD COLUMN IF NOT EXISTS main_academic_achievement TEXT,
    ADD COLUMN IF NOT EXISTS areas_to_strengthen TEXT;

UPDATE students.resume_academic_performance_info SET
    best_subject = NULLIF(payload::jsonb ->> 'bestSubject', ''),
    academic_recognitions = CASE payload::jsonb ->> 'academicRecognitions' WHEN 'Si' THEN TRUE WHEN 'No' THEN FALSE ELSE NULL END,
    academic_recognitions_details = NULLIF(payload::jsonb ->> 'academicRecognitionsDetails', ''),
    subjects_to_strengthen = NULLIF(payload::jsonb ->> 'subjectsToStrengthen', ''),
    study_habits = NULLIF(payload::jsonb ->> 'studyHabits', ''),
    weekly_study_hours = NULLIF(payload::jsonb ->> 'weeklyStudyHours', ''),
    task_responsibility = NULLIF(payload::jsonb ->> 'taskResponsibility', ''),
    main_academic_achievement = NULLIF(payload::jsonb ->> 'mainAcademicAchievement', ''),
    areas_to_strengthen = NULLIF(payload::jsonb ->> 'areasToStrengthen', '');

INSERT INTO students.resume_academic_strength_areas (student_id, option_value)
SELECT r.student_id, value
FROM students.resume_academic_performance_info r
CROSS JOIN LATERAL jsonb_array_elements_text(COALESCE(r.payload::jsonb -> 'strengthAreas', '[]'::jsonb)) value
WHERE NULLIF(value, '') IS NOT NULL
ON CONFLICT DO NOTHING;

INSERT INTO students.resume_academic_skills (student_id, option_value)
SELECT r.student_id, value
FROM students.resume_academic_performance_info r
CROSS JOIN LATERAL jsonb_array_elements_text(COALESCE(r.payload::jsonb -> 'academicSkills', '[]'::jsonb)) value
WHERE NULLIF(value, '') IS NOT NULL
ON CONFLICT DO NOTHING;

ALTER TABLE students.resume_program_knowledge_info
    ADD COLUMN IF NOT EXISTS nursing_understanding TEXT,
    ADD COLUMN IF NOT EXISTS geriatrics_understanding TEXT,
    ADD COLUMN IF NOT EXISTS gerontology_understanding TEXT,
    ADD COLUMN IF NOT EXISTS geriatrics_gerontology_difference TEXT,
    ADD COLUMN IF NOT EXISTS study_reason TEXT,
    ADD COLUMN IF NOT EXISTS care_experience BOOLEAN,
    ADD COLUMN IF NOT EXISTS care_experience_description TEXT,
    ADD COLUMN IF NOT EXISTS elder_care_qualities TEXT,
    ADD COLUMN IF NOT EXISTS human_dignity_meaning TEXT,
    ADD COLUMN IF NOT EXISTS sad_older_adult_action TEXT,
    ADD COLUMN IF NOT EXISTS needs_help_no_staff_action TEXT,
    ADD COLUMN IF NOT EXISTS patience_importance TEXT,
    ADD COLUMN IF NOT EXISTS health_values TEXT,
    ADD COLUMN IF NOT EXISTS program_expected_learning TEXT,
    ADD COLUMN IF NOT EXISTS community_contribution TEXT,
    ADD COLUMN IF NOT EXISTS desired_workplace TEXT,
    ADD COLUMN IF NOT EXISTS humanized_service_meaning TEXT,
    ADD COLUMN IF NOT EXISTS practice_responsibility BOOLEAN,
    ADD COLUMN IF NOT EXISTS main_training_challenge TEXT,
    ADD COLUMN IF NOT EXISTS refuses_help_reaction TEXT,
    ADD COLUMN IF NOT EXISTS scholarship_merit_reason TEXT;

UPDATE students.resume_program_knowledge_info SET
    nursing_understanding = NULLIF(payload::jsonb ->> 'nursingUnderstanding', ''),
    geriatrics_understanding = NULLIF(payload::jsonb ->> 'geriatricsUnderstanding', ''),
    gerontology_understanding = NULLIF(payload::jsonb ->> 'gerontologyUnderstanding', ''),
    geriatrics_gerontology_difference = NULLIF(payload::jsonb ->> 'geriatricsGerontologyDifference', ''),
    study_reason = NULLIF(payload::jsonb ->> 'studyReason', ''),
    care_experience = CASE payload::jsonb ->> 'careExperience' WHEN 'Si' THEN TRUE WHEN 'No' THEN FALSE ELSE NULL END,
    care_experience_description = NULLIF(payload::jsonb ->> 'careExperienceDescription', ''),
    elder_care_qualities = NULLIF(payload::jsonb ->> 'elderCareQualities', ''),
    human_dignity_meaning = NULLIF(payload::jsonb ->> 'humanDignityMeaning', ''),
    sad_older_adult_action = NULLIF(payload::jsonb ->> 'sadOlderAdultAction', ''),
    needs_help_no_staff_action = NULLIF(payload::jsonb ->> 'needsHelpNoStaffAction', ''),
    patience_importance = NULLIF(payload::jsonb ->> 'patienceImportance', ''),
    health_values = NULLIF(payload::jsonb ->> 'healthValues', ''),
    program_expected_learning = NULLIF(payload::jsonb ->> 'programExpectedLearning', ''),
    community_contribution = NULLIF(payload::jsonb ->> 'communityContribution', ''),
    desired_workplace = NULLIF(payload::jsonb ->> 'desiredWorkplace', ''),
    humanized_service_meaning = NULLIF(payload::jsonb ->> 'humanizedServiceMeaning', ''),
    practice_responsibility = CASE payload::jsonb ->> 'practiceResponsibility' WHEN 'Si' THEN TRUE WHEN 'No' THEN FALSE ELSE NULL END,
    main_training_challenge = NULLIF(payload::jsonb ->> 'mainTrainingChallenge', ''),
    refuses_help_reaction = NULLIF(payload::jsonb ->> 'refusesHelpReaction', ''),
    scholarship_merit_reason = NULLIF(payload::jsonb ->> 'scholarshipMeritReason', '');

ALTER TABLE students.resume_institutional_commitment_info
    ADD COLUMN IF NOT EXISTS foundation_social_work_knowledge BOOLEAN,
    ADD COLUMN IF NOT EXISTS foundation_social_work_description TEXT,
    ADD COLUMN IF NOT EXISTS foundation_program_reason TEXT,
    ADD COLUMN IF NOT EXISTS understands_scholarship_responsibility BOOLEAN,
    ADD COLUMN IF NOT EXISTS student_rules_commitment BOOLEAN,
    ADD COLUMN IF NOT EXISTS respectful_conduct_commitment BOOLEAN,
    ADD COLUMN IF NOT EXISTS punctual_attendance_commitment BOOLEAN,
    ADD COLUMN IF NOT EXISTS social_community_participation BOOLEAN,
    ADD COLUMN IF NOT EXISTS resource_care_commitment BOOLEAN,
    ADD COLUMN IF NOT EXISTS understands_non_compliance_consequences BOOLEAN,
    ADD COLUMN IF NOT EXISTS tracking_authorization BOOLEAN;

UPDATE students.resume_institutional_commitment_info SET
    foundation_social_work_knowledge = CASE payload::jsonb ->> 'foundationSocialWorkKnowledge' WHEN 'Si' THEN TRUE WHEN 'No' THEN FALSE ELSE NULL END,
    foundation_social_work_description = NULLIF(payload::jsonb ->> 'foundationSocialWorkDescription', ''),
    foundation_program_reason = NULLIF(payload::jsonb ->> 'foundationProgramReason', ''),
    understands_scholarship_responsibility = CASE payload::jsonb ->> 'understandsScholarshipResponsibility' WHEN 'Si' THEN TRUE WHEN 'No' THEN FALSE ELSE NULL END,
    student_rules_commitment = CASE payload::jsonb ->> 'studentRulesCommitment' WHEN 'Si' THEN TRUE WHEN 'No' THEN FALSE ELSE NULL END,
    respectful_conduct_commitment = CASE payload::jsonb ->> 'respectfulConductCommitment' WHEN 'Si' THEN TRUE WHEN 'No' THEN FALSE ELSE NULL END,
    punctual_attendance_commitment = CASE payload::jsonb ->> 'punctualAttendanceCommitment' WHEN 'Si' THEN TRUE WHEN 'No' THEN FALSE ELSE NULL END,
    social_community_participation = CASE payload::jsonb ->> 'socialCommunityParticipation' WHEN 'Si' THEN TRUE WHEN 'No' THEN FALSE ELSE NULL END,
    resource_care_commitment = CASE payload::jsonb ->> 'resourceCareCommitment' WHEN 'Si' THEN TRUE WHEN 'No' THEN FALSE ELSE NULL END,
    understands_non_compliance_consequences = CASE payload::jsonb ->> 'understandsNonComplianceConsequences' WHEN 'Si' THEN TRUE WHEN 'No' THEN FALSE ELSE NULL END,
    tracking_authorization = CASE payload::jsonb ->> 'trackingAuthorization' WHEN 'Si' THEN TRUE WHEN 'No' THEN FALSE ELSE NULL END;

INSERT INTO students.resume_institutional_call_sources (student_id, option_value)
SELECT r.student_id, value
FROM students.resume_institutional_commitment_info r
CROSS JOIN LATERAL jsonb_array_elements_text(COALESCE(r.payload::jsonb -> 'callSource', '[]'::jsonb)) value
WHERE NULLIF(value, '') IS NOT NULL
ON CONFLICT DO NOTHING;

ALTER TABLE students.resume_declaration_info
    ADD COLUMN IF NOT EXISTS truthful_complete_information BOOLEAN,
    ADD COLUMN IF NOT EXISTS applicant_name VARCHAR(200),
    ADD COLUMN IF NOT EXISTS identity_document VARCHAR(30),
    ADD COLUMN IF NOT EXISTS signature_management_space VARCHAR(200),
    ADD COLUMN IF NOT EXISTS signature_date DATE;

UPDATE students.resume_declaration_info SET
    truthful_complete_information = CASE payload::jsonb ->> 'truthfulCompleteInformation' WHEN 'Si' THEN TRUE WHEN 'No' THEN FALSE ELSE NULL END,
    applicant_name = NULLIF(payload::jsonb ->> 'applicantName', ''),
    identity_document = NULLIF(payload::jsonb ->> 'identityDocument', ''),
    signature_management_space = NULLIF(payload::jsonb ->> 'signatureManagementSpace', ''),
    signature_date = CASE WHEN (payload::jsonb ->> 'signatureDate') ~ '^[0-9]{4}-[0-9]{2}-[0-9]{2}$' THEN (payload::jsonb ->> 'signatureDate')::DATE ELSE NULL END;

ALTER TABLE students.resume_personal_info ADD CONSTRAINT ck_resume_personal_children_count CHECK (children_count IS NULL OR children_count >= 0);
ALTER TABLE students.resume_socioeconomic_info ADD CONSTRAINT ck_resume_socioeconomic_family_members_count CHECK (family_members_count IS NULL OR family_members_count >= 0);
ALTER TABLE students.resume_academic_info ADD CONSTRAINT ck_resume_academic_graduation_year CHECK (graduation_year IS NULL OR graduation_year BETWEEN 1900 AND 2100);
ALTER TABLE students.resume_risk_factors_info ADD CONSTRAINT ck_resume_risk_tobacco_use CHECK (tobacco_use IS NULL OR tobacco_use IN ('Si', 'No', 'Ocasionalmente'));
ALTER TABLE students.resume_risk_factors_info ADD CONSTRAINT ck_resume_risk_alcohol_use CHECK (alcohol_use IS NULL OR alcohol_use IN ('Si', 'No', 'Ocasionalmente'));

DO $$
DECLARE
    source_count BIGINT;
    migrated_count BIGINT;
BEGIN
    SELECT COALESCE(sum(jsonb_array_length(COALESCE(payload::jsonb -> 'callSource', '[]'::jsonb))), 0)
    INTO source_count FROM students.resume_foundation_knowledge_info;
    SELECT count(*) INTO migrated_count FROM students.resume_foundation_call_sources;
    IF migrated_count <> source_count THEN
        RAISE EXCEPTION 'resume_foundation_call_sources count mismatch: expected %, migrated %', source_count, migrated_count;
    END IF;

    SELECT COALESCE(sum(jsonb_array_length(COALESCE(payload::jsonb -> 'vaccines', '[]'::jsonb))), 0)
    INTO source_count FROM students.resume_health_info;
    SELECT count(*) INTO migrated_count FROM students.resume_health_vaccines;
    IF migrated_count <> source_count THEN
        RAISE EXCEPTION 'resume_health_vaccines count mismatch: expected %, migrated %', source_count, migrated_count;
    END IF;

    SELECT COALESCE(sum(jsonb_array_length(COALESCE(payload::jsonb -> 'strengthAreas', '[]'::jsonb))), 0)
    INTO source_count FROM students.resume_academic_performance_info;
    SELECT count(*) INTO migrated_count FROM students.resume_academic_strength_areas;
    IF migrated_count <> source_count THEN
        RAISE EXCEPTION 'resume_academic_strength_areas count mismatch: expected %, migrated %', source_count, migrated_count;
    END IF;

    SELECT COALESCE(sum(jsonb_array_length(COALESCE(payload::jsonb -> 'academicSkills', '[]'::jsonb))), 0)
    INTO source_count FROM students.resume_academic_performance_info;
    SELECT count(*) INTO migrated_count FROM students.resume_academic_skills;
    IF migrated_count <> source_count THEN
        RAISE EXCEPTION 'resume_academic_skills count mismatch: expected %, migrated %', source_count, migrated_count;
    END IF;

    SELECT COALESCE(sum(jsonb_array_length(COALESCE(payload::jsonb -> 'callSource', '[]'::jsonb))), 0)
    INTO source_count FROM students.resume_institutional_commitment_info;
    SELECT count(*) INTO migrated_count FROM students.resume_institutional_call_sources;
    IF migrated_count <> source_count THEN
        RAISE EXCEPTION 'resume_institutional_call_sources count mismatch: expected %, migrated %', source_count, migrated_count;
    END IF;
END $$;

ALTER TABLE students.resume_personal_info DROP COLUMN IF EXISTS payload;
ALTER TABLE students.resume_socioeconomic_info DROP COLUMN IF EXISTS payload;
ALTER TABLE students.resume_academic_info DROP COLUMN IF EXISTS payload;
ALTER TABLE students.resume_motivation_info DROP COLUMN IF EXISTS payload;
ALTER TABLE students.resume_availability_info DROP COLUMN IF EXISTS payload;
ALTER TABLE students.resume_foundation_knowledge_info DROP COLUMN IF EXISTS payload;
ALTER TABLE students.resume_authorizations_info DROP COLUMN IF EXISTS payload;
ALTER TABLE students.resume_health_info DROP COLUMN IF EXISTS payload;
ALTER TABLE students.resume_risk_factors_info DROP COLUMN IF EXISTS payload;
ALTER TABLE students.resume_academic_performance_info DROP COLUMN IF EXISTS payload;
ALTER TABLE students.resume_program_knowledge_info DROP COLUMN IF EXISTS payload;
ALTER TABLE students.resume_institutional_commitment_info DROP COLUMN IF EXISTS payload;
ALTER TABLE students.resume_declaration_info DROP COLUMN IF EXISTS payload;
