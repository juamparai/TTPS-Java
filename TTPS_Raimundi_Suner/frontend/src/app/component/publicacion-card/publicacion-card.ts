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
    // Si la publicación tiene municipioId, obtener el nombre desde Georef
    if (this.publicacion.municipioId) {
      this.obtenerNombreMunicipio(this.publicacion.municipioId);
    } else if (this.publicacion.lat && this.publicacion.lng) {
      // Fallback: Si tiene coordenadas pero no municipioId, obtener desde coordenadas
      this.georef.getUbicacionPorCoordenadas(this.publicacion.lat, this.publicacion.lng).subscribe({
        next: (ubicacion) => {
          const nombreUbicacion = ubicacion.municipio?.nombre || ubicacion.departamento?.nombre || 'Ubicación desconocida';
          this.ubicacionCargada.set(nombreUbicacion);
        },
        error: (err) => {
          console.error('Error al obtener ubicación:', err);
          this.ubicacionCargada.set('Ubicación desconocida');
        }
      });
    } else {
      // Si no hay municipioId ni coordenadas
      this.ubicacionCargada.set('Ubicación desconocida');
    }
  }

  private obtenerNombreMunicipio(municipioId: string): void {
    this.georef.getMunicipioPorId(municipioId).subscribe({
      next: (municipio) => {
        this.ubicacionCargada.set(municipio.nombre);
      },
      error: (err) => {
        console.error('Error al obtener municipio:', err);
        // Si falla, intentar con coordenadas si están disponibles
        if (this.publicacion.lat && this.publicacion.lng) {
          this.georef.getUbicacionPorCoordenadas(this.publicacion.lat, this.publicacion.lng).subscribe({
            next: (ubicacion) => {
              const nombreUbicacion = ubicacion.municipio?.nombre || ubicacion.departamento?.nombre || 'Ubicación desconocida';
              this.ubicacionCargada.set(nombreUbicacion);
            },
            error: () => {
              this.ubicacionCargada.set('Ubicación desconocida');
            }
          });
        } else {
          this.ubicacionCargada.set('Ubicación desconocida');
        }
      }
    });
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
    return this.ubicacionCargada() || 'Ubicación desconocida';
  }
}
