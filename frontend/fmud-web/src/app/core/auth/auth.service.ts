import { HttpClient } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, map, tap, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiError, AuthenticatedUser, LoginRequest, LoginResponse } from '../models/auth.model';

const TOKEN_KEY = 'fmud.accessToken';
const USER_KEY = 'fmud.user';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly tokenState = signal<string | null>(localStorage.getItem(TOKEN_KEY));
  private readonly userState = signal<AuthenticatedUser | null>(this.readStoredUser());

  readonly token = this.tokenState.asReadonly();
  readonly user = this.userState.asReadonly();
  readonly isAuthenticated = computed(() => Boolean(this.tokenState() && this.userState()));

  login(credentials: LoginRequest) {
    return this.http.post<LoginResponse>(`${environment.apiBaseUrl}/auth/login`, credentials).pipe(
      map((response) => this.validateLoginResponse(response)),
      tap((response) => this.setSession(response))
    );
  }

  loadMe() {
    return this.http.get<AuthenticatedUser>(`${environment.apiBaseUrl}/auth/me`).pipe(
      tap((user) => {
        localStorage.setItem(USER_KEY, JSON.stringify(user));
        this.userState.set(user);
      }),
      catchError((error: unknown) => {
        this.logout(false);
        return throwError(() => error);
      })
    );
  }

  logout(navigate = true): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    this.tokenState.set(null);
    this.userState.set(null);
    if (navigate) {
      void this.router.navigate(['/login']);
    }
  }

  private validateLoginResponse(response: LoginResponse | null): LoginResponse {
    if (!response || !response.accessToken || !response.user) {
      this.logout(false);
      throw this.invalidLoginResponseError();
    }
    return response;
  }

  private setSession(response: LoginResponse): void {
    localStorage.setItem(TOKEN_KEY, response.accessToken);
    localStorage.setItem(USER_KEY, JSON.stringify(response.user));
    this.tokenState.set(response.accessToken);
    this.userState.set(response.user);
  }

  private invalidLoginResponseError(): ApiError {
    return {
      timestamp: new Date().toISOString(),
      status: 502,
      error: 'Bad Gateway',
      code: 'INVALID_LOGIN_RESPONSE',
      message: 'No fue posible iniciar sesion. El servidor devolvio una respuesta invalida.',
      path: `${environment.apiBaseUrl}/auth/login`,
      details: []
    };
  }

  private readStoredUser(): AuthenticatedUser | null {
    const raw = localStorage.getItem(USER_KEY);
    if (!raw) {
      return null;
    }
    try {
      return JSON.parse(raw) as AuthenticatedUser;
    } catch {
      localStorage.removeItem(USER_KEY);
      return null;
    }
  }
}
