import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../auth/auth.service';
import { SidebarService } from '../../services/sidebar.service';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive],
  template: `
    <aside class="sidebar" [class.collapsed]="sidebar.collapsed()" [class.mobile-open]="sidebar.mobileOpen()">
      <button class="brand" type="button" (click)="sidebar.toggleCollapsed()" aria-label="Alternar barra lateral">
        <span class="brand-mark">FM</span>
        @if (!sidebar.collapsed()) {
          <span class="brand-text">Fundaci&oacute;n Manos Unidas de Dios</span>
        }
      </button>

      <nav class="nav-menu" aria-label="Navegacion principal">
        <a routerLink="/hojas-de-vida" routerLinkActive="active" class="nav-item" title="Hojas de Vida" (click)="sidebar.closeMobile()">
          <span class="nav-icon" aria-hidden="true">HV</span>
          @if (!sidebar.collapsed()) {
            <span>Hojas de Vida</span>
          }
        </a>
        @if (auth.user()?.role === 'ADMIN') {
          <a routerLink="/users" routerLinkActive="active" class="nav-item" title="Usuarios" (click)="sidebar.closeMobile()">
            <span class="nav-icon" aria-hidden="true">US</span>
            @if (!sidebar.collapsed()) {
              <span>Usuarios</span>
            }
          </a>
        }
      </nav>
    </aside>
  `
})
export class SidebarComponent {
  readonly sidebar = inject(SidebarService);
  readonly auth = inject(AuthService);
}
