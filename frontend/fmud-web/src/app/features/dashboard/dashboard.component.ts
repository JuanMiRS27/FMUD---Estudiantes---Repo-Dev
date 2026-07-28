import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { roleDisplayName } from '../../core/models/role.model';
import { StudentsService } from '../students/students.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [RouterLink],
  template: `
    <div class="dashboard">
      @if (user(); as currentUser) {
        <section class="welcome">
          <p class="section-kicker">Inicio</p>
          <h2>Bienvenido, {{ currentUser.name }}</h2>
          <p class="role-line">Rol: {{ roleLabel() }}</p>
          <p class="welcome-copy">
            Plataforma enfocada en registrar, consultar y mantener las hojas de vida de estudiantes de la Fundacion Manos Unidas de Dios.
          </p>
        </section>
      }

      <section class="quick-grid" aria-label="Accesos rapidos">
        <a class="quick-link" routerLink="/students">
          <strong>Estudiantes</strong>
          <span>Registrar, buscar y editar estudiantes.</span>
        </a>
        <a class="quick-link" routerLink="/students">
          <strong>Hojas de Vida</strong>
          <span>Consultar informacion personal, documentos e historial.</span>
        </a>
        <div class="quick-link metric">
          <strong>{{ totalStudents() ?? '...' }}</strong>
          <span>Estudiantes registrados</span>
        </div>
      </section>
    </div>
  `
})
export class DashboardComponent {
  private readonly auth = inject(AuthService);
  private readonly students = inject(StudentsService);
  readonly user = this.auth.user;
  readonly totalStudents = signal<number | null>(null);
  readonly roleLabel = computed(() => {
    const user = this.user();
    return user ? roleDisplayName(user.role) : '';
  });

  constructor() {
    this.students.list(0, 1, '', '').subscribe({
      next: (page) => this.totalStudents.set(page.totalElements),
      error: () => this.totalStudents.set(null)
    });
  }
}
