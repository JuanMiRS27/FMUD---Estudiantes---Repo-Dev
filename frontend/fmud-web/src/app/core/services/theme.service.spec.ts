import { TestBed } from '@angular/core/testing';
import { ThemeService } from './theme.service';

describe('ThemeService', () => {
  beforeEach(() => {
    localStorage.clear();
    document.documentElement.className = '';
  });

  it('toggles dark theme and persists preference', () => {
    const service = TestBed.inject(ThemeService);

    service.toggle();

    expect(service.isDark()).toBeTrue();
    expect(localStorage.getItem('fmud.theme')).toBe('dark');
    expect(document.documentElement.classList.contains('dark-theme')).toBeTrue();
  });
});
