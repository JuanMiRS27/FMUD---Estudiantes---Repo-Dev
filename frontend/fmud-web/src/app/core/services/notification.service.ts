import { Injectable, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private readonly messageState = signal<string | null>(null);
  readonly message = this.messageState.asReadonly();

  show(message: string): void {
    this.messageState.set(message);
  }

  clear(): void {
    this.messageState.set(null);
  }
}
