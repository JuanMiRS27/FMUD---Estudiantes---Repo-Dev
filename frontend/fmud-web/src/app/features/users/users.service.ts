import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { ManagedUser, UserFormValue } from './user.model';

@Injectable({ providedIn: 'root' })
export class UsersService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/users`;

  list() {
    return this.http.get<ManagedUser[]>(this.baseUrl);
  }

  create(value: UserFormValue) {
    return this.http.post<ManagedUser>(this.baseUrl, this.payload(value, true));
  }

  update(id: string, value: UserFormValue) {
    return this.http.put<ManagedUser>(`${this.baseUrl}/${id}`, this.payload(value, false));
  }

  changeEnabled(id: string, enabled: boolean) {
    return this.http.patch<ManagedUser>(`${this.baseUrl}/${id}/enabled`, { enabled });
  }

  private payload(value: UserFormValue, includePassword: boolean) {
    return {
      name: value.name.trim(),
      email: value.email.trim().toLowerCase(),
      password: includePassword || value.password.trim() ? value.password : null,
      role: value.role,
      enabled: value.enabled
    };
  }
}
