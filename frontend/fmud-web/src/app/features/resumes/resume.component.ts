import { DatePipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthService } from '../../core/auth/auth.service';
import { EmptyStateComponent } from '../../shared/components/empty-state/empty-state.component';
import { Resume, StudentDocument, StudentFormValue, StudentStatus } from '../students/student.model';
import { StudentsService } from '../students/students.service';

const DOCUMENT_TYPES = [
  'Documento de identidad',
  'Registro civil',
  'Certificado de estudio',
  'Afiliacion a salud',
  'Hoja de vida firmada',
  'Fotografia adicional',
  'Otro'
];

@Component({
  selector: 'app-resume',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, EmptyStateComponent, DatePipe],
  template: `
    <a class="back-link" routerLink="/students">Volver a estudiantes</a>

    @if (loading()) {
      <app-empty-state title="Cargando" message="Consultando hoja de vida." />
    } @else if (error()) {
      <app-empty-state title="Error" message="No fue posible cargar la hoja de vida." />
    } @else if (resume(); as data) {
      <section class="resume-head">
        <div>
          <p class="section-kicker">Hoja de vida</p>
          <h2>{{ data.student.firstName }} {{ data.student.lastName }}</h2>
          <p>CC: {{ data.student.documentNumber }}</p>
        </div>
        <div class="student-photo large-photo">
          @if (data.student.photoUrl) {
            <img [src]="data.student.photoUrl" alt="Fotografia de {{ data.student.firstName }} {{ data.student.lastName }}" />
          } @else {
            <span>{{ data.student.firstName[0] }}{{ data.student.lastName[0] }}</span>
          }
        </div>
      </section>

      <div class="resume-layout">
        <section class="info-panel">
          <div class="panel-title">
            <h3>Informacion personal</h3>
            <button class="secondary-action" type="button" (click)="toggleEdit()">{{ editing() ? 'Cancelar' : 'Editar' }}</button>
          </div>
          @if (editing()) {
            <form class="stack-form" [formGroup]="form" (ngSubmit)="saveStudent(data.student.id)">
              <label>Nombres <input formControlName="firstName" /></label>
              <label>Apellidos <input formControlName="lastName" /></label>
              <label>Cedula <input formControlName="documentNumber" /></label>
              <label>Fecha de nacimiento <input type="date" formControlName="birthDate" /></label>
              <label>Lugar de nacimiento <input formControlName="birthPlace" /></label>
              <label>Direccion <input formControlName="address" /></label>
              <label>Telefono <input formControlName="phone" /></label>
              <label>Correo <input formControlName="email" /></label>
              <label>Estado
                <select formControlName="status">
                  <option value="ACTIVE">Activo</option>
                  <option value="INACTIVE">Inactivo</option>
                </select>
              </label>
              <label>Fotografia <input type="file" accept=".jpg,.jpeg,.png,.webp,image/jpeg,image/png,image/webp" (change)="selectPhoto($event)" /></label>
              @if (saveError()) {
                <p class="form-error">{{ saveError() }}</p>
              }
              <button class="primary-action compact" type="submit" [disabled]="form.invalid || saving()">Guardar cambios</button>
            </form>
          } @else {
            <dl class="resume-data">
              <div><dt>Fecha de nacimiento</dt><dd>{{ data.student.birthDate }}</dd></div>
              <div><dt>Lugar de nacimiento</dt><dd>{{ data.student.birthPlace || 'Sin registrar' }}</dd></div>
              <div><dt>Direccion</dt><dd>{{ data.student.address || 'Sin registrar' }}</dd></div>
              <div><dt>Telefono</dt><dd>{{ data.student.phone || 'Sin registrar' }}</dd></div>
              <div><dt>Correo</dt><dd>{{ data.student.email || 'Sin registrar' }}</dd></div>
              <div><dt>Estado</dt><dd>{{ data.student.status === 'ACTIVE' ? 'Activo' : 'Inactivo' }}</dd></div>
              <div><dt>Fecha de registro</dt><dd>{{ data.student.createdAt | date:'medium' }}</dd></div>
            </dl>
          }
        </section>

        <section class="info-panel">
          <h3>Documentos</h3>
          <form class="document-form" [formGroup]="documentForm" (ngSubmit)="attach(data.student.id)">
            <label>Tipo
              <select formControlName="documentType">
                @for (type of documentTypes; track type) {
                  <option [value]="type">{{ type }}</option>
                }
              </select>
            </label>
            <label>Nombre visible <input formControlName="displayName" /></label>
            <label>Descripcion <input formControlName="description" /></label>
            <label>Archivo <input type="file" accept=".pdf,.jpg,.jpeg,.png,.webp,application/pdf,image/jpeg,image/png,image/webp" (change)="selectDocument($event)" /></label>
            <button class="secondary-action" type="button" (click)="resetDocument()">Cancelar</button>
            <button class="primary-action compact" type="submit" [disabled]="documentForm.invalid || !documentFile() || uploading()">Adjuntar documento</button>
          </form>

          @if (data.documents.length === 0) {
            <app-empty-state title="Documentos" message="Este estudiante aun no tiene documentos adjuntos." />
          } @else {
            <div class="document-list">
              @for (document of data.documents; track document.id) {
                <article class="document-row">
                  <div>
                    <strong>{{ document.displayName }}</strong>
                    <span>{{ document.documentType }} · {{ document.uploadedByName }} · {{ document.createdAt | date:'short' }}</span>
                    <span>{{ sizeLabel(document.size) }} · {{ document.status }}</span>
                  </div>
                  <div class="row-actions">
                    <button class="secondary-action" type="button" (click)="download(data.student.id, document)">Descargar</button>
                    @if (canDelete()) {
                      <button class="danger-action" type="button" (click)="deleteDocument(data.student.id, document.id)">Eliminar</button>
                    }
                  </div>
                </article>
              }
            </div>
          }
        </section>

        <section class="info-panel full-width">
          <h3>Historial de cambios</h3>
          @if (data.history.length === 0) {
            <app-empty-state title="Historial" message="Aun no hay cambios registrados." />
          } @else {
            <div class="history-list">
              @for (event of data.history; track event.id) {
                <article>
                  <strong>{{ event.summary }}</strong>
                  <span>{{ event.actorName }} · {{ event.createdAt | date:'medium' }}</span>
                </article>
              }
            </div>
          }
        </section>
      </div>
    }
  `
})
export class ResumeComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly service = inject(StudentsService);
  private readonly auth = inject(AuthService);
  private readonly fb = inject(FormBuilder);

  readonly documentTypes = DOCUMENT_TYPES;
  readonly resume = signal<Resume | null>(null);
  readonly loading = signal(true);
  readonly error = signal(false);
  readonly editing = signal(false);
  readonly saving = signal(false);
  readonly uploading = signal(false);
  readonly saveError = signal('');
  readonly photo = signal<File | null>(null);
  readonly documentFile = signal<File | null>(null);
  readonly canDelete = computed(() => this.auth.user()?.role === 'ADMIN');
  private readonly studentId = this.route.snapshot.paramMap.get('id') ?? '';

  readonly form = this.fb.nonNullable.group({
    firstName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(80)]],
    lastName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(120)]],
    documentNumber: ['', [Validators.required, Validators.pattern(/^\d{6,12}$/)]],
    birthDate: ['', Validators.required],
    birthPlace: [''],
    address: [''],
    phone: ['', Validators.pattern(/^[0-9+]{0,20}$/)],
    email: ['', [Validators.email, Validators.maxLength(150)]],
    status: ['ACTIVE' as StudentStatus]
  });

  readonly documentForm = this.fb.nonNullable.group({
    documentType: [DOCUMENT_TYPES[0], Validators.required],
    displayName: ['', [Validators.required, Validators.maxLength(140)]],
    description: ['']
  });

  constructor() {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set(false);
    this.service.resume(this.studentId).pipe(finalize(() => this.loading.set(false))).subscribe({
      next: (resume) => {
        this.resume.set(resume);
        this.form.patchValue({
          firstName: resume.student.firstName,
          lastName: resume.student.lastName,
          documentNumber: resume.student.documentNumber,
          birthDate: resume.student.birthDate,
          birthPlace: resume.student.birthPlace ?? '',
          address: resume.student.address ?? '',
          phone: resume.student.phone ?? '',
          email: resume.student.email ?? '',
          status: resume.student.status
        });
      },
      error: () => this.error.set(true)
    });
  }

  toggleEdit(): void {
    this.editing.update((value) => !value);
    this.saveError.set('');
  }

  saveStudent(id: string): void {
    if (this.form.invalid || this.saving()) {
      return;
    }
    this.saving.set(true);
    this.service.update(id, this.form.getRawValue() as StudentFormValue, this.photo())
      .pipe(finalize(() => this.saving.set(false)))
      .subscribe({
        next: () => {
          this.editing.set(false);
          this.load();
        },
        error: () => this.saveError.set('No fue posible actualizar el estudiante.')
      });
  }

  attach(studentId: string): void {
    const file = this.documentFile();
    if (!file || this.documentForm.invalid || this.uploading()) {
      return;
    }
    const value = this.documentForm.getRawValue();
    this.uploading.set(true);
    this.service.attachDocument(studentId, value.documentType, value.displayName, value.description, file)
      .pipe(finalize(() => this.uploading.set(false)))
      .subscribe({ next: () => { this.resetDocument(); this.load(); }, error: () => this.error.set(true) });
  }

  download(studentId: string, document: StudentDocument): void {
    this.service.downloadDocument(studentId, document.id).subscribe((response) => {
      const blob = response.body;
      if (!blob) {
        return;
      }
      const url = URL.createObjectURL(blob);
      const link = window.document.createElement('a');
      link.href = url;
      link.download = document.originalName;
      link.click();
      URL.revokeObjectURL(url);
    });
  }

  deleteDocument(studentId: string, documentId: string): void {
    this.service.deleteDocument(studentId, documentId).subscribe({ next: () => this.load(), error: () => this.error.set(true) });
  }

  selectPhoto(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.photo.set(input.files?.item(0) ?? null);
  }

  selectDocument(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.documentFile.set(input.files?.item(0) ?? null);
  }

  resetDocument(): void {
    this.documentFile.set(null);
    this.documentForm.reset({ documentType: DOCUMENT_TYPES[0], displayName: '', description: '' });
  }

  sizeLabel(size: number): string {
    return `${(size / 1024).toFixed(1)} KB`;
  }
}
