CREATE SCHEMA IF NOT EXISTS auth;
CREATE SCHEMA IF NOT EXISTS students;

DO $$
BEGIN
    IF to_regclass('public.students') IS NOT NULL AND to_regclass('students.students') IS NULL THEN
        ALTER TABLE public.students SET SCHEMA students;
    END IF;
    IF to_regclass('public.student_documents') IS NOT NULL AND to_regclass('students.student_documents') IS NULL THEN
        ALTER TABLE public.student_documents SET SCHEMA students;
    END IF;
    IF to_regclass('public.student_history_events') IS NOT NULL AND to_regclass('students.student_history_events') IS NULL THEN
        ALTER TABLE public.student_history_events SET SCHEMA students;
    END IF;
END $$;

DO $$
DECLARE
    missing_users bigint;
BEGIN
    IF to_regclass('auth.users') IS NULL THEN
        RAISE EXCEPTION 'auth.users must exist before normalizing student-service schema';
    END IF;

    SELECT count(*) INTO missing_users
    FROM students.student_documents d
    WHERE d.uploaded_by_user_id IS NULL
       OR NOT EXISTS (SELECT 1 FROM auth.users u WHERE u.id = d.uploaded_by_user_id);
    IF missing_users > 0 THEN
        RAISE EXCEPTION 'Cannot normalize student_documents: % rows have unresolved uploaded_by_user_id', missing_users;
    END IF;

    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'students'
          AND table_name = 'students'
          AND column_name = 'photo_storage_key'
    ) THEN
        SELECT count(*) INTO missing_users
        FROM students.students s
        WHERE s.photo_storage_key IS NOT NULL
          AND NOT EXISTS (
              SELECT 1
              FROM students.student_history_events h
              WHERE h.student_id = s.id
                AND h.actor_user_id IS NOT NULL
                AND EXISTS (SELECT 1 FROM auth.users u WHERE u.id = h.actor_user_id)
          );
        IF missing_users > 0 THEN
            RAISE EXCEPTION 'Cannot migrate student photos: % rows cannot resolve actor_user_id from history', missing_users;
        END IF;
    END IF;

    SELECT count(*) INTO missing_users
    FROM students.student_history_events h
    WHERE h.actor_user_id IS NULL
       OR NOT EXISTS (SELECT 1 FROM auth.users u WHERE u.id = h.actor_user_id);
    IF missing_users > 0 THEN
        RAISE EXCEPTION 'Cannot normalize history: % rows have unresolved actor_user_id', missing_users;
    END IF;
END $$;

INSERT INTO students.student_documents (
    id,
    student_id,
    document_type,
    display_name,
    original_name,
    storage_key,
    content_type,
    size_bytes,
    description,
    status,
    uploaded_by_user_id,
    created_at,
    updated_at
)
SELECT
    gen_random_uuid(),
    s.id,
    'PHOTO',
    'Fotografia',
    'fotografia',
    s.photo_storage_key,
    COALESCE(s.photo_content_type, 'application/octet-stream'),
    1,
    'Fotografia migrada desde students.',
    'ACTIVE',
    (
        SELECT h.actor_user_id
        FROM students.student_history_events h
        WHERE h.student_id = s.id
          AND h.actor_user_id IS NOT NULL
        ORDER BY h.created_at ASC
        LIMIT 1
    ),
    s.created_at,
    s.updated_at
FROM students.students s
WHERE EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema = 'students'
      AND table_name = 'students'
      AND column_name = 'photo_storage_key'
)
  AND s.photo_storage_key IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM students.student_documents d
      WHERE d.student_id = s.id
        AND d.document_type = 'PHOTO'
        AND d.status = 'ACTIVE'
  );

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'students'
          AND table_name = 'student_history_events'
          AND column_name IN ('actor_name', 'entity_type', 'entity_id', 'document_id')
    ) THEN
        ALTER TABLE students.student_history_events RENAME TO student_history_events_legacy;
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS students.student_history_events (
    id UUID PRIMARY KEY,
    student_id UUID NOT NULL,
    actor_user_id UUID NOT NULL,
    action VARCHAR(60) NOT NULL,
    summary VARCHAR(500) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_student_history_student
        FOREIGN KEY (student_id) REFERENCES students.students(id) ON DELETE RESTRICT,
    CONSTRAINT fk_student_history_actor
        FOREIGN KEY (actor_user_id) REFERENCES auth.users(id) ON DELETE RESTRICT,
    CONSTRAINT ck_student_history_action CHECK (
        action IN ('STUDENT_CREATED', 'STUDENT_UPDATED', 'STUDENT_STATUS_CHANGED')
    )
);

CREATE TABLE IF NOT EXISTS students.document_history_events (
    id UUID PRIMARY KEY,
    document_id UUID NOT NULL,
    actor_user_id UUID NOT NULL,
    action VARCHAR(60) NOT NULL,
    summary VARCHAR(500) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_document_history_document
        FOREIGN KEY (document_id) REFERENCES students.student_documents(id) ON DELETE RESTRICT,
    CONSTRAINT fk_document_history_actor
        FOREIGN KEY (actor_user_id) REFERENCES auth.users(id) ON DELETE RESTRICT,
    CONSTRAINT ck_document_history_action CHECK (
        action IN ('DOCUMENT_UPLOADED', 'DOCUMENT_REPLACED', 'DOCUMENT_DELETED')
    )
);

INSERT INTO students.student_history_events (id, student_id, actor_user_id, action, summary, created_at)
SELECT id, student_id, actor_user_id, action, summary, created_at
FROM students.student_history_events_legacy
WHERE to_regclass('students.student_history_events_legacy') IS NOT NULL
  AND action IN ('STUDENT_CREATED', 'STUDENT_UPDATED', 'STUDENT_STATUS_CHANGED')
ON CONFLICT (id) DO NOTHING;

DO $$
BEGIN
    IF to_regclass('students.student_history_events_legacy') IS NULL THEN
        RETURN;
    END IF;

    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'students'
          AND table_name = 'student_history_events_legacy'
          AND column_name = 'document_id'
    ) THEN
        INSERT INTO students.document_history_events (id, document_id, actor_user_id, action, summary, created_at)
        SELECT id, document_id, actor_user_id, action, summary, created_at
        FROM students.student_history_events_legacy
        WHERE action IN ('DOCUMENT_UPLOADED', 'DOCUMENT_REPLACED', 'DOCUMENT_DELETED')
          AND document_id IS NOT NULL
        ON CONFLICT (id) DO NOTHING;
    ELSE
        INSERT INTO students.document_history_events (id, document_id, actor_user_id, action, summary, created_at)
        SELECT id, entity_id, actor_user_id, action, summary, created_at
        FROM students.student_history_events_legacy
        WHERE action IN ('DOCUMENT_UPLOADED', 'DOCUMENT_REPLACED', 'DOCUMENT_DELETED')
          AND entity_type = 'DOCUMENT'
          AND entity_id IS NOT NULL
        ON CONFLICT (id) DO NOTHING;
    END IF;
END $$;

DO $$
DECLARE
    unresolved bigint;
BEGIN
    IF to_regclass('students.student_history_events_legacy') IS NULL THEN
        RETURN;
    END IF;

    SELECT count(*) INTO unresolved
    FROM students.student_history_events_legacy h
    WHERE h.action IN ('DOCUMENT_UPLOADED', 'DOCUMENT_REPLACED', 'DOCUMENT_DELETED')
      AND NOT EXISTS (SELECT 1 FROM students.document_history_events dhe WHERE dhe.id = h.id);
    IF unresolved > 0 THEN
        RAISE EXCEPTION 'Cannot migrate document history: % rows cannot resolve document_id', unresolved;
    END IF;
END $$;

ALTER TABLE students.students DROP COLUMN IF EXISTS photo_storage_key;
ALTER TABLE students.students DROP COLUMN IF EXISTS photo_content_type;
ALTER TABLE students.student_documents DROP COLUMN IF EXISTS uploaded_by_name;

ALTER TABLE students.students
    ALTER COLUMN created_at SET NOT NULL,
    ALTER COLUMN updated_at SET NOT NULL;

ALTER TABLE students.student_documents
    ALTER COLUMN uploaded_by_user_id SET NOT NULL,
    ALTER COLUMN created_at SET NOT NULL,
    ALTER COLUMN updated_at SET NOT NULL;

ALTER TABLE students.students DROP CONSTRAINT IF EXISTS ck_students_document_number;
ALTER TABLE students.students DROP CONSTRAINT IF EXISTS ck_students_status;
ALTER TABLE students.students
    ADD CONSTRAINT ck_students_document_number CHECK (document_number ~ '^[0-9]{6,12}$'),
    ADD CONSTRAINT ck_students_status CHECK (status IN ('ACTIVE', 'INACTIVE'));

ALTER TABLE students.student_documents DROP CONSTRAINT IF EXISTS ck_student_documents_type;
ALTER TABLE students.student_documents DROP CONSTRAINT IF EXISTS ck_student_documents_status;
ALTER TABLE students.student_documents DROP CONSTRAINT IF EXISTS ck_student_documents_size;
ALTER TABLE students.student_documents
    ADD CONSTRAINT ck_student_documents_type CHECK (
        document_type IN (
            'PHOTO',
            'IDENTITY_DOCUMENT',
            'CIVIL_REGISTRY',
            'STUDY_CERTIFICATE',
            'HEALTH_AFFILIATION',
            'SIGNED_RESUME',
            'OTHER'
        )
    ),
    ADD CONSTRAINT ck_student_documents_status CHECK (status IN ('ACTIVE', 'REPLACED', 'DELETED')),
    ADD CONSTRAINT ck_student_documents_size CHECK (size_bytes > 0);

DO $$
DECLARE
    constraint_name text;
BEGIN
    FOR constraint_name IN
        SELECT tc.constraint_name
        FROM information_schema.table_constraints tc
        JOIN information_schema.key_column_usage kcu
          ON kcu.constraint_schema = tc.constraint_schema
         AND kcu.constraint_name = tc.constraint_name
        WHERE tc.table_schema = 'students'
          AND tc.table_name = 'student_documents'
          AND tc.constraint_type = 'FOREIGN KEY'
          AND kcu.column_name IN ('student_id', 'uploaded_by_user_id')
    LOOP
        EXECUTE format('ALTER TABLE students.student_documents DROP CONSTRAINT %I', constraint_name);
    END LOOP;
END $$;

ALTER TABLE students.student_documents
    ADD CONSTRAINT fk_student_documents_student
        FOREIGN KEY (student_id) REFERENCES students.students(id) ON DELETE RESTRICT,
    ADD CONSTRAINT fk_student_documents_uploaded_by
        FOREIGN KEY (uploaded_by_user_id) REFERENCES auth.users(id) ON DELETE RESTRICT;

DROP INDEX IF EXISTS students.idx_student_history_student_date;
DROP INDEX IF EXISTS students.idx_student_documents_type;
CREATE INDEX IF NOT EXISTS idx_students_first_name ON students.students (lower(first_name));
CREATE INDEX IF NOT EXISTS idx_students_last_name ON students.students (lower(last_name));
CREATE INDEX IF NOT EXISTS idx_students_status ON students.students (status);
CREATE INDEX IF NOT EXISTS idx_student_documents_student ON students.student_documents (student_id);
CREATE INDEX IF NOT EXISTS idx_student_documents_type ON students.student_documents (student_id, document_type);
CREATE INDEX IF NOT EXISTS idx_student_documents_status ON students.student_documents (status);
CREATE UNIQUE INDEX IF NOT EXISTS uq_student_active_photo
ON students.student_documents(student_id)
WHERE document_type = 'PHOTO'
  AND status = 'ACTIVE';
CREATE INDEX IF NOT EXISTS idx_student_history_events_student_created_at
ON students.student_history_events (student_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_student_history_events_actor_user
ON students.student_history_events (actor_user_id);
CREATE INDEX IF NOT EXISTS idx_document_history_events_document_created_at
ON students.document_history_events (document_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_document_history_events_actor_user
ON students.document_history_events (actor_user_id);
