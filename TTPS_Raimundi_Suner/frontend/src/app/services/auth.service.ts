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

type ApiResponse<T> = {
  mensaje: string;
  usuario: T;
};

const API_BASE = 'http://localhost:8080/api';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly platformId = inject(PLATFORM_ID);
  private readonly toast = inject(ToastService);

  readonly currentUser = signal<UsuarioDTO | null>(null);

  constructor() {
    // SSR-safe init
    if (isPlatformBrowser(this.platformId)) {
      this.currentUser.set(this.readCurrentUserFromStorage());
    }
  }

  login(payload: LoginRequest): Observable<ApiResponse<UsuarioDTO>> {
    return this.http
      .post<ApiResponse<UsuarioDTO>>(`${API_BASE}/usuarios/login`, payload)
      .pipe(tap((res) => this.setCurrentUser(res.usuario)));
  }

  registro(payload: RegistroRequest): Observable<ApiResponse<UsuarioDTO>> {
    return this.http
      .post<ApiResponse<UsuarioDTO>>(`${API_BASE}/usuarios/registro`, payload)
      .pipe(tap((res) => this.setCurrentUser(res.usuario)));
  }

  logout(): void {
    const hadUser = this.currentUser() != null;
    if (isPlatformBrowser(this.platformId)) {
      localStorage.removeItem('currentUser');
    }
    this.currentUser.set(null);

    if (hadUser) {
      this.toast.success('Se ha cerrado sesión correctamente', { title: 'Sesión' });
    }
  }

  getCurrentUser(): UsuarioDTO | null {
    return this.currentUser();
  }

  private setCurrentUser(user: UsuarioDTO): void {
    this.currentUser.set(user);
    if (isPlatformBrowser(this.platformId)) {
      localStorage.setItem('currentUser', JSON.stringify(user));
    }
  }

  private readCurrentUserFromStorage(): UsuarioDTO | null {
    const raw = localStorage.getItem('currentUser');
    if (!raw) return null;
    try {
      return JSON.parse(raw) as UsuarioDTO;
    } catch {
      return null;
    }
  }
}
