import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

/**
 * Guard que redirige al home si el usuario ya está autenticado.
 * Útil para proteger rutas como /login y /registro.
 */
export const authRedirectGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  // Si estamos en el servidor (SSR), permitir acceso
  // La redirección solo debe ocurrir en el cliente
  if (typeof window === 'undefined') {
    return true;
  }

  // Estamos en el cliente - verificar si el usuario está autenticado
  try {
    const stored = localStorage.getItem('auth');
    if (stored) {
      const auth = JSON.parse(stored);
      if (auth.usuario && auth.usuario.id) {
        // Asegurar que el servicio tenga el usuario actualizado
        if (!authService.currentUser()) {
          authService.setCurrentUser(auth.usuario);
        }
        // Usuario ya está logueado, redirigir al home
        router.navigate(['/']);
        return false;
      }
    }
  } catch (error) {
    console.error('[authRedirectGuard] Error accediendo localStorage:', error);
  }

  // Verificar el signal como fallback
  const user = authService.getCurrentUser();
  if (user) {
    // Usuario ya está logueado, redirigir al home
    router.navigate(['/']);
    return false;
  }

  // Usuario no está logueado, permitir acceso a la ruta
  return true;
};
