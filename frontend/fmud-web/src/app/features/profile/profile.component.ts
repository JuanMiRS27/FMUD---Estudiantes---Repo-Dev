import { Component, computed, inject } from '@angular/core';
import { AuthService } from '../../core/auth/auth.service';
import { roleDisplayName } from '../../core/models/role.model';

@Component({
  selector: 'app-profile',
  standalone: true,
  template: `
    <section class="placeholder-page">
      <div>
        <p class="section-kicker">Mi perfil</p>
        <h2>{{ user()?.name }}</h2>
        <p class="role-line">{{ user()?.email }} · {{ roleLabel() }}</p>
      </div>
      <p class="welcome-copy">La configuraci&oacute;n del perfil estar&aacute; disponible pr&oacute;ximamente.</p>
    </section>
  `
})
export class ProfileComponent {
  private readonly auth = inject(AuthService);
  readonly user = this.auth.user;
  readonly roleLabel = computed(() => {
    const user = this.user();
    return user ? roleDisplayName(user.role) : '';
  });
}
