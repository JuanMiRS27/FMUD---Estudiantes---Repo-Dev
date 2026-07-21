import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';
import { ApiError } from '../../../core/models/auth.model';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule],
  template: `
    <main class="login-page">
      <section class="login-panel">
        <div class="login-brand">
          <span class="brand-mark large">FM</span>
          <div>
            <p>Fundación Manos Unidas de Dios</p>
            <h1>Ingreso institucional</h1>
          </div>
        </div>

        <form [formGroup]="form" (ngSubmit)="submit()" novalidate>
          <label>
            <span>Correo electrónico</span>
            <input type="email" formControlName="email" autocomplete="email" placeholder="usuario@fmud.local">
          </label>
          <label>
            <span>Contraseña</span>
            <input type="password" formControlName="password" autocomplete="current-password" placeholder="Ingresa tu contraseña">
          </label>

          @if (errorMessage()) {
            <p class="form-error">{{ errorMessage() }}</p>
          }

          <button class="primary-action" type="submit" [disabled]="form.invalid || loading()">
            <span class="button-icon" aria-hidden="true">I</span>
            <span>{{ loading() ? 'Ingresando...' : 'Ingresar' }}</span>
          </button>
        </form>
      </section>
    </main>
  `
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly canSubmit = computed(() => this.form.valid && !this.loading());

  readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]]
  });

  submit(): void {
    if (!this.canSubmit()) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading.set(true);
    this.errorMessage.set(null);
    this.auth.login(this.form.getRawValue()).subscribe({
      next: () => void this.router.navigate(['/dashboard']),
      error: (error: ApiError) => {
        this.loading.set(false);
        this.errorMessage.set(error.message || 'No fue posible iniciar sesión.');
      }
    });
  }
}
