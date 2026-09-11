import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { AuthService } from './auth.service';
import { ApiError, LoginResponse } from '../models/auth.model';
import { environment } from '../../../environments/environment';

describe('AuthService', () => {
  let service: AuthService;
  let http: HttpTestingController;

  const credentials = { email: 'secretario@fmud.local', password: 'Cambiar123!' };
  const validResponse: LoginResponse = {
    accessToken: 'token',
    tokenType: 'Bearer',
    expiresIn: 3600,
    user: { id: '1', name: 'Usuario Secretaria', email: 'secretario@fmud.local', role: 'SECRETARIO' }
  };

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])]
    });
    service = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('stores token and user after a valid login response', () => {
    service.login(credentials).subscribe((response) => {
      expect(response).toEqual(validResponse);
    });

    flushLogin(validResponse);

    expect(service.isAuthenticated()).toBeTrue();
    expect(service.token()).toBe('token');
    expect(service.user()).toEqual(validResponse.user);
    expect(localStorage.getItem('fmud.accessToken')).toBe('token');
    expect(JSON.parse(localStorage.getItem('fmud.user') ?? 'null')).toEqual(validResponse.user);
  });

  it('rejects a null login response and does not store a session', () => {
    expectInvalidLoginResponse(null);
  });

  it('rejects a login response without accessToken and does not store a session', () => {
    expectInvalidLoginResponse({ ...validResponse, accessToken: null as unknown as string });
  });

  it('rejects a login response without user and does not store a session', () => {
    expectInvalidLoginResponse({ ...validResponse, user: null as unknown as LoginResponse['user'] });
  });

  it('propagates HTTP 401 errors without storing a session', () => {
    service.login(credentials).subscribe({
      next: fail,
      error: (error) => {
        expect(error.status).toBe(401);
        expectSessionCleared();
      }
    });

    flushLoginError(401, 'Unauthorized');
  });

  it('propagates HTTP 500 errors without storing a session', () => {
    service.login(credentials).subscribe({
      next: fail,
      error: (error) => {
        expect(error.status).toBe(500);
        expectSessionCleared();
      }
    });

    flushLoginError(500, 'Internal Server Error');
  });

  function expectInvalidLoginResponse(response: LoginResponse | null): void {
    localStorage.setItem('fmud.accessToken', 'stale-token');
    localStorage.setItem('fmud.user', JSON.stringify(validResponse.user));

    service.login(credentials).subscribe({
      next: fail,
      error: (error: ApiError) => {
        expect(error.code).toBe('INVALID_LOGIN_RESPONSE');
        expect(error.message).toContain('respuesta invalida');
        expectSessionCleared();
      }
    });

    flushLogin(response);
  }

  function flushLogin(response: LoginResponse | null): void {
    const request = http.expectOne(`${environment.apiBaseUrl}/auth/login`);
    expect(request.request.method).toBe('POST');
    request.flush(response);
  }

  function flushLoginError(status: number, statusText: string): void {
    const request = http.expectOne(`${environment.apiBaseUrl}/auth/login`);
    request.flush(
      {
        timestamp: new Date().toISOString(),
        status,
        error: statusText,
        code: status === 401 ? 'AUTHENTICATION_FAILED' : 'UNEXPECTED_ERROR',
        message: status === 401 ? 'Credenciales invalidas.' : 'No fue posible completar la solicitud.',
        path: '/api/auth/login',
        details: []
      },
      { status, statusText }
    );
  }

  function expectSessionCleared(): void {
    expect(service.isAuthenticated()).toBeFalse();
    expect(service.token()).toBeNull();
    expect(service.user()).toBeNull();
    expect(localStorage.getItem('fmud.accessToken')).toBeNull();
    expect(localStorage.getItem('fmud.user')).toBeNull();
  }
});
