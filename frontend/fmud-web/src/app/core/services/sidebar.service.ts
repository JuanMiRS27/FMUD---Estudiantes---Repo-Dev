import { Injectable, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class SidebarService {
  private readonly collapsedState = signal(false);
  private readonly mobileOpenState = signal(false);

  readonly collapsed = this.collapsedState.asReadonly();
  readonly mobileOpen = this.mobileOpenState.asReadonly();

  toggleCollapsed(): void {
    this.collapsedState.update((value) => !value);
  }

  openMobile(): void {
    this.mobileOpenState.set(true);
  }

  closeMobile(): void {
    this.mobileOpenState.set(false);
  }
}
