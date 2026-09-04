CREATE TABLE IF NOT EXISTS students.resume_personal_info (
    student_id UUID PRIMARY KEY REFERENCES students.students(id) ON DELETE RESTRICT,
    payload TEXT NOT NULL DEFAULT '{}',
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS students.resume_socioeconomic_info (
    student_id UUID PRIMARY KEY REFERENCES students.students(id) ON DELETE RESTRICT,
    payload TEXT NOT NULL DEFAULT '{}',
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS students.resume_academic_info (
    student_id UUID PRIMARY KEY REFERENCES students.students(id) ON DELETE RESTRICT,
    payload TEXT NOT NULL DEFAULT '{}',
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS students.resume_motivation_info (
    student_id UUID PRIMARY KEY REFERENCES students.students(id) ON DELETE RESTRICT,
    payload TEXT NOT NULL DEFAULT '{}',
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS students.resume_availability_info (
    student_id UUID PRIMARY KEY REFERENCES students.students(id) ON DELETE RESTRICT,
    payload TEXT NOT NULL DEFAULT '{}',
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS students.resume_foundation_knowledge_info (
    student_id UUID PRIMARY KEY REFERENCES students.students(id) ON DELETE RESTRICT,
    payload TEXT NOT NULL DEFAULT '{}',
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS students.resume_authorizations_info (
    student_id UUID PRIMARY KEY REFERENCES students.students(id) ON DELETE RESTRICT,
    payload TEXT NOT NULL DEFAULT '{}',
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS students.resume_health_info (
    student_id UUID PRIMARY KEY REFERENCES students.students(id) ON DELETE RESTRICT,
    payload TEXT NOT NULL DEFAULT '{}',
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS students.resume_risk_factors_info (
    student_id UUID PRIMARY KEY REFERENCES students.students(id) ON DELETE RESTRICT,
    payload TEXT NOT NULL DEFAULT '{}',
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS students.resume_academic_performance_info (
    student_id UUID PRIMARY KEY REFERENCES students.students(id) ON DELETE RESTRICT,
    payload TEXT NOT NULL DEFAULT '{}',
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS students.resume_program_knowledge_info (
    student_id UUID PRIMARY KEY REFERENCES students.students(id) ON DELETE RESTRICT,
    payload TEXT NOT NULL DEFAULT '{}',
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS students.resume_institutional_commitment_info (
    student_id UUID PRIMARY KEY REFERENCES students.students(id) ON DELETE RESTRICT,
    payload TEXT NOT NULL DEFAULT '{}',
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS students.resume_declaration_info (
    student_id UUID PRIMARY KEY REFERENCES students.students(id) ON DELETE RESTRICT,
    payload TEXT NOT NULL DEFAULT '{}',
    updated_at TIMESTAMPTZ NOT NULL
);
