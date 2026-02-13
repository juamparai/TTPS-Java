import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { UsuarioDTO } from './auth.service';
import { Mascota } from './mascota.service';

const API_BASE = 'http://localhost:8080/api';

@Injectable({ providedIn: 'root' })
export class AdminService {
  private readonly http = inject(HttpClient);

  // Usuarios
  getUsuarios(): Observable<UsuarioDTO[]> {
    return this.http.get<UsuarioDTO[]>(`${API_BASE}/usuarios`);
  }

  getUsuarioById(id: number): Observable<UsuarioDTO> {
    return this.http.get<UsuarioDTO>(`${API_BASE}/usuarios/${id}`);
  }

  bloquearUsuario(id: number, estado: boolean): Observable<{ mensaje: string; usuario: UsuarioDTO }> {
    return this.http.patch<{ mensaje: string; usuario: UsuarioDTO }>(
      `${API_BASE}/usuarios/${id}/estado`,
      { estado }
    );
  }

  eliminarUsuario(id: number): Observable<{ mensaje: string }> {
    return this.http.delete<{ mensaje: string }>(`${API_BASE}/usuarios/${id}`);
  }

  actualizarUsuario(id: number, usuario: Partial<UsuarioDTO>): Observable<{ mensaje: string; usuario: UsuarioDTO }> {
    return this.http.put<{ mensaje: string; usuario: UsuarioDTO }>(
      `${API_BASE}/usuarios/${id}`,
      usuario
    );
  }

  agregarPuntosUsuario(id: number, puntos: number): Observable<{ mensaje: string; usuario: UsuarioDTO }> {
    return this.http.patch<{ mensaje: string; usuario: UsuarioDTO }>(
      `${API_BASE}/usuarios/${id}/puntos`,
      { puntos }
    );
  }

  // Mascotas
  getMascotas(): Observable<Mascota[]> {
    return this.http.get<Mascota[]>(`${API_BASE}/mascotas`);
  }

  getMascotaById(id: number): Observable<Mascota> {
    return this.http.get<Mascota>(`${API_BASE}/mascotas/${id}`);
  }

  eliminarMascota(id: number): Observable<void> {
    return this.http.delete<void>(`${API_BASE}/mascotas/${id}`);
  }

  actualizarMascota(id: number, mascota: Partial<Mascota>): Observable<Mascota> {
    return this.http.put<Mascota>(`${API_BASE}/mascotas/${id}`, mascota);
  }
}

