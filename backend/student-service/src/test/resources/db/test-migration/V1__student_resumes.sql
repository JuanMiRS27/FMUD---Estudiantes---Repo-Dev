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
