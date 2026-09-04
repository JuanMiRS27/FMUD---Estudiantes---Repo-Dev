import { DatePipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthService } from '../../core/auth/auth.service';
import { ApiError } from '../../core/models/auth.model';
import { EmptyStateComponent } from '../../shared/components/empty-state/empty-state.component';
import { Student, StudentFormValue, StudentStatus } from './student.model';
import { StudentsService } from './students.service';

@Component({
  selector: 'app-students',
  standalone: true,
  imports: [ReactiveFormsModule, EmptyStateComponent, DatePipe],
  template: `
    <section class="page-head">
      <div>
        <p class="section-kicker">Fundacion Manos Unidas de Dios</p>
        <h2>Estudiantes</h2>
        <p>Consulta y administra las hojas de vida registradas.</p>
      </div>
      <div class="page-actions">
        <button class="primary-action compact" type="button" (click)="openCreate()">
          <span aria-hidden="true">+</span>
          <span>Registrar estudiante</span>
        </button>
      </div>
    </section>

    <form class="filters" [formGroup]="filters" (ngSubmit)="search()">
      <label>
        <span>Buscar</span>
        <input formControlName="search" placeholder="Buscar por nombre o documento..." aria-label="Buscar por nombre o documento" />
      </label>
      <label>
        <span>Estado</span>
        <select formControlName="status" aria-label="Filtrar por estado">
          <option value="">Todos</option>
          <option value="ACTIVE">Activo</option>
          <option value="INACTIVE">Inactivo</option>
        </select>
      </label>
      <button class="secondary-action" type="submit">Buscar</button>
    </form>
    @if (statusError()) {
      <p class="form-error">{{ statusError() }}</p>
    }

    @if (loading()) {
      <app-empty-state title="Cargando" message="Consultando estudiantes registrados." />
    } @else if (error()) {
      <app-empty-state title="Error" message="No fue posible cargar los estudiantes." />
    } @else if (students().length === 0) {
      <app-empty-state title="Sin resultados" message="No se encontraron estudiantes que coincidan con la busqueda." />
    } @else {
      <div class="resume-table" role="table" aria-label="Listado de estudiantes">
        <div class="resume-table-row resume-table-head" role="row">
          <span>Nombre</span>
          <span>Documento</span>
          <span>Telefono</span>
          <span>Estado</span>
          <span>Actualizacion</span>
          <span>Acciones</span>
        </div>
        @for (student of students(); track student.id) {
          <div class="resume-table-row" role="row">
            <strong>{{ student.firstName }} {{ student.lastName }}</strong>
            <span>{{ student.documentNumber }}</span>
            <span>{{ student.phone || 'Sin registrar' }}</span>
            <span><span class="status-pill" [class.inactive]="student.status === 'INACTIVE'">{{ student.status === 'ACTIVE' ? 'Activo' : 'Inactivo' }}</span></span>
            <span>{{ student.updatedAt | date:'short' }}</span>
            <span class="row-actions">
              <button class="secondary-action" type="button" title="Abrir hoja de vida" (click)="openResume(student)">Ver</button>
              @if (canDeactivate()) {
                <button class="danger-action" type="button" title="Cambiar estado" (click)="changeStatus(student, $event)">
                  {{ student.status === 'ACTIVE' ? 'Desactivar' : 'Activar' }}
                </button>
              }
            </span>
          </div>
        }
      </div>
      <div class="pagination" aria-label="Paginacion">
        <button class="secondary-action" type="button" [disabled]="page() === 0" (click)="go(page() - 1)">Anterior</button>
        <span>Pagina {{ page() + 1 }} de {{ totalPages() || 1 }}</span>
        <button class="secondary-action" type="button" [disabled]="page() + 1 >= totalPages()" (click)="go(page() + 1)">Siguiente</button>
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
  readonly statusError = signal('');
  readonly editing = signal<Student | null>(null);
  readonly selectedPhoto = signal<File | null>(null);
  readonly preview = signal<string | null>(null);
  readonly photoError = signal('');
  readonly canDeactivate = computed(() => this.auth.user()?.role === 'ADMIN');
  readonly today = new Date().toISOString().slice(0, 10);

  readonly filters = this.fb.nonNullable.group({
    search: [''],
    status: ['' as StudentStatus | '']
  });

  readonly form = this.fb.nonNullable.group({
    firstName: ['', [trimmedRequired(), Validators.minLength(2), Validators.maxLength(80), Validators.pattern(/^[\p{L} -]+$/u)]],
    lastName: ['', [trimmedRequired(), Validators.minLength(2), Validators.maxLength(120), Validators.pattern(/^[\p{L} -]+$/u)]],
    documentNumber: ['', [trimmedRequired(), Validators.pattern(/^\d{6,12}$/)]],
    birthDate: ['', [Validators.required, notFutureDate()]],
    birthPlace: ['', [optionalNotBlank(), Validators.maxLength(120)]],
    address: ['', [optionalNotBlank(), Validators.maxLength(180)]],
    phone: ['', [Validators.maxLength(20), Validators.pattern(/^\+?[0-9](?:[0-9 ]{4,18}[0-9])?$/)]],
    email: ['', [Validators.email, Validators.maxLength(150)]],
    status: ['ACTIVE' as StudentStatus, Validators.required]
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
    void this.router.navigate(['/hojas-de-vida/nueva']);
  }

  save(): void {
    this.trimTextFields();
    if (this.saving()) {
      return;
    }
    if (this.form.invalid || this.photoError()) {
      this.form.markAllAsTouched();
      this.saveError.set('Debe completar los campos obligatorios y corregir los datos marcados.');
      queueMicrotask(() => this.scrollToFirstInvalid());
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
      error: (error: ApiError) => this.applySaveError(error)
    });
  }

  selectPhoto(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.item(0) ?? null;
    this.photoError.set('');
    if (file && !['image/jpeg', 'image/png', 'image/webp'].includes(file.type)) {
      this.photoError.set('La fotografia debe estar en formato JPG, PNG o WEBP.');
      input.value = '';
      this.selectedPhoto.set(null);
      this.preview.set(null);
      return;
    }
    if (file && file.size > 5 * 1024 * 1024) {
      this.photoError.set('La fotografia no puede superar 5 MB.');
      input.value = '';
      this.selectedPhoto.set(null);
      this.preview.set(null);
      return;
    }
    this.selectedPhoto.set(file);
    this.preview.set(file ? URL.createObjectURL(file) : null);
  }

  removePhoto(): void {
    this.selectedPhoto.set(null);
    this.preview.set(null);
    this.photoError.set('');
    const input = document.getElementById('student-photo') as HTMLInputElement | null;
    if (input) {
      input.value = '';
    }
  }

  closeModal(): void {
    this.modalOpen.set(false);
  }

  openResume(student: Student): void {
    void this.router.navigate(['/hojas-de-vida', student.id]);
  }

  changeStatus(student: Student, event: Event): void {
    event.stopPropagation();
    this.statusError.set('');
    this.service.changeStatus(student.id, student.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE').subscribe({
      next: () => this.load(),
      error: (error: ApiError) => this.statusError.set(error.message || 'No fue posible cambiar el estado del estudiante.')
    });
  }

  photoUrl(student: Student): string | null {
    return this.service.photoUrl(student.photoUrl);
  }

  initials(student: Student): string {
    return `${student.firstName.at(0) ?? ''}${student.lastName.at(0) ?? ''}`.toUpperCase();
  }

  showFieldError(controlName: keyof StudentFormValue): boolean {
    const control = this.form.controls[controlName];
    return control.invalid && (control.touched || control.dirty);
  }

  fieldError(controlName: keyof StudentFormValue): string {
    const control = this.form.controls[controlName];
    if (!this.showFieldError(controlName)) {
      return '';
    }
    const errors = control.errors ?? {};
    if (errors['duplicate']) {
      return 'El numero de documento ya se encuentra registrado.';
    }
    if (errors['required'] || errors['blank']) {
      return 'Este campo es obligatorio.';
    }
    if (errors['minlength']) {
      return `Debe contener al menos ${errors['minlength'].requiredLength} caracteres.`;
    }
    if (errors['maxlength']) {
      return `No puede superar ${errors['maxlength'].requiredLength} caracteres.`;
    }
    if (controlName === 'documentNumber' && errors['pattern']) {
      return 'La cedula debe contener unicamente numeros, entre 6 y 12 digitos.';
    }
    if ((controlName === 'firstName' || controlName === 'lastName') && errors['pattern']) {
      return 'Solo se permiten letras, espacios y guiones.';
    }
    if (controlName === 'birthDate' && errors['futureDate']) {
      return 'La fecha de nacimiento no puede ser futura.';
    }
    if (controlName === 'phone' && errors['pattern']) {
      return 'El telefono solo puede contener digitos, espacios controlados y + al inicio.';
    }
    if (controlName === 'email' && errors['email']) {
      return 'Ingresa un correo electronico valido.';
    }
    return 'Revisa este campo.';
  }

  private trimTextFields(): void {
    (Object.keys(this.form.controls) as Array<keyof StudentFormValue>).forEach((key) => {
      const control = this.form.controls[key];
      const value = control.value;
      if (typeof value === 'string') {
        control.setValue(value.trim() as never, { emitEvent: false });
      }
    });
  }

  private scrollToFirstInvalid(): void {
    const first = document.querySelector('.student-modal .ng-invalid:not(form), .student-modal .field-error') as HTMLElement | null;
    first?.scrollIntoView({ block: 'center', behavior: 'smooth' });
    first?.focus();
  }

  private applySaveError(error: ApiError): void {
    const text = `${error.message ?? ''} ${(error.details ?? []).join(' ')}`.toLowerCase();
    if (text.includes('cedula') || text.includes('document')) {
      this.form.controls.documentNumber.setErrors({ duplicate: true });
      this.form.controls.documentNumber.markAsTouched();
      this.saveError.set('El numero de documento ya se encuentra registrado.');
      return;
    }
    this.saveError.set(error.message || 'No fue posible guardar el estudiante.');
  }
}

function trimmedRequired(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value;
    return typeof value === 'string' && value.trim().length === 0 ? { required: true } : null;
  };
}

function optionalNotBlank(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value;
    return typeof value === 'string' && value.length > 0 && value.trim().length === 0 ? { blank: true } : null;
  };
}

function notFutureDate(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    if (!control.value) {
      return null;
    }
    return String(control.value) > new Date().toISOString().slice(0, 10) ? { futureDate: true } : null;
  };
}
