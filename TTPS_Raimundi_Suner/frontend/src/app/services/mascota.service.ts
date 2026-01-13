import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export type Mascota = {
  id?: number;
  nombre: string;
  tipo: string;
  raza?: string;
  color?: string;
  tamanio?: string;
  descripcion?: string;
  fechaNac?: string;
  estadoMascota?: string;
  usuarioId?: number;
};

const API_BASE = 'http://localhost:8080/api';

@Injectable({ providedIn: 'root' })
export class MascotaService {
  private readonly http = inject(HttpClient);

  getMascotas(): Observable<Mascota[]> {
    return this.http.get<Mascota[]>(`${API_BASE}/mascotas`);
  }

  getMascotaById(id: number): Observable<Mascota> {
    return this.http.get<Mascota>(`${API_BASE}/mascotas/${id}`);
  }

  getMascotasByUsuario(usuarioId: number): Observable<Mascota[]> {
    return this.http.get<Mascota[]>(`${API_BASE}/mascotas/usuario/${usuarioId}`);
  }

  createMascota(mascota: Mascota): Observable<Mascota> {
    return this.http.post<Mascota>(`${API_BASE}/mascotas`, mascota);
  }

  updateMascota(id: number, mascota: Mascota): Observable<Mascota> {
    return this.http.put<Mascota>(`${API_BASE}/mascotas/${id}`, mascota);
  }

  deleteMascota(id: number): Observable<void> {
    return this.http.delete<void>(`${API_BASE}/mascotas/${id}`);
  }
}
