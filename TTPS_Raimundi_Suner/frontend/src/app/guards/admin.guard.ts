import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const adminGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  const user = auth.currentUser();

  // En SSR, permitir acceso temporal
  if (typeof window === 'undefined') {
    return true;
  }

  // Verificar si el usuario es admin (rolId === 0)
  if (user && user.rolId === 0) {
    return true;
  }

  // Redirigir al home si no es admin
  router.navigateByUrl('/');
  return false;
};

