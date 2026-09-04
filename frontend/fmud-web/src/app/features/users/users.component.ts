import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import { ApiError } from '../../core/models/auth.model';
import { roleDisplayName } from '../../core/models/role.model';
import { EmptyStateComponent } from '../../shared/components/empty-state/empty-state.component';
import { ManagedUser, UserFormValue } from './user.model';
import { UsersService } from './users.service';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [ReactiveFormsModule, EmptyStateComponent],
  template: `
    <section class="page-head">
      <div>
        <p class="section-kicker">Administracion</p>
        <h2>Usuarios autorizados</h2>
        <p>Gestiona los accesos de administradores y secretarios.</p>
      </div>
      <button class="primary-action compact" type="button" (click)="openCreate()">
        <span aria-hidden="true">+</span>
        <span>Registrar usuario</span>
      </button>
    </section>

    @if (loading()) {
      <app-empty-state title="Cargando" message="Consultando usuarios autorizados." />
    } @else if (error()) {
      <app-empty-state title="Error" message="No fue posible cargar los usuarios." />
    } @else if (users().length === 0) {
      <app-empty-state title="Sin usuarios" message="No hay usuarios autorizados registrados." />
    } @else {
      <div class="document-list">
        @for (user of users(); track user.id) {
          <article class="document-row">
            <div>
              <strong>{{ user.name }}</strong>
              <span>{{ user.email }} - {{ roleLabel(user.role) }}</span>
              <span class="status-chip" [class.deleted]="!user.enabled">{{ user.enabled ? 'Habilitado' : 'Deshabilitado' }}</span>
            </div>
            <div class="row-actions">
              <button class="secondary-action" type="button" (click)="openEdit(user)">Editar</button>
              <button class="danger-action" type="button" (click)="toggleEnabled(user)">{{ user.enabled ? 'Deshabilitar' : 'Habilitar' }}</button>
            </div>
          </article>
        }
      </div>
    }

    @if (modalOpen()) {
      <div class="modal-backdrop" role="presentation">
        <form class="student-modal" [formGroup]="form" (ngSubmit)="save()" novalidate>
          <header>
            <h3>{{ editing() ? 'Editar usuario' : 'Registrar usuario' }}</h3>
            <button class="icon-only" type="button" aria-label="Cerrar" (click)="close()">x</button>
          </header>
          <div class="modal-body">
            <label [class.invalid-field]="showError('name')">Nombre
              <input formControlName="name" autocomplete="name" />
              @if (fieldError('name'); as message) { <span class="field-error">{{ message }}</span> }
            </label>
            <label [class.invalid-field]="showError('email')">Correo
              <input formControlName="email" autocomplete="email" />
              @if (fieldError('email'); as message) { <span class="field-error">{{ message }}</span> }
            </label>
            <label [class.invalid-field]="showError('password')">Contrasena
              <input type="password" formControlName="password" autocomplete="new-password" [placeholder]="editing() ? 'Dejar vacia para conservarla' : ''" />
              @if (fieldError('password'); as message) { <span class="field-error">{{ message }}</span> }
            </label>
            <label>Rol
              <select formControlName="role">
                <option value="ADMIN">Junta Administrativa</option>
                <option value="SECRETARIO">Secretaria</option>
              </select>
            </label>
            <label class="checkbox-row">
              <input type="checkbox" formControlName="enabled" />
              Usuario habilitado
            </label>
            @if (saveError()) {
              <p class="form-error full-width">{{ saveError() }}</p>
            }
          </div>
          <footer>
            <button class="secondary-action" type="button" (click)="close()">Cancelar</button>
            <button class="primary-action compact" type="submit" [disabled]="saving()">{{ saving() ? 'Guardando...' : 'Guardar usuario' }}</button>
          </footer>
        </form>
      </div>
    }
  `
})
export class UsersComponent {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(UsersService);

  readonly users = signal<ManagedUser[]>([]);
  readonly loading = signal(false);
  readonly error = signal(false);
  readonly modalOpen = signal(false);
  readonly saving = signal(false);
  readonly saveError = signal('');
  readonly editing = signal<ManagedUser | null>(null);

  readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(120)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(160)]],
    password: ['', [Validators.minLength(8), Validators.maxLength(80)]],
    role: ['SECRETARIO' as 'ADMIN' | 'SECRETARIO', Validators.required],
    enabled: [true]
  });

  constructor() {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set(false);
    this.service.list().pipe(finalize(() => this.loading.set(false))).subscribe({
      next: (users) => this.users.set(users),
      error: () => this.error.set(true)
    });
  }

  openCreate(): void {
    this.editing.set(null);
    this.form.reset({ name: '', email: '', password: '', role: 'SECRETARIO', enabled: true });
    this.saveError.set('');
    this.modalOpen.set(true);
  }

  openEdit(user: ManagedUser): void {
    this.editing.set(user);
    this.form.reset({ name: user.name, email: user.email, password: '', role: user.role, enabled: user.enabled });
    this.saveError.set('');
    this.modalOpen.set(true);
  }

  close(): void {
    this.modalOpen.set(false);
  }

  save(): void {
    if (!this.editing() && !this.form.controls.password.value.trim()) {
      this.form.controls.password.setErrors({ required: true });
    }
    if (this.form.invalid || this.saving()) {
      this.form.markAllAsTouched();
      this.saveError.set('Debe completar los campos obligatorios y corregir los datos marcados.');
      return;
    }
    this.saving.set(true);
    const value = this.form.getRawValue() as UserFormValue;
    const request = this.editing() ? this.service.update(this.editing()!.id, value) : this.service.create(value);
    request.pipe(finalize(() => this.saving.set(false))).subscribe({
      next: () => {
        this.close();
        this.load();
      },
      error: (error: ApiError) => this.saveError.set(error.message || 'No fue posible guardar el usuario.')
    });
  }

  toggleEnabled(user: ManagedUser): void {
    this.service.changeEnabled(user.id, !user.enabled).subscribe({
      next: () => this.load(),
      error: () => this.error.set(true)
    });
  }

  roleLabel(role: 'ADMIN' | 'SECRETARIO'): string {
    return roleDisplayName(role);
  }

  showError(name: 'name' | 'email' | 'password'): boolean {
    const control = this.form.controls[name];
    return control.invalid && (control.touched || control.dirty);
  }

  fieldError(name: 'name' | 'email' | 'password'): string {
    const control = this.form.controls[name];
    if (!this.showError(name)) {
      return '';
    }
    if (control.errors?.['required']) {
      return 'Este campo es obligatorio.';
    }
    if (control.errors?.['email']) {
      return 'Ingresa un correo electronico valido.';
    }
    if (control.errors?.['minlength']) {
      return `Debe contener al menos ${control.errors['minlength'].requiredLength} caracteres.`;
    }
    if (control.errors?.['maxlength']) {
      return `No puede superar ${control.errors['maxlength'].requiredLength} caracteres.`;
    }
    return 'Revisa este campo.';
  }
}
