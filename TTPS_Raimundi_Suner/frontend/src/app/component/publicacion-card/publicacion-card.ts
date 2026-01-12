import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

export type Publicacion = {
  id?: number;
  descripcion: string;
  fecha?: string;
  fechaCierre?: string;
  estadoPublicacion?: string;
  lat?: number;
  lng?: number;
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
export class PublicacionCard {
  @Input() publicacion!: Publicacion;

  constructor(private router: Router) {}

  verDetalle(): void {
    if (this.publicacion.id) {
      this.router.navigateByUrl(`/publicacion/${this.publicacion.id}`);
    }
  }

  get inicialNombre(): string {
    return this.publicacion.mascotaNombre?.charAt(0).toUpperCase() || '?';
  }
}

