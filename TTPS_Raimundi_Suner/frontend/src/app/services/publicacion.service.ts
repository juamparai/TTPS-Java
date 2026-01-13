import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { Publicacion } from '../component/publicacion-card/publicacion-card';

const API_BASE = 'http://localhost:8080/api';

@Injectable({
  providedIn: 'root',
})
export class PublicacionService {
  private readonly http = inject(HttpClient);

  getAllPublicaciones(): Observable<Publicacion[]> {
    return this.http.get<Publicacion[]>(`${API_BASE}/publicaciones`);
  }

  getPublicacionesByUsuario(usuarioId: number): Observable<Publicacion[]> {
    return this.http.get<Publicacion[]>(`${API_BASE}/publicaciones`).pipe(
      map(publicaciones => publicaciones.filter(p => p.usuarioId === usuarioId))
    );
  }

  getPublicacionById(id: number): Observable<Publicacion> {
    return this.http.get<Publicacion>(`${API_BASE}/publicaciones/${id}`);
  }

  createPublicacion(publicacion: Publicacion): Observable<any> {
    return this.http.post(`${API_BASE}/publicaciones`, publicacion);
  }

  updatePublicacion(id: number, publicacion: Publicacion): Observable<any> {
    return this.http.put(`${API_BASE}/publicaciones/${id}`, publicacion);
  }

  deletePublicacion(id: number): Observable<any> {
    return this.http.delete(`${API_BASE}/publicaciones/${id}`);
  }
}

