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
  imagenUrl?: string;
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

  createMascotaWithImage(mascota: Mascota, imagen?: File): Observable<any> {
    const formData = new FormData();
    formData.append('mascota', JSON.stringify(mascota));

    if (imagen) {
      formData.append('imagen', imagen, imagen.name);
    }

    return this.http.post(`${API_BASE}/mascotas`, formData);
  }

  updateMascota(id: number, mascota: Mascota): Observable<Mascota> {
    return this.http.put<Mascota>(`${API_BASE}/mascotas/${id}`, mascota);
  }

  updateMascotaWithImage(id: number, mascota: Mascota, imagen?: File): Observable<any> {
    const formData = new FormData();
    formData.append('mascota', JSON.stringify(mascota));

    if (imagen) {
      formData.append('imagen', imagen, imagen.name);
    }

    return this.http.put(`${API_BASE}/mascotas/${id}`, formData);
  }

  deleteMascota(id: number): Observable<void> {
    return this.http.delete<void>(`${API_BASE}/mascotas/${id}`);
  }
}
