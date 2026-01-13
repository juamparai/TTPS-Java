import { Component, Input, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

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
  ubicacion?: string; // Para mostrar el barrio/ciudad
  esMia?: boolean; // Indica si la mascota pertenece al usuario autenticado
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
  @Input() showEditButton: boolean = false;

  private readonly router = inject(Router);
  private readonly auth = inject(AuthService);

  editarMascota(): void {
    if (this.mascota.id) {
      this.router.navigateByUrl(`/mascotas/${this.mascota.id}/editar`);
    }
  }

  get inicialNombre(): string {
    return this.mascota.nombre?.charAt(0).toUpperCase() || '?';
  }

  get imageSrc(): string | null {
    const url = this.mascota.imagenUrl;
    if (!url) return null;
    if (url.startsWith('http://') || url.startsWith('https://')) return url;
    return `http://localhost:8080${url}`;
  }

  get isOwner(): boolean {
    const currentUser = this.auth.currentUser();
    console.log('Verificando owner:', {
      currentUserId: currentUser?.id,
      mascotaUserId: this.mascota.usuarioId,
      mascota: this.mascota
    });
    return currentUser?.id === this.mascota.usuarioId;
  }

  get shouldShowEditButton(): boolean {
    return this.showEditButton && this.isOwner;
  }

  get estadoMascota(): string {
    if (!this.mascota.estadoMascota) return 'Sin estado';

    switch (this.mascota.estadoMascota) {
      case 'ADOPTADA':
        return 'Adoptada';
      case 'PERDIDA_PROPIA':
        return 'Perdida';
      case 'PERDIDA_AJENA':
        return 'Perdida (Ajena)';
      default:
        return this.mascota.estadoMascota;
    }
  }

  get estadoClass(): string {
    if (!this.mascota.estadoMascota) return '';

    switch (this.mascota.estadoMascota) {
      case 'ADOPTADA':
        return 'estado-adoptada';
      case 'PERDIDA_PROPIA':
        return 'estado-perdida-propia';
      case 'PERDIDA_AJENA':
        return 'estado-perdida-ajena';
      default:
        return '';
    }
  }
}
