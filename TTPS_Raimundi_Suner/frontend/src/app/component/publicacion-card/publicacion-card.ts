import { Component, Input, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { GeorefService } from '../../services/georef.service';

export type Publicacion = {
  id?: number;
  descripcion: string;
  fecha?: string;
  fechaCierre?: string;
  estadoPublicacion?: string;
  lat?: number;
  lng?: number;
  municipioId?: string;
  mascotaId?: number;
  usuarioId?: number;
  // Datos populados
  mascotaNombre?: string;
  mascotaTipo?: string;
  localidad?: string;
};

@Component({
  selector: 'app-publicacion-card',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './publicacion-card.html',
  styleUrl: './publicacion-card.css',
})
export class PublicacionCard implements OnInit {
  @Input() publicacion!: Publicacion;

  private readonly router = inject(Router);
  private readonly georef = inject(GeorefService);

  readonly ubicacionCargada = signal<string>('');

  ngOnInit(): void {
    // Si la publicación tiene coordenadas, obtener el municipio
    if (this.publicacion.lat && this.publicacion.lng) {
      this.georef.getUbicacionPorCoordenadas(this.publicacion.lat, this.publicacion.lng).subscribe({
        next: (ubicacion) => {
          // Priorizar municipio, si no hay usar departamento
          const nombreUbicacion = ubicacion.municipio?.nombre || ubicacion.departamento?.nombre || '';
          this.ubicacionCargada.set(nombreUbicacion);
        },
        error: (err) => {
          console.error('Error al obtener ubicación:', err);
          this.ubicacionCargada.set(this.publicacion.localidad || '');
        }
      });
    } else {
      // Si no hay coordenadas, usar el campo localidad si existe
      this.ubicacionCargada.set(this.publicacion.localidad || '');
    }
  }

  verDetalle(): void {
    if (this.publicacion.id) {
      this.router.navigateByUrl(`/publicacion/${this.publicacion.id}`);
    }
  }

  get inicialNombre(): string {
    return this.publicacion.mascotaNombre?.charAt(0).toUpperCase() || '?';
  }

  get localidadMostrar(): string {
    return this.ubicacionCargada() || this.publicacion.localidad || '';
  }
}
