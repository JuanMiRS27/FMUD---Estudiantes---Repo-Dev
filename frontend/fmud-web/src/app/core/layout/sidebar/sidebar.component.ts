import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
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
        <a routerLink="/dashboard" routerLinkActive="active" class="nav-item" title="Dashboard" (click)="sidebar.closeMobile()">
          <span class="nav-icon" aria-hidden="true">D</span>
          @if (!sidebar.collapsed()) {
            <span>Dashboard</span>
          }
        </a>
        <a routerLink="/students" routerLinkActive="active" class="nav-item" title="Estudiantes" (click)="sidebar.closeMobile()">
          <span class="nav-icon" aria-hidden="true">E</span>
          @if (!sidebar.collapsed()) {
            <span>Estudiantes</span>
          }
        </a>
        <a routerLink="/students" routerLinkActive="active" class="nav-item" title="Hojas de Vida" (click)="sidebar.closeMobile()">
          <span class="nav-icon" aria-hidden="true">H</span>
          @if (!sidebar.collapsed()) {
            <span>Hojas de Vida</span>
          }
        </a>
      </nav>
    </aside>
  `
})
export class SidebarComponent {
  readonly sidebar = inject(SidebarService);
}
