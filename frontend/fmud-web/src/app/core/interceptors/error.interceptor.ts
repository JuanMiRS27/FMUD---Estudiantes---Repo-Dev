import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../auth/auth.service';
import { ApiError } from '../models/auth.model';
import { NotificationService } from '../services/notification.service';

export const errorInterceptor: HttpInterceptorFn = (request, next) => {
  const auth = inject(AuthService);
  const notifications = inject(NotificationService);

  return next(request).pipe(
    catchError((error: HttpErrorResponse) => {
      const apiError = normalizeError(error);
      if (error.status === 401 && !request.url.includes('/auth/login')) {
        auth.logout();
      }
      notifications.show(apiError.message);
      return throwError(() => apiError);
    })
  );
};

function normalizeError(error: HttpErrorResponse): ApiError {
  const body = error.error as Partial<ApiError> | null;
  if (body?.message && body?.code) {
    return {
      timestamp: body.timestamp ?? new Date().toISOString(),
      status: body.status ?? error.status,
      error: body.error ?? error.statusText,
      code: body.code,
      message: body.message,
      path: body.path ?? error.url ?? '',
      details: body.details ?? []
    };
  }
  if (error.status === 0 || error.status === 503) {
    return technicalError(error, 'SERVICE_UNAVAILABLE', 'Este módulo no se encuentra disponible temporalmente. Intenta nuevamente más tarde.');
  }
  return technicalError(error, 'UNEXPECTED_ERROR', 'No fue posible completar la solicitud. Intenta nuevamente.');
}

function technicalError(error: HttpErrorResponse, code: string, message: string): ApiError {
  return {
    timestamp: new Date().toISOString(),
    status: error.status || 503,
    error: error.statusText || 'Service Unavailable',
    code,
    message,
    path: error.url ?? '',
    details: []
  };
}
