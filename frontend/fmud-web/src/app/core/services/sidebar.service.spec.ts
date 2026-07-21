import { TestBed } from '@angular/core/testing';
import { SidebarService } from './sidebar.service';

describe('SidebarService', () => {
  it('toggles collapsed state and mobile state', () => {
    const service = TestBed.inject(SidebarService);

    service.toggleCollapsed();
    service.openMobile();

    expect(service.collapsed()).toBeTrue();
    expect(service.mobileOpen()).toBeTrue();

    service.closeMobile();
    expect(service.mobileOpen()).toBeFalse();
  });
});
