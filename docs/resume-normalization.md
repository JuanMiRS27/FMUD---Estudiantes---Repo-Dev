# Normalizacion de hojas de vida

## Estructura anterior

Las tablas `students.resume_*_info` tenian `student_id`, `payload TEXT` y `updated_at`. El `payload` guardaba JSON serializado con campos de negocio del formulario Angular.

## Campos detectados y destino relacional

| Seccion | Campos simples convertidos a columnas | Campos repetitivos convertidos a tablas |
| --- | --- | --- |
| personal | documentType -> document_type VARCHAR(40); municipality -> municipality VARCHAR(120); department -> department VARCHAR(120); civilStatus -> civil_status VARCHAR(40); hasChildren -> has_children BOOLEAN; childrenCount -> children_count INTEGER; emergencyContactName -> emergency_contact_name VARCHAR(160); emergencyContactPhone -> emergency_contact_phone VARCHAR(30) | Ninguno |
| socioeconomic | livesWith -> lives_with VARCHAR(180); familyMembersCount -> family_members_count INTEGER; mainHouseholdProvider -> main_household_provider VARCHAR(160); monthlyFamilyIncome -> monthly_family_income VARCHAR(80); housingType -> housing_type VARCHAR(40); healthSystemAffiliation -> health_system_affiliation VARCHAR(160); healthRegime -> health_regime VARCHAR(40); currentlyWorks -> currently_works BOOLEAN; company -> company VARCHAR(160); position -> position VARCHAR(120); workSchedule -> work_schedule VARCHAR(120); unemployedLastSixMonths -> unemployed_last_six_months BOOLEAN; receivesGovernmentAid -> receives_government_aid BOOLEAN | Ninguno |
| academic | lastApprovedLevel -> last_approved_level VARCHAR(160); institution -> institution VARCHAR(180); graduationYear -> graduation_year INTEGER; higherStudies -> higher_studies TEXT; previousScholarships -> previous_scholarships TEXT; complementaryCertificates -> complementary_certificates TEXT; internetAccess -> internet_access BOOLEAN; studyDeviceAvailability -> study_device_availability BOOLEAN | Ninguno |
| motivation | scholarshipReason -> scholarship_reason TEXT; programMotivation -> program_motivation TEXT; personalProfessionalGoals -> personal_professional_goals TEXT; qualityOfLifeImpact -> quality_of_life_impact TEXT; expectedLearning -> expected_learning TEXT; knowledgeApplicationPlan -> knowledge_application_plan TEXT | Ninguno |
| availability | availableForClasses -> available_for_classes BOOLEAN; timeLimitations -> time_limitations BOOLEAN; timeLimitationsDescription -> time_limitations_description TEXT; acceptsInstitutionRules -> accepts_institution_rules BOOLEAN; participatesCommunityActivities -> participates_community_activities BOOLEAN; attendanceCommitment -> attendance_commitment BOOLEAN | Ninguno |
| foundationKnowledge | knewFoundationBefore -> knew_foundation_before BOOLEAN; knownSocialWork -> known_social_work TEXT | callSource -> students.resume_foundation_call_sources(student_id, option_value) |
| authorizations | personalDataProcessing -> personal_data_processing BOOLEAN; informationVerification -> information_verification BOOLEAN; photoVideoUse -> photo_video_use BOOLEAN | Ninguno |
| health | diagnosedDisease -> diagnosed_disease BOOLEAN; diagnosedDiseaseName -> diagnosed_disease_name VARCHAR(180); chronicDisease -> chronic_disease BOOLEAN; chronicDiseaseName -> chronic_disease_name VARCHAR(180); physicalLimitation -> physical_limitation BOOLEAN; physicalLimitationDescription -> physical_limitation_description TEXT; disability -> disability BOOLEAN; disabilityDescription -> disability_description TEXT; allergies -> allergies BOOLEAN; allergiesSpecification -> allergies_specification TEXT; currentMedicalTreatment -> current_medical_treatment BOOLEAN; currentMedicalTreatmentName -> current_medical_treatment_name TEXT; permanentMedication -> permanent_medication BOOLEAN; permanentMedicationNames -> permanent_medication_names TEXT; hospitalizedLastTwoYears -> hospitalized_last_two_years BOOLEAN; hospitalizationReason -> hospitalization_reason TEXT; completeVaccination -> complete_vaccination BOOLEAN; visualDifficulties -> visual_difficulties BOOLEAN; usesGlasses -> uses_glasses BOOLEAN; hearingDifficulties -> hearing_difficulties BOOLEAN; accidentsWithSequelae -> accidents_with_sequelae BOOLEAN; accidentExplanation -> accident_explanation TEXT; activeHealthAffiliation -> active_health_affiliation BOOLEAN; eps -> eps VARCHAR(160); medicalEmergencyContactName -> medical_emergency_contact_name VARCHAR(160); medicalEmergencyContactRelationship -> medical_emergency_contact_relationship VARCHAR(80); medicalEmergencyContactPhone -> medical_emergency_contact_phone VARCHAR(30) | vaccines -> students.resume_health_vaccines(student_id, option_value) |
| riskFactors | tobaccoUse -> tobacco_use VARCHAR(20); alcoholUse -> alcohol_use VARCHAR(20); psychoactiveSubstancesUse -> psychoactive_substances_use BOOLEAN; substanceUseTime -> substance_use_time VARCHAR(40); consumptionAffectedPerformance -> consumption_affected_performance BOOLEAN; preventionProgramParticipation -> prevention_program_participation BOOLEAN; currentProfessionalSupport -> current_professional_support BOOLEAN; preventionGuidanceInterest -> prevention_guidance_interest BOOLEAN; healthyHabitsActivitiesWillingness -> healthy_habits_activities_willingness BOOLEAN; personalFamilySituationAffectsProcess -> personal_family_situation_affects_process BOOLEAN; personalFamilySituationDescription -> personal_family_situation_description TEXT | Ninguno |
| academicPerformance | bestSubject -> best_subject VARCHAR(120); academicRecognitions -> academic_recognitions BOOLEAN; academicRecognitionsDetails -> academic_recognitions_details TEXT; subjectsToStrengthen -> subjects_to_strengthen TEXT; studyHabits -> study_habits VARCHAR(40); weeklyStudyHours -> weekly_study_hours VARCHAR(40); taskResponsibility -> task_responsibility TEXT; mainAcademicAchievement -> main_academic_achievement TEXT; areasToStrengthen -> areas_to_strengthen TEXT | strengthAreas -> students.resume_academic_strength_areas(student_id, option_value); academicSkills -> students.resume_academic_skills(student_id, option_value) |
| programKnowledge | nursingUnderstanding -> nursing_understanding TEXT; geriatricsUnderstanding -> geriatrics_understanding TEXT; gerontologyUnderstanding -> gerontology_understanding TEXT; geriatricsGerontologyDifference -> geriatrics_gerontology_difference TEXT; studyReason -> study_reason TEXT; careExperience -> care_experience BOOLEAN; careExperienceDescription -> care_experience_description TEXT; elderCareQualities -> elder_care_qualities TEXT; humanDignityMeaning -> human_dignity_meaning TEXT; sadOlderAdultAction -> sad_older_adult_action TEXT; needsHelpNoStaffAction -> needs_help_no_staff_action TEXT; patienceImportance -> patience_importance TEXT; healthValues -> health_values TEXT; programExpectedLearning -> program_expected_learning TEXT; communityContribution -> community_contribution TEXT; desiredWorkplace -> desired_workplace TEXT; humanizedServiceMeaning -> humanized_service_meaning TEXT; practiceResponsibility -> practice_responsibility BOOLEAN; mainTrainingChallenge -> main_training_challenge TEXT; refusesHelpReaction -> refuses_help_reaction TEXT; scholarshipMeritReason -> scholarship_merit_reason TEXT | Ninguno |
| institutionalCommitment | foundationSocialWorkKnowledge -> foundation_social_work_knowledge BOOLEAN; foundationSocialWorkDescription -> foundation_social_work_description TEXT; foundationProgramReason -> foundation_program_reason TEXT; understandsScholarshipResponsibility -> understands_scholarship_responsibility BOOLEAN; studentRulesCommitment -> student_rules_commitment BOOLEAN; respectfulConductCommitment -> respectful_conduct_commitment BOOLEAN; punctualAttendanceCommitment -> punctual_attendance_commitment BOOLEAN; socialCommunityParticipation -> social_community_participation BOOLEAN; resourceCareCommitment -> resource_care_commitment BOOLEAN; understandsNonComplianceConsequences -> understands_non_compliance_consequences BOOLEAN; trackingAuthorization -> tracking_authorization BOOLEAN | callSource -> students.resume_institutional_call_sources(student_id, option_value) |
| declaration | truthfulCompleteInformation -> truthful_complete_information BOOLEAN; applicantName -> applicant_name VARCHAR(200); identityDocument -> identity_document VARCHAR(30); signatureManagementSpace -> signature_management_space VARCHAR(200); signatureDate -> signature_date DATE | Ninguno |

Todos los campos del formulario son opcionales a nivel de base excepto `student_id` y `updated_at`, porque las filas pueden existir parcialmente diligenciadas. Los campos marcados como obligatorios en UI siguen siendo validados por Angular.

## DER textual final

- `auth.users` 1:N `students.student_documents` por `uploaded_by_user_id`.
- `auth.users` 1:N `students.student_history_events` por `actor_user_id`.
- `auth.users` 1:N `students.document_history_events` por `actor_user_id`.
- `students.students` 1:N `students.student_documents`.
- `students.students` 1:N `students.student_history_events`.
- `students.student_documents` 1:N `students.document_history_events`.
- `students.students` 1:N `students.enrollments`.
- `students.students` 1:1 con cada tabla principal `students.resume_*_info`, usando `student_id` como PK/FK.
- `students.students` 1:N `students.resume_foundation_call_sources`.
- `students.students` 1:N `students.resume_health_vaccines`.
- `students.students` 1:N `students.resume_academic_strength_areas`.
- `students.students` 1:N `students.resume_academic_skills`.
- `students.students` 1:N `students.resume_institutional_call_sources`.

## Tablas 1:N creadas

- `students.resume_foundation_call_sources(student_id, option_value)`.
- `students.resume_health_vaccines(student_id, option_value)`.
- `students.resume_academic_strength_areas(student_id, option_value)`.
- `students.resume_academic_skills(student_id, option_value)`.
- `students.resume_institutional_call_sources(student_id, option_value)`.

Estas tablas eliminan arrays de negocio dentro de `payload` y modelan selecciones multiples como filas atomicas.

## Migracion de datos

Archivo: `backend/student-service/src/main/resources/db/migration/V5__normalize_resume_details_payloads.sql`.

Orden aplicado:

1. Crea tablas hijas para selecciones multiples.
2. Valida que los JSON existentes no tengan claves fuera del formulario actual.
3. Valida que campos de checkbox sean arrays.
4. Agrega columnas normalizadas a cada tabla `resume_*_info`.
5. Copia cada propiedad JSON a su columna tipada.
6. Copia cada opcion seleccionada a su tabla hija.
7. Valida conteos de opciones migradas.
8. Elimina `payload`.

Si aparecen claves no soportadas o checkboxes que no sean arrays, la migracion falla antes de eliminar `payload`.

## Normalizacion

| Tabla | 1FN | 2FN | 3FN | Justificacion |
| --- | --- | --- | --- | --- |
| auth.users | Si | Si | Si | Atributos atomicos de usuario; PK simple; no hay atributos no clave que dependan de otros no clave. |
| students.students | Si | Si | Si | Datos base del estudiante son atomicos; PK simple; no se repiten documentos, fotos ni historial. |
| students.student_documents | Si | Si | Si | Cada fila representa un documento; el usuario de carga es FK, no nombre duplicado. |
| students.student_history_events | Si | Si | Si | Cada evento es atomico y referencia estudiante y actor. |
| students.document_history_events | Si | Si | Si | Cada evento es atomico y referencia documento y actor. |
| students.enrollments | Si | Si | Si | Una matricula por estudiante y periodo; atributos dependen de la matricula completa. |
| students.resume_personal_info | Si | Si | Si | Relacion 1:1; columnas atomicas; no duplica nombre/documento/email ya presentes en students. |
| students.resume_socioeconomic_info | Si | Si | Si | Datos socioeconomicos atomicos; PK simple; sin dependencias transitivas modeladas. |
| students.resume_academic_info | Si | Si | Si | Campos academicos actuales son simples; no hay lista real de estudios en UI. |
| students.resume_motivation_info | Si | Si | Si | Respuestas textuales atomicas dependientes del estudiante. |
| students.resume_availability_info | Si | Si | Si | Booleanos y descripcion atomicos; no hay grupos repetidos. |
| students.resume_foundation_knowledge_info | Si | Si | Si | Campos simples 1:1; las fuentes multiples salen a tabla hija. |
| students.resume_foundation_call_sources | Si | Si | Si | Una opcion por fila; PK compuesta completa `(student_id, option_value)` sin atributos no clave. |
| students.resume_authorizations_info | Si | Si | Si | Autorizaciones booleanas atomicas dependientes del estudiante. |
| students.resume_health_info | Si | Si | Si | Datos de salud atomicos; vacunas multiples salen a tabla hija. |
| students.resume_health_vaccines | Si | Si | Si | Una vacuna por fila; PK compuesta sin atributos adicionales. |
| students.resume_risk_factors_info | Si | Si | Si | Habitos y factores actuales son respuestas atomicas; no hay dependencias transitivas. |
| students.resume_academic_performance_info | Si | Si | Si | Campos simples atomicos; areas y habilidades multiples salen a tablas hijas. |
| students.resume_academic_strength_areas | Si | Si | Si | Una area por fila; PK compuesta sin atributos no clave. |
| students.resume_academic_skills | Si | Si | Si | Una habilidad por fila; PK compuesta sin atributos no clave. |
| students.resume_program_knowledge_info | Si | Si | Si | Respuestas de conocimiento/motivacion atomicas dependientes del estudiante. |
| students.resume_institutional_commitment_info | Si | Si | Si | Compromisos booleanos/textuales atomicos; fuentes multiples salen a tabla hija. |
| students.resume_institutional_call_sources | Si | Si | Si | Una fuente por fila; PK compuesta sin atributos no clave. |
| students.resume_declaration_info | Si | Si | Si | Declaracion 1:1 con fecha tipada; campos derivados se mantienen como datos capturados de declaracion. |

## Verificacion

- Backend completo: `./mvnw.cmd test` OK.
- Student service: `./mvnw.cmd -pl backend/student-service test` OK.
- Frontend build: `npm run build` OK.
- Frontend tests: `npm test` OK.
- PostgreSQL 18/Flyway real: no ejecutado por Docker Desktop detenido; `docker compose up -d postgres` fallo al conectar con `dockerDesktopLinuxEngine`.
- Persistencia de hoja de vida: cubierta por `JdbcResumeDetailsRepositoryTest`.
- Matriculas, documentos e historial: cubiertos por pruebas existentes de servicio.

## Busqueda de payload

Coincidencias restantes:

- Migracion historica V4: conserva el estado historico de Flyway y no debe editarse.
- Migracion V5: usa `payload` solo para migrar y luego lo elimina.
- `frontend/fmud-web/src/app/features/users/users.service.ts`: metodo llamado `payload()` para usuarios; no corresponde a `resume_*` ni a almacenamiento JSON.

No queda codigo activo de hoja de vida que persista `payload TEXT`.
