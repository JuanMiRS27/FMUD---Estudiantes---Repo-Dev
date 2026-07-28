import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { PageResponse, Resume, Student, StudentDocument, StudentFormValue, StudentStatus } from './student.model';

@Injectable({ providedIn: 'root' })
export class StudentsService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/students`;

  list(page: number, size: number, search: string, status: StudentStatus | '') {
    let params = new HttpParams().set('page', page).set('size', size);
    if (search.trim()) {
      params = params.set('search', search.trim());
    }
    if (status) {
      params = params.set('status', status);
    }
    return this.http.get<PageResponse<Student>>(this.baseUrl, { params });
  }

  create(value: StudentFormValue, photo: File | null) {
    return this.http.post<Student>(this.baseUrl, this.formData(value, photo));
  }

  update(id: string, value: StudentFormValue, photo: File | null) {
    return this.http.put<Student>(`${this.baseUrl}/${id}`, this.formData(value, photo));
  }

  changeStatus(id: string, status: StudentStatus) {
    return this.http.patch<Student>(`${this.baseUrl}/${id}/status`, { status });
  }

  resume(id: string) {
    return this.http.get<Resume>(`${this.baseUrl}/${id}/resume`);
  }

  attachDocument(studentId: string, documentType: string, displayName: string, description: string, file: File) {
    const data = new FormData();
    data.append('documentType', documentType);
    data.append('displayName', displayName);
    data.append('description', description);
    data.append('file', file);
    return this.http.post<StudentDocument>(`${this.baseUrl}/${studentId}/documents`, data);
  }

  deleteDocument(studentId: string, documentId: string) {
    return this.http.delete<void>(`${this.baseUrl}/${studentId}/documents/${documentId}`);
  }

  downloadDocument(studentId: string, documentId: string) {
    return this.http.get(`${this.baseUrl}/${studentId}/documents/${documentId}/download`, {
      observe: 'response',
      responseType: 'blob'
    });
  }

  photoUrl(path: string | null): string | null {
    return path ? path : null;
  }

  private formData(value: StudentFormValue, photo: File | null): FormData {
    const data = new FormData();
    Object.entries(value).forEach(([key, entry]) => data.append(key, entry));
    if (photo) {
      data.append('photo', photo);
    }
    return data;
  }
}
