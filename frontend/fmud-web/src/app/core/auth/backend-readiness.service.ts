import { HttpBackend, HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { catchError, finalize, map, Observable, retry, shareReplay, throwError, timeout, timer } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class BackendReadinessService {
  // Startup probes must not attach a stale token or display a toast per retry.
  private readonly http = new HttpClient(inject(HttpBackend));
  private inFlight: Observable<void> | null = null;

  // Shared across callers so a warm-up started on page load and a later
  // submit() reuse the same polling cycle instead of racing two of them
  // against a cold Render instance.
  waitUntilReady(): Observable<void> {
    if (!this.inFlight) {
      this.inFlight = this.http.get<{ status: string }>(`${environment.apiBaseUrl}/health/ready`).pipe(
        timeout(15000),
        map(response => {
          if (response?.status !== 'UP') {
            throw { status: 503 };
          }
        }),
        retry({
          delay: (error: { status?: number; name?: string }) => {
            if (error.name === 'TimeoutError' || [0, 200, 429, 502, 503, 504].includes(error.status ?? 0)) {
              return timer(3000);
            }
            return throwError(() => error);
          }
        }),
        timeout(300000),
        catchError(() => throwError(() => ({
          message: 'No pudimos preparar el servicio a tiempo. Intenta de nuevo en unos minutos.'
        }))),
        // Clear the cache once this attempt settles so a later call (e.g. after
        // logout, once the instance has had time to fall back asleep) re-checks
        // readiness instead of replaying a stale "ready" result forever.
        finalize(() => this.inFlight = null),
        shareReplay({ bufferSize: 1, refCount: false })
      );
    }
    return this.inFlight;
  }
}
