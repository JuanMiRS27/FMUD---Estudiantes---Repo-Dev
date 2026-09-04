import { Component, computed, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { HeaderComponent } from '../header/header.component';
import { SidebarComponent } from '../sidebar/sidebar.component';
import { SidebarService } from '../../services/sidebar.service';
import { NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [RouterOutlet, HeaderComponent, SidebarComponent],
  template: `
    <div class="app-shell" [class.sidebar-collapsed]="sidebar.collapsed()">
      <app-sidebar />
      <main class="main-panel">
        <app-header />
        @if (notification.message(); as message) {
          <button class="toast" type="button" aria-label="Cerrar notificacion" (click)="notification.clear()">{{ message }}</button>
        }
        <section class="content">
          <router-outlet />
        </section>
      </main>
      @if (sidebar.mobileOpen()) {
        <button class="mobile-backdrop" type="button" aria-label="Cerrar menú" (click)="sidebar.closeMobile()"></button>
      }
    </div>
  `
})
export class ShellComponent {
  readonly sidebar = inject(SidebarService);
  readonly notification = inject(NotificationService);
  readonly contentState = computed(() => this.sidebar.collapsed() ? 'compact' : 'expanded');
}
