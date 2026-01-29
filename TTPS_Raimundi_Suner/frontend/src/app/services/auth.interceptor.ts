import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from './auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.getToken();
  // Debug: log token presence for outgoing requests
  try {
    // eslint-disable-next-line no-console
    console.debug('[authInterceptor] url=', req.url, 'token=', token ? '***present***' : null);
  } catch {}

  if (token) {
    const cloned = req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
    return next(cloned);
  }
  return next(req);
};
