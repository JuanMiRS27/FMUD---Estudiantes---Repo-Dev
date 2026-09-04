export type StudentStatus = 'ACTIVE' | 'INACTIVE';
export type DocumentStatus = 'ACTIVE' | 'REPLACED' | 'DELETED';
export type EnrollmentStatus = 'ENROLLED' | 'WITHDRAWN' | 'COMPLETED';

export interface Student {
  id: string;
  firstName: string;
  lastName: string;
  documentNumber: string;
  birthDate: string;
  birthPlace: string | null;
  address: string | null;
  phone: string | null;
  email: string | null;
  photoUrl: string | null;
  status: StudentStatus;
  createdAt: string;
  updatedAt: string;
}

export interface ResumeDetails {
  personal: Record<string, unknown>;
  socioeconomic: Record<string, unknown>;
  academic: Record<string, unknown>;
  motivation: Record<string, unknown>;
  availability: Record<string, unknown>;
  foundationKnowledge: Record<string, unknown>;
  authorizations: Record<string, unknown>;
  health: Record<string, unknown>;
  riskFactors: Record<string, unknown>;
  academicPerformance: Record<string, unknown>;
  programKnowledge: Record<string, unknown>;
  institutionalCommitment: Record<string, unknown>;
  declaration: Record<string, unknown>;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface StudentDocument {
  id: string;
  studentId: string;
  documentType: string;
  displayName: string;
  originalName: string;
  contentType: string;
  size: number;
  description: string | null;
  status: DocumentStatus;
  uploadedByUserId: string;
  createdAt: string;
  updatedAt: string;
}

export interface Enrollment {
  id: string;
  studentId: string;
  periodCode: string;
  program: string;
  status: EnrollmentStatus;
  createdAt: string;
  updatedAt: string;
}

export interface HistoryEvent {
  id: string;
  studentId: string;
  documentId: string | null;
  actorUserId: string;
  action: string;
  summary: string;
  createdAt: string;
}

export interface Resume {
  student: Student;
  details: ResumeDetails;
  enrollments: Enrollment[];
  documents: StudentDocument[];
  history: HistoryEvent[];
}

export interface StudentFormValue {
  firstName: string;
  lastName: string;
  documentNumber: string;
  birthDate: string;
  birthPlace: string;
  address: string;
  phone: string;
  email: string;
  status: StudentStatus;
}

export interface EnrollmentFormValue {
  periodCode: string;
  program: string;
  status: EnrollmentStatus;
}
