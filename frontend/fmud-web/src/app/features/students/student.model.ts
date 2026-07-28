export type StudentStatus = 'ACTIVE' | 'INACTIVE';
export type DocumentStatus = 'ACTIVE' | 'REPLACED' | 'DELETED';

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
  uploadedByName: string;
  createdAt: string;
  updatedAt: string;
}

export interface HistoryEvent {
  id: string;
  action: string;
  actorName: string;
  entityType: string;
  summary: string;
  createdAt: string;
}

export interface Resume {
  student: Student;
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
