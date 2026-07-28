create table students (
  id uuid primary key,
  first_name varchar(80) not null,
  last_name varchar(120) not null,
  document_number varchar(12) not null unique,
  birth_date date not null,
  birth_place varchar(120),
  address varchar(180),
  phone varchar(20),
  email varchar(150),
  photo_storage_key varchar(255),
  photo_content_type varchar(80),
  status varchar(20) not null,
  created_at timestamp with time zone not null,
  updated_at timestamp with time zone not null
);

create index idx_students_first_name on students (first_name);
create index idx_students_last_name on students (last_name);
create index idx_students_status on students (status);

create table student_documents (
  id uuid primary key,
  student_id uuid not null references students(id),
  document_type varchar(80) not null,
  display_name varchar(140) not null,
  original_name varchar(255) not null,
  storage_key varchar(255) not null,
  content_type varchar(80) not null,
  size_bytes bigint not null,
  description varchar(500),
  status varchar(20) not null,
  uploaded_by_user_id uuid,
  uploaded_by_name varchar(160) not null,
  created_at timestamp with time zone not null,
  updated_at timestamp with time zone not null
);

create index idx_student_documents_student on student_documents (student_id);
create index idx_student_documents_type on student_documents (student_id, document_type);
create index idx_student_documents_status on student_documents (status);

create table student_history_events (
  id uuid primary key,
  student_id uuid not null references students(id),
  actor_user_id uuid,
  actor_name varchar(160) not null,
  action varchar(60) not null,
  entity_type varchar(60) not null,
  entity_id uuid not null,
  summary varchar(500) not null,
  created_at timestamp with time zone not null
);

create index idx_student_history_student_date on student_history_events (student_id, created_at desc);
