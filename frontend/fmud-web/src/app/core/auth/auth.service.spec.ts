import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { AuthService } from './auth.service';
import { environment } from '../../../environments/environment';

describe('AuthService', () => {
  let service: AuthService;
  let http: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])]
    });
    service = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('stores token and user after login', () => {
    service.login({ email: 'secretario@fmud.local', password: 'Cambiar123!' }).subscribe();

    const request = http.expectOne(`${environment.apiBaseUrl}/auth/login`);
    request.flush({
      accessToken: 'token',
      tokenType: 'Bearer',
      expiresIn: 3600,
      user: { id: '1', name: 'Usuario Secretaría', email: 'secretario@fmud.local', role: 'SECRETARIO' }
    });

    expect(service.isAuthenticated()).toBeTrue();
    expect(service.token()).toBe('token');
    expect(service.user()?.role).toBe('SECRETARIO');
  });
});
