import { inject, PLATFORM_ID } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { isPlatformBrowser } from '@angular/common';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const platformId = inject(PLATFORM_ID);

  // En el navegador, verificar directamente el localStorage
  if (isPlatformBrowser(platformId)) {
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
      console.error('Error reading auth from storage:', error);
    }
  }

  // Si no hay usuario en localStorage, verificar el signal
  const user = authService.currentUser();
  if (user && user.id) {
    return true;
  }

  // No autenticado
  router.navigateByUrl('/login');
  return false;
};
