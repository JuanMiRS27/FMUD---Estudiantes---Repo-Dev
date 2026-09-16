ALTER TABLE students.student_documents DROP CONSTRAINT IF EXISTS ck_student_documents_type;
ALTER TABLE students.student_documents
    ADD CONSTRAINT ck_student_documents_type CHECK (
        document_type IN (
            'PHOTO',
            'SIGNATURE',
            'IDENTITY_DOCUMENT',
            'CIVIL_REGISTRY',
            'STUDY_CERTIFICATE',
            'HEALTH_AFFILIATION',
            'SIGNED_RESUME',
            'OTHER'
        )
    );

CREATE UNIQUE INDEX IF NOT EXISTS uq_student_active_signature
ON students.student_documents(student_id)
WHERE document_type = 'SIGNATURE'
  AND status = 'ACTIVE';

ALTER TABLE students.resume_declaration_info DROP COLUMN IF EXISTS signature_management_space;
