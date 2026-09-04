CREATE TABLE IF NOT EXISTS students.enrollments (
    id UUID PRIMARY KEY,
    student_id UUID NOT NULL,
    period_code VARCHAR(30) NOT NULL,
    program VARCHAR(120) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_enrollments_student
        FOREIGN KEY (student_id) REFERENCES students.students(id) ON DELETE RESTRICT,
    CONSTRAINT ck_enrollments_status CHECK (status IN ('ENROLLED', 'WITHDRAWN', 'COMPLETED')),
    CONSTRAINT uq_enrollments_student_period UNIQUE (student_id, period_code)
);

CREATE INDEX IF NOT EXISTS idx_enrollments_student_created_at
ON students.enrollments (student_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_enrollments_status
ON students.enrollments (status);
