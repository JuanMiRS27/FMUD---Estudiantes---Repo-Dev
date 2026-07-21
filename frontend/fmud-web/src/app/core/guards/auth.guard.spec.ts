import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { authGuard } from './auth.guard';

describe('authGuard', () => {
  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({ providers: [provideRouter([])] });
  });

  it('redirects anonymous users to login', () => {
    const result = TestBed.runInInjectionContext(() => authGuard({} as never, {} as never));
    expect((result as ReturnType<Router['createUrlTree']>).toString()).toBe('/login');
  });

  it('allows authenticated users', () => {
    localStorage.setItem('fmud.accessToken', 'token');
    localStorage.setItem('fmud.user', JSON.stringify({ id: '1', name: 'Usuario', email: 'u@fmud.local', role: 'ADMIN' }));

    const result = TestBed.runInInjectionContext(() => authGuard({} as never, {} as never));
    expect(result).toBeTrue();
  });
});
