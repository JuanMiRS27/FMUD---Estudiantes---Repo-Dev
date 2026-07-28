import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthService } from '../../core/auth/auth.service';
import { EmptyStateComponent } from '../../shared/components/empty-state/empty-state.component';
import { Student, StudentFormValue, StudentStatus } from './student.model';
import { StudentsService } from './students.service';

@Component({
  selector: 'app-students',
  standalone: true,
  imports: [ReactiveFormsModule, EmptyStateComponent],
  template: `
    <section class="page-head">
      <div>
        <p class="section-kicker">Hojas de vida</p>
        <h2>Estudiantes</h2>
      </div>
      <button class="primary-action compact" type="button" (click)="openCreate()">Registrar estudiante</button>
    </section>

    <form class="filters" [formGroup]="filters" (ngSubmit)="search()">
      <label>
        Buscar
        <input formControlName="search" placeholder="Nombres, apellidos o cedula" />
      </label>
      <label>
        Estado
        <select formControlName="status">
          <option value="">Todos</option>
          <option value="ACTIVE">Activo</option>
          <option value="INACTIVE">Inactivo</option>
        </select>
      </label>
      <button class="secondary-action" type="submit">Buscar</button>
    </form>

    @if (loading()) {
      <app-empty-state title="Cargando" message="Consultando estudiantes registrados." />
    } @else if (error()) {
      <app-empty-state title="Error" message="No fue posible cargar los estudiantes." />
    } @else if (students().length === 0) {
      <app-empty-state title="Sin resultados" message="Aun no hay estudiantes registrados." />
    } @else {
      <div class="student-grid">
        @for (student of students(); track student.id) {
          <article class="student-card" tabindex="0" (click)="openResume(student)" (keydown.enter)="openResume(student)">
            <div class="student-card-copy">
              <h3>{{ student.firstName }} {{ student.lastName }}</h3>
              <p>CC: {{ student.documentNumber }}</p>
              <span class="status-pill" [class.inactive]="student.status === 'INACTIVE'">{{ student.status === 'ACTIVE' ? 'Activo' : 'Inactivo' }}</span>
              <small>Espacio preparado para informacion futura</small>
            </div>
            <div class="student-photo">
              @if (photoUrl(student); as url) {
                <img [src]="url" alt="Fotografia de {{ student.firstName }} {{ student.lastName }}" />
              } @else {
                <span>{{ initials(student) }}</span>
              }
            </div>
          </article>
        }
      </div>
      <div class="pagination">
        <button class="secondary-action" type="button" [disabled]="page() === 0" (click)="go(page() - 1)">Anterior</button>
        <span>Pagina {{ page() + 1 }} de {{ totalPages() || 1 }}</span>
        <button class="secondary-action" type="button" [disabled]="page() + 1 >= totalPages()" (click)="go(page() + 1)">Siguiente</button>
      </div>
    }

    @if (modalOpen()) {
      <div class="modal-backdrop" role="presentation">
        <form class="student-modal" [formGroup]="form" (ngSubmit)="save()">
          <header>
            <h3>{{ editing() ? 'Editar estudiante' : 'Registrar estudiante' }}</h3>
            <button type="button" class="icon-only" (click)="closeModal()">×</button>
          </header>
          <div class="modal-body">
            <label class="photo-input">
              Fotografia
              <input type="file" accept=".jpg,.jpeg,.png,.webp,image/jpeg,image/png,image/webp" (change)="selectPhoto($event)" />
              @if (preview(); as image) {
                <img [src]="image" alt="Vista previa de fotografia" />
              }
            </label>
            <label>Nombres <input formControlName="firstName" /></label>
            <label>Apellidos <input formControlName="lastName" /></label>
            <label>Cedula <input formControlName="documentNumber" /></label>
            <label>Fecha de nacimiento <input type="date" formControlName="birthDate" /></label>
            <label>Lugar de nacimiento <input formControlName="birthPlace" /></label>
            <label>Direccion <input formControlName="address" /></label>
            <label>Telefono <input formControlName="phone" /></label>
            <label>Correo electronico <input formControlName="email" /></label>
            <label>Estado
              <select formControlName="status">
                <option value="ACTIVE">Activo</option>
                <option value="INACTIVE">Inactivo</option>
              </select>
            </label>
            @if (saveError()) {
              <p class="form-error">{{ saveError() }}</p>
            }
          </div>
          <footer>
            <button class="secondary-action" type="button" (click)="closeModal()">Cancelar</button>
            <button class="primary-action compact" type="submit" [disabled]="form.invalid || saving()">
              {{ saving() ? 'Guardando...' : 'Guardar estudiante' }}
            </button>
          </footer>
        </form>
      </div>
    }
  `
})
export class StudentsComponent {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(StudentsService);
  private readonly router = inject(Router);
  private readonly auth = inject(AuthService);

  readonly students = signal<Student[]>([]);
  readonly loading = signal(false);
  readonly error = signal(false);
  readonly page = signal(0);
  readonly size = signal(12);
  readonly totalPages = signal(0);
  readonly modalOpen = signal(false);
  readonly saving = signal(false);
  readonly saveError = signal('');
  readonly editing = signal<Student | null>(null);
  readonly selectedPhoto = signal<File | null>(null);
  readonly preview = signal<string | null>(null);
  readonly canDeactivate = computed(() => this.auth.user()?.role === 'ADMIN');

  readonly filters = this.fb.nonNullable.group({
    search: [''],
    status: ['' as StudentStatus | '']
  });

  readonly form = this.fb.nonNullable.group({
    firstName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(80), Validators.pattern(/^[A-Za-zÁÉÍÓÚÜÑáéíóúüñ -]+$/)]],
    lastName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(120), Validators.pattern(/^[A-Za-zÁÉÍÓÚÜÑáéíóúüñ -]+$/)]],
    documentNumber: ['', [Validators.required, Validators.pattern(/^\d{6,12}$/)]],
    birthDate: ['', Validators.required],
    birthPlace: [''],
    address: [''],
    phone: ['', Validators.pattern(/^[0-9+]{0,20}$/)],
    email: ['', [Validators.email, Validators.maxLength(150)]],
    status: ['ACTIVE' as StudentStatus]
  });

  constructor() {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set(false);
    this.service.list(this.page(), this.size(), this.filters.controls.search.value, this.filters.controls.status.value)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (response) => {
          this.students.set(response.content);
          this.totalPages.set(response.totalPages);
        },
        error: () => this.error.set(true)
      });
  }

  search(): void {
    this.page.set(0);
    this.load();
  }

  go(page: number): void {
    this.page.set(page);
    this.load();
  }

  openCreate(): void {
    this.editing.set(null);
    this.form.reset({ firstName: '', lastName: '', documentNumber: '', birthDate: '', birthPlace: '', address: '', phone: '', email: '', status: 'ACTIVE' });
    this.selectedPhoto.set(null);
    this.preview.set(null);
    this.saveError.set('');
    this.modalOpen.set(true);
  }

  save(): void {
    if (this.form.invalid || this.saving()) {
      return;
    }
    this.saving.set(true);
    this.saveError.set('');
    const value = this.form.getRawValue() as StudentFormValue;
    const request = this.editing()
      ? this.service.update(this.editing()!.id, value, this.selectedPhoto())
      : this.service.create(value, this.selectedPhoto());
    request.pipe(finalize(() => this.saving.set(false))).subscribe({
      next: () => {
        this.modalOpen.set(false);
        this.load();
      },
      error: () => this.saveError.set('No fue posible guardar el estudiante. Revisa los datos e intenta de nuevo.')
    });
  }

  selectPhoto(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.item(0) ?? null;
    this.selectedPhoto.set(file);
    this.preview.set(file ? URL.createObjectURL(file) : null);
  }

  closeModal(): void {
    this.modalOpen.set(false);
  }

  openResume(student: Student): void {
    void this.router.navigate(['/students', student.id, 'resume']);
  }

  photoUrl(student: Student): string | null {
    return this.service.photoUrl(student.photoUrl);
  }

  initials(student: Student): string {
    return `${student.firstName.at(0) ?? ''}${student.lastName.at(0) ?? ''}`.toUpperCase();
  }
}
