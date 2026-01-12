import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

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
  ubicacion?: string; // Para mostrar el barrio/ciudad
};

@Component({
  selector: 'app-mascota-card',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './mascota-card.html',
  styleUrl: './mascota-card.css',
})
export class MascotaCard {
  @Input() mascota!: Mascota;

  constructor(private router: Router) {}

  verDetalle(): void {
    if (this.mascota.id) {
      this.router.navigateByUrl(`/mascota/${this.mascota.id}`);
    }
  }

  get inicialNombre(): string {
    return this.mascota.nombre?.charAt(0).toUpperCase() || '?';
  }
}

