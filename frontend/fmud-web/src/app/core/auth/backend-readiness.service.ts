import { HttpBackend, HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { catchError, map, retry, throwError, timeout, timer } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class BackendReadinessService {
  // Startup probes must not attach a stale token or display a toast per retry.
  private readonly http = new HttpClient(inject(HttpBackend));

  waitUntilReady() {
    return this.http.get<{ status: string }>(`${environment.apiBaseUrl}/health/ready`).pipe(
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
      timeout(240000),
      catchError(() => throwError(() => ({
        message: 'No pudimos preparar el servicio a tiempo. Intenta de nuevo en unos minutos.'
      })))
    );
  }
}
