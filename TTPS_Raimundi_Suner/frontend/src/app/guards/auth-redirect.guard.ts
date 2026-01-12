import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

/**
 * Guard que redirige al home si el usuario ya está autenticado.
 * Útil para proteger rutas como /login y /registro.
 */
export const authRedirectGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const user = authService.getCurrentUser();

  if (user) {
    // Usuario ya está logueado, redirigir al home
    router.navigate(['/']);
    return false;
  }

  // Usuario no está logueado, permitir acceso a la ruta
  return true;
};

