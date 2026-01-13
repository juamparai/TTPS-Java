import { Injectable, PLATFORM_ID, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { isPlatformBrowser } from '@angular/common';
import { Observable, tap } from 'rxjs';
import { ToastService } from '../shared/toast/toast.service';

export type UsuarioDTO = {
  id?: number;
  nombre?: string;
  apellido?: string;
  email?: string;
  telefono?: string;
  provinciaId?: string;
  departamentoId?: string;
  localidadId?: string;
  estado?: boolean;
  puntos?: number;
  rolId?: number;
};

export type LoginRequest = {
  email: string;
  password: string;
};

export type RegistroRequest = {
  nombre?: string;
  apellido?: string;
  email: string;
  password: string;
  telefono?: string;
  provinciaId?: string;
  departamentoId?: string;
  localidadId?: string;
};

// Cambiamos la respuesta esperada para login/registro: incluye token
type AuthResponse<T> = {
  mensaje: string;
  usuario: T;
  token: string;
};

const API_BASE = 'http://localhost:8080/api';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly platformId = inject(PLATFORM_ID);
  private readonly toast = inject(ToastService);

  readonly currentUser = signal<UsuarioDTO | null>(null);
  private token: string | null = null;

  constructor() {
    // SSR-safe init
    if (isPlatformBrowser(this.platformId)) {
      const stored = this.readAuthFromStorage();
      if (stored) {
        this.currentUser.set(stored.usuario);
        this.token = stored.token;
      }
    }
  }

  login(payload: LoginRequest): Observable<AuthResponse<UsuarioDTO>> {
    return this.http
      .post<AuthResponse<UsuarioDTO>>(`${API_BASE}/usuarios/login`, payload)
      .pipe(tap((res) => this.setAuth(res.token, res.usuario)));
  }

  registro(payload: RegistroRequest): Observable<AuthResponse<UsuarioDTO>> {
    return this.http
      .post<AuthResponse<UsuarioDTO>>(`${API_BASE}/usuarios/registro`, payload)
      .pipe(tap((res) => this.setAuth(res.token, res.usuario)));
  }

  logout(): void {
    const hadUser = this.currentUser() != null;
    if (isPlatformBrowser(this.platformId)) {
      localStorage.removeItem('auth');
    }
    this.currentUser.set(null);
    this.token = null;

    if (hadUser) {
      this.toast.success('Se ha cerrado sesión correctamente', { title: 'Sesión' });
    }
  }

  getCurrentUser(): UsuarioDTO | null {
    return this.currentUser();
  }

  getToken(): string | null {
    return this.token;
  }

  setCurrentUser(user: UsuarioDTO): void {
    this.currentUser.set(user);
    if (isPlatformBrowser(this.platformId)) {
      // preserve existing token if present
      const stored = this.readAuthFromStorage();
      const token = stored?.token ?? this.token;
      localStorage.setItem('auth', JSON.stringify({ token, usuario: user }));
    }
  }

  private setAuth(token: string, user: UsuarioDTO): void {
    this.token = token;
    this.currentUser.set(user);
    if (isPlatformBrowser(this.platformId)) {
      localStorage.setItem('auth', JSON.stringify({ token, usuario: user }));
    }
  }

  private readAuthFromStorage(): { token: string; usuario: UsuarioDTO } | null {
    try {
      const raw = localStorage.getItem('auth');
      if (!raw) return null;
      return JSON.parse(raw) as { token: string; usuario: UsuarioDTO };
    } catch {
      return null;
    }
  }
}
