import { Component, computed, ElementRef, HostListener, inject, signal } from '@angular/core';
import { NavigationStart, Router } from '@angular/router';
import { filter } from 'rxjs';
import { AuthService } from '../../auth/auth.service';
import { roleDisplayName } from '../../models/role.model';
import { SidebarService } from '../../services/sidebar.service';

@Component({
  selector: 'app-header',
  standalone: true,
  template: `
    <header class="topbar">
      <button class="icon-button mobile-menu" type="button" aria-label="Abrir menu" (click)="sidebar.openMobile()">
        <span aria-hidden="true">M</span>
      </button>

      <div>
        <p class="section-kicker">Panel institucional</p>
        <h1>Dashboard</h1>
      </div>

      @if (user(); as currentUser) {
        <div class="user-menu">
          <button
            class="user-box"
            type="button"
            id="user-menu-trigger"
            aria-haspopup="menu"
            aria-controls="user-menu-popover"
            [attr.aria-expanded]="menuOpen()"
            [class.active]="menuOpen()"
            (click)="toggleMenu($event)"
          >
            <span class="user-copy">
              <strong>{{ currentUser.name }}</strong>
              <span>{{ roleLabel() }}</span>
            </span>
            <span class="avatar">{{ initials() }}</span>
            <span class="chevron" aria-hidden="true"></span>
          </button>

          @if (menuOpen()) {
            <div
              id="user-menu-popover"
              class="user-popover"
              role="menu"
              aria-labelledby="user-menu-trigger"
              (click)="$event.stopPropagation()"
            >
              <div class="user-summary">
                <span class="avatar avatar-large">{{ initials() }}</span>
                <div>
                  <strong>{{ currentUser.name }}</strong>
                  <span>{{ currentUser.email }}</span>
                  <span>{{ roleLabel() }}</span>
                </div>
              </div>

              <div class="menu-divider"></div>

              <button class="menu-option danger-option" type="button" role="menuitem" (click)="logout()">
                <span class="menu-icon" aria-hidden="true">S</span>
                <span>Cerrar sesi&oacute;n</span>
              </button>
            </div>
          }
        </div>
      }
    </header>
  `
})
export class HeaderComponent {
  readonly auth = inject(AuthService);
  readonly sidebar = inject(SidebarService);
  private readonly elementRef = inject(ElementRef<HTMLElement>);
  private readonly router = inject(Router);

  readonly menuOpen = signal(false);
  readonly user = this.auth.user;
  readonly roleLabel = computed(() => {
    const user = this.user();
    return user ? roleDisplayName(user.role) : '';
  });
  readonly initials = computed(() => {
    const name = this.user()?.name ?? '';
    return name.split(' ').filter(Boolean).slice(0, 2).map((part) => part[0]?.toUpperCase()).join('');
  });

  constructor() {
    this.router.events.pipe(filter((event) => event instanceof NavigationStart)).subscribe(() => this.closeMenu());
  }

  toggleMenu(event: MouseEvent): void {
    event.stopPropagation();
    this.menuOpen.update((open) => !open);
  }

  closeMenu(): void {
    this.menuOpen.set(false);
  }

  logout(): void {
    this.closeMenu();
    this.auth.logout();
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    if (!this.menuOpen()) {
      return;
    }

    const target = event.target;
    if (target instanceof Node && !this.elementRef.nativeElement.contains(target)) {
      this.closeMenu();
    }
  }

  @HostListener('document:keydown.escape')
  onEscape(): void {
    this.closeMenu();
  }
}
