import { Component, computed, inject } from '@angular/core';
import { AuthService } from '../../core/auth/auth.service';
import { roleDisplayName } from '../../core/models/role.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  template: `
    <div class="dashboard">
      @if (user(); as currentUser) {
        <section class="welcome">
          <p class="section-kicker">Inicio</p>
          <h2>Bienvenido, {{ currentUser.name }}</h2>
          <p class="role-line">Rol: {{ roleLabel() }}</p>
          <p class="welcome-copy">
            La plataforma institucional se encuentra preparada para comenzar la gestión digital de la Fundación Manos Unidas de Dios.
          </p>
        </section>
      }

      <section class="future-grid" aria-label="Espacios reservados para métricas futuras">
        <div class="future-slot"></div>
        <div class="future-slot"></div>
        <div class="future-slot"></div>
      </section>

      <p class="module-note">Los módulos institucionales se habilitarán progresivamente.</p>
    </div>
  `
})
export class DashboardComponent {
  private readonly auth = inject(AuthService);
  readonly user = this.auth.user;
  readonly roleLabel = computed(() => {
    const user = this.user();
    return user ? roleDisplayName(user.role) : '';
  });
}
