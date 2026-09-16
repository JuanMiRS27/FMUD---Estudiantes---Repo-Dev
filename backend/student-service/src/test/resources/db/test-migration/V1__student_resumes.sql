create schema if not exists auth;
create schema if not exists students;

create table auth.users (
  id uuid primary key,
  name varchar(120) not null,
  email varchar(160) not null unique,
  password_hash varchar(120) not null,
  role varchar(40) not null,
  enabled boolean not null default true,
  created_at timestamp with time zone not null,
  updated_at timestamp with time zone not null,
  constraint ck_users_role check (role in ('ADMIN', 'SECRETARIO'))
);

create table students.students (
  id uuid primary key,
  first_name varchar(80) not null,
  last_name varchar(120) not null,
  document_number varchar(12) not null unique,
  birth_date date not null,
  birth_place varchar(120),
  address varchar(180),
  phone varchar(20),
  email varchar(150),
  status varchar(20) not null,
  created_at timestamp with time zone not null,
  updated_at timestamp with time zone not null,
  constraint ck_students_status check (status in ('ACTIVE', 'INACTIVE'))
);

create index idx_students_first_name on students.students (first_name);
create index idx_students_last_name on students.students (last_name);
create index idx_students_status on students.students (status);

create table students.enrollments (
  id uuid primary key,
  student_id uuid not null,
  period_code varchar(30) not null,
  program varchar(120) not null,
  status varchar(20) not null,
  created_at timestamp with time zone not null,
  updated_at timestamp with time zone not null,
  constraint fk_enrollments_student foreign key (student_id) references students.students(id) on delete restrict,
  constraint ck_enrollments_status check (status in ('ENROLLED', 'WITHDRAWN', 'COMPLETED')),
  constraint uq_enrollments_student_period unique (student_id, period_code)
);

create index idx_enrollments_student_created_at on students.enrollments (student_id, created_at desc);
create index idx_enrollments_status on students.enrollments (status);

create table students.student_documents (
  id uuid primary key,
  student_id uuid not null,
  document_type varchar(40) not null,
  display_name varchar(140) not null,
  original_name varchar(255) not null,
  storage_key varchar(255) not null,
  content_type varchar(80) not null,
  size_bytes bigint not null,
  description varchar(500),
  status varchar(20) not null,
  uploaded_by_user_id uuid not null,
  created_at timestamp with time zone not null,
  updated_at timestamp with time zone not null,
  constraint fk_student_documents_student foreign key (student_id) references students.students(id) on delete restrict,
  constraint fk_student_documents_uploaded_by foreign key (uploaded_by_user_id) references auth.users(id) on delete restrict,
  constraint ck_student_documents_type check (document_type in (
    'PHOTO',
    'SIGNATURE',
    'IDENTITY_DOCUMENT',
    'CIVIL_REGISTRY',
    'STUDY_CERTIFICATE',
    'HEALTH_AFFILIATION',
    'SIGNED_RESUME',
    'OTHER'
  )),
  constraint ck_student_documents_status check (status in ('ACTIVE', 'REPLACED', 'DELETED')),
  constraint ck_student_documents_size check (size_bytes > 0)
);

create index idx_student_documents_student on students.student_documents (student_id);
create index idx_student_documents_type on students.student_documents (student_id, document_type);
create index idx_student_documents_status on students.student_documents (status);
create table students.student_history_events (
  id uuid primary key,
  student_id uuid not null,
  actor_user_id uuid not null,
  action varchar(60) not null,
  summary varchar(500) not null,
  created_at timestamp with time zone not null,
  constraint fk_student_history_student foreign key (student_id) references students.students(id) on delete restrict,
  constraint fk_student_history_actor foreign key (actor_user_id) references auth.users(id) on delete restrict,
  constraint ck_student_history_action check (action in ('STUDENT_CREATED', 'STUDENT_UPDATED', 'STUDENT_STATUS_CHANGED'))
);

create table students.document_history_events (
  id uuid primary key,
  document_id uuid not null,
  actor_user_id uuid not null,
  action varchar(60) not null,
  summary varchar(500) not null,
  created_at timestamp with time zone not null,
  constraint fk_document_history_document foreign key (document_id) references students.student_documents(id) on delete restrict,
  constraint fk_document_history_actor foreign key (actor_user_id) references auth.users(id) on delete restrict,
  constraint ck_document_history_action check (action in ('DOCUMENT_UPLOADED', 'DOCUMENT_REPLACED', 'DOCUMENT_DELETED'))
);

create index idx_student_history_events_student_created_at on students.student_history_events (student_id, created_at desc);
create index idx_student_history_events_actor_user on students.student_history_events (actor_user_id);
create index idx_document_history_events_document_created_at on students.document_history_events (document_id, created_at desc);
create index idx_document_history_events_actor_user on students.document_history_events (actor_user_id);

create table students.resume_personal_info (
  student_id uuid primary key references students.students(id) on delete restrict,
  document_type varchar(40),
  municipality varchar(120),
  department varchar(120),
  civil_status varchar(40),
  has_children boolean,
  children_count integer,
  emergency_contact_name varchar(160),
  emergency_contact_phone varchar(30),
  updated_at timestamp with time zone not null
);

create table students.resume_socioeconomic_info (
  student_id uuid primary key references students.students(id) on delete restrict,
  lives_with varchar(180),
  family_members_count integer,
  main_household_provider varchar(160),
  monthly_family_income varchar(80),
  housing_type varchar(40),
  health_system_affiliation varchar(160),
  health_regime varchar(40),
  currently_works boolean,
  company varchar(160),
  position varchar(120),
  work_schedule varchar(120),
  unemployed_last_six_months boolean,
  receives_government_aid boolean,
  updated_at timestamp with time zone not null
);

create table students.resume_academic_info (
  student_id uuid primary key references students.students(id) on delete restrict,
  last_approved_level varchar(160),
  institution varchar(180),
  graduation_year integer,
  higher_studies text,
  previous_scholarships text,
  complementary_certificates text,
  internet_access boolean,
  study_device_availability boolean,
  updated_at timestamp with time zone not null
);

create table students.resume_motivation_info (
  student_id uuid primary key references students.students(id) on delete restrict,
  scholarship_reason text,
  program_motivation text,
  personal_professional_goals text,
  quality_of_life_impact text,
  expected_learning text,
  knowledge_application_plan text,
  updated_at timestamp with time zone not null
);

create table students.resume_availability_info (
  student_id uuid primary key references students.students(id) on delete restrict,
  available_for_classes boolean,
  time_limitations boolean,
  time_limitations_description text,
  accepts_institution_rules boolean,
  participates_community_activities boolean,
  attendance_commitment boolean,
  updated_at timestamp with time zone not null
);

create table students.resume_foundation_knowledge_info (
  student_id uuid primary key references students.students(id) on delete restrict,
  knew_foundation_before boolean,
  known_social_work text,
  updated_at timestamp with time zone not null
);

create table students.resume_foundation_call_sources (
  student_id uuid not null references students.students(id) on delete restrict,
  option_value varchar(80) not null,
  primary key (student_id, option_value)
);

create table students.resume_authorizations_info (
  student_id uuid primary key references students.students(id) on delete restrict,
  personal_data_processing boolean,
  information_verification boolean,
  photo_video_use boolean,
  updated_at timestamp with time zone not null
);

create table students.resume_health_info (
  student_id uuid primary key references students.students(id) on delete restrict,
  diagnosed_disease boolean,
  diagnosed_disease_name varchar(180),
  chronic_disease boolean,
  chronic_disease_name varchar(180),
  physical_limitation boolean,
  physical_limitation_description text,
  disability boolean,
  disability_description text,
  allergies boolean,
  allergies_specification text,
  current_medical_treatment boolean,
  current_medical_treatment_name text,
  permanent_medication boolean,
  permanent_medication_names text,
  hospitalized_last_two_years boolean,
  hospitalization_reason text,
  complete_vaccination boolean,
  visual_difficulties boolean,
  uses_glasses boolean,
  hearing_difficulties boolean,
  accidents_with_sequelae boolean,
  accident_explanation text,
  active_health_affiliation boolean,
  eps varchar(160),
  medical_emergency_contact_name varchar(160),
  medical_emergency_contact_relationship varchar(80),
  medical_emergency_contact_phone varchar(30),
  updated_at timestamp with time zone not null
);

create table students.resume_health_vaccines (
  student_id uuid not null references students.students(id) on delete restrict,
  option_value varchar(80) not null,
  primary key (student_id, option_value)
);

create table students.resume_risk_factors_info (
  student_id uuid primary key references students.students(id) on delete restrict,
  tobacco_use varchar(20),
  alcohol_use varchar(20),
  psychoactive_substances_use boolean,
  substance_use_time varchar(40),
  consumption_affected_performance boolean,
  prevention_program_participation boolean,
  current_professional_support boolean,
  prevention_guidance_interest boolean,
  healthy_habits_activities_willingness boolean,
  personal_family_situation_affects_process boolean,
  personal_family_situation_description text,
  updated_at timestamp with time zone not null
);

create table students.resume_academic_performance_info (
  student_id uuid primary key references students.students(id) on delete restrict,
  best_subject varchar(120),
  academic_recognitions boolean,
  academic_recognitions_details text,
  subjects_to_strengthen text,
  study_habits varchar(40),
  weekly_study_hours varchar(40),
  task_responsibility text,
  main_academic_achievement text,
  areas_to_strengthen text,
  updated_at timestamp with time zone not null
);

create table students.resume_academic_strength_areas (
  student_id uuid not null references students.students(id) on delete restrict,
  option_value varchar(80) not null,
  primary key (student_id, option_value)
);

create table students.resume_academic_skills (
  student_id uuid not null references students.students(id) on delete restrict,
  option_value varchar(120) not null,
  primary key (student_id, option_value)
);

create table students.resume_program_knowledge_info (
  student_id uuid primary key references students.students(id) on delete restrict,
  nursing_understanding text,
  geriatrics_understanding text,
  gerontology_understanding text,
  geriatrics_gerontology_difference text,
  study_reason text,
  care_experience boolean,
  care_experience_description text,
  elder_care_qualities text,
  human_dignity_meaning text,
  sad_older_adult_action text,
  needs_help_no_staff_action text,
  patience_importance text,
  health_values text,
  program_expected_learning text,
  community_contribution text,
  desired_workplace text,
  humanized_service_meaning text,
  practice_responsibility boolean,
  main_training_challenge text,
  refuses_help_reaction text,
  scholarship_merit_reason text,
  updated_at timestamp with time zone not null
);

create table students.resume_institutional_commitment_info (
  student_id uuid primary key references students.students(id) on delete restrict,
  foundation_social_work_knowledge boolean,
  foundation_social_work_description text,
  foundation_program_reason text,
  understands_scholarship_responsibility boolean,
  student_rules_commitment boolean,
  respectful_conduct_commitment boolean,
  punctual_attendance_commitment boolean,
  social_community_participation boolean,
  resource_care_commitment boolean,
  understands_non_compliance_consequences boolean,
  tracking_authorization boolean,
  updated_at timestamp with time zone not null
);

create table students.resume_institutional_call_sources (
  student_id uuid not null references students.students(id) on delete restrict,
  option_value varchar(80) not null,
  primary key (student_id, option_value)
);

create table students.resume_declaration_info (
  student_id uuid primary key references students.students(id) on delete restrict,
  truthful_complete_information boolean,
  applicant_name varchar(200),
  identity_document varchar(30),
  signature_date date,
  updated_at timestamp with time zone not null
);
