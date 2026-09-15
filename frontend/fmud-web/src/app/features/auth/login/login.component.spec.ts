import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, Subject, throwError } from 'rxjs';
import { BackendReadinessService } from '../../../core/auth/backend-readiness.service';
import { AuthService } from '../../../core/auth/auth.service';
import { LoginComponent } from './login.component';

describe('LoginComponent', () => {
  let fixture: ComponentFixture<LoginComponent>;
  let component: LoginComponent;
  let auth: jasmine.SpyObj<AuthService>;
  let readiness: jasmine.SpyObj<BackendReadinessService>;

  beforeEach(async () => {
    auth = jasmine.createSpyObj<AuthService>('AuthService', ['login']);
    readiness = jasmine.createSpyObj<BackendReadinessService>('BackendReadinessService', ['waitUntilReady']);
    readiness.waitUntilReady.and.returnValue(of(undefined));
    await TestBed.configureTestingModule({
      imports: [LoginComponent],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: auth },
        { provide: BackendReadinessService, useValue: readiness }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
  });

  it('submits valid credentials', () => {
    auth.login.and.returnValue(of({
      accessToken: 'token',
      tokenType: 'Bearer',
      expiresIn: 3600,
      user: { id: '1', name: 'Usuario', email: 'secretario@fmud.local', role: 'SECRETARIO' }
    }));
    component.form.setValue({ email: 'secretario@fmud.local', password: 'Cambiar123!' });

    component.submit();

    expect(auth.login).toHaveBeenCalledWith({ email: 'secretario@fmud.local', password: 'Cambiar123!' });
  });

  it('shows backend login errors', () => {
    auth.login.and.returnValue(throwError(() => ({ message: 'Credenciales incorrectas.' })));
    component.form.setValue({ email: 'secretario@fmud.local', password: 'Cambiar123!' });

    component.submit();

    expect(component.errorMessage()).toBe('Credenciales incorrectas.');
  });

  it('does not submit credentials before readiness and allows cancelling', () => {
    const pending = new Subject<void>();
    readiness.waitUntilReady.and.returnValue(pending);
    component.form.setValue({ email: 'secretario@fmud.local', password: 'Cambiar123!' });
    component.submit();
    expect(component.preparing()).toBeTrue();
    expect(auth.login).not.toHaveBeenCalled();
    component.cancel();
    pending.next();
    expect(auth.login).not.toHaveBeenCalled();
    expect(component.loading()).toBeFalse();
    expect(component.preparing()).toBeFalse();
  });
});
