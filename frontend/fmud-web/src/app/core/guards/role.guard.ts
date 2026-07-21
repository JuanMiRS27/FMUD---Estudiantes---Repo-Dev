import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../auth/auth.service';
import { UserRole } from '../models/role.model';

export const roleGuard: CanActivateFn = (route) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const allowed = (route.data['roles'] ?? []) as UserRole[];
  const user = auth.user();

  if (!user) {
    return router.createUrlTree(['/login']);
  }
  return allowed.length === 0 || allowed.includes(user.role) ? true : router.createUrlTree(['/dashboard']);
};
