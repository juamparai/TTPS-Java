import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  // Si estamos en el servidor (SSR), permitir acceso temporal
  // La validación real se hará cuando se hidrate en el cliente
  if (typeof window === 'undefined') {
    return true;
  }

  // Estamos en el cliente - validar autenticación
  try {
    const stored = localStorage.getItem('auth');
    if (stored) {
      const auth = JSON.parse(stored);
      if (auth.usuario && auth.usuario.id) {
        // Asegurar que el servicio tenga el usuario actualizado
        if (!authService.currentUser()) {
          authService.setCurrentUser(auth.usuario);
        }
        return true;
      }
    }
  } catch (error) {
    console.error('[authGuard] Error accediendo localStorage:', error);
  }

  // Verificar el signal como fallback
  const user = authService.currentUser();
  if (user && user.id) {
    return true;
  }

  // No autenticado en el cliente - redirigir
  router.navigateByUrl('/login');
  return false;
};
