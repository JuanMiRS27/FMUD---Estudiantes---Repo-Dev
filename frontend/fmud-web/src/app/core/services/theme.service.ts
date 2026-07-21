import { computed, Injectable, signal } from '@angular/core';

type ThemeMode = 'light' | 'dark';
const THEME_KEY = 'fmud.theme';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly modeState = signal<ThemeMode>(this.loadInitialTheme());
  readonly mode = this.modeState.asReadonly();
  readonly isDark = computed(() => this.modeState() === 'dark');

  constructor() {
    this.apply(this.modeState());
  }

  toggle(): void {
    this.setTheme(this.modeState() === 'dark' ? 'light' : 'dark');
  }

  setTheme(mode: ThemeMode): void {
    localStorage.setItem(THEME_KEY, mode);
    this.modeState.set(mode);
    this.apply(mode);
  }

  private loadInitialTheme(): ThemeMode {
    return localStorage.getItem(THEME_KEY) === 'dark' ? 'dark' : 'light';
  }

  private apply(mode: ThemeMode): void {
    document.documentElement.classList.toggle('dark-theme', mode === 'dark');
  }
}
