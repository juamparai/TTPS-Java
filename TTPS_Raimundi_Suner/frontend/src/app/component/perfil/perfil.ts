import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { MascotaService } from '../../services/mascota.service';
import { PublicacionService } from '../../services/publicacion.service';
import { MascotaCard, Mascota } from '../mascota-card/mascota-card';
import { PublicacionCard, Publicacion } from '../publicacion-card/publicacion-card';

@Component({
  selector: 'app-perfil',
  standalone: true,
  imports: [CommonModule, MascotaCard, PublicacionCard],
  templateUrl: './perfil.html',
  styleUrl: './perfil.css',
})
export class Perfil implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly mascotaService = inject(MascotaService);
  private readonly publicacionService = inject(PublicacionService);
  private readonly router = inject(Router);

  readonly user = this.auth.currentUser;
  readonly mascotas = signal<Mascota[]>([]);
  readonly publicaciones = signal<Publicacion[]>([]);
  readonly loading = signal(true);
  readonly loadingPublicaciones = signal(true);
  readonly error = signal<string | null>(null);
  readonly errorPublicaciones = signal<string | null>(null);

  // Carrusel de mascotas
  readonly mascotasCurrentIndex = signal(0);
  readonly mascotasPerPage = 3;

  // Carrusel de publicaciones
  readonly publicacionesCurrentIndex = signal(0);
  readonly publicacionesPerPage = 3;

  ngOnInit(): void {
    const currentUser = this.user();
    if (!currentUser?.id) {
      this.router.navigateByUrl('/login');
      return;
    }

    this.loadMascotas(currentUser.id);
    this.loadPublicaciones(currentUser.id);
  }

  private loadMascotas(usuarioId: number): void {
    this.loading.set(true);
    this.error.set(null);

    this.mascotaService.getMascotasByUsuario(usuarioId).subscribe({
      next: (mascotas) => {
        console.log('Mascotas cargadas:', mascotas);
        this.mascotas.set(mascotas);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error al cargar mascotas:', err);
        this.error.set('No se pudieron cargar tus mascotas');
        this.loading.set(false);
      },
    });
  }

  private loadPublicaciones(usuarioId: number): void {
    this.loadingPublicaciones.set(true);
    this.errorPublicaciones.set(null);

    this.publicacionService.getPublicacionesByUsuario(usuarioId).subscribe({
      next: (publicaciones) => {
        console.log('Publicaciones cargadas:', publicaciones);
        this.publicaciones.set(publicaciones);
        this.loadingPublicaciones.set(false);
      },
      error: (err) => {
        console.error('Error al cargar publicaciones:', err);
        this.errorPublicaciones.set('No se pudieron cargar tus publicaciones');
        this.loadingPublicaciones.set(false);
      },
    });
  }

  get iniciales(): string {
    const u = this.user();
    if (!u) return '?';
    const nombre = u.nombre?.charAt(0).toUpperCase() || '';
    const apellido = u.apellido?.charAt(0).toUpperCase() || '';
    return nombre + apellido || u.email?.charAt(0).toUpperCase() || '?';
  }

  get nombreCompleto(): string {
    const u = this.user();
    if (!u) return 'Usuario';
    const name = [u.nombre, u.apellido].filter(Boolean).join(' ').trim();
    return name || u.email || 'Usuario';
  }

  agregarMascota(): void {
    this.router.navigateByUrl('/mascotas/crear');
  }

  editarPerfil(): void {
    this.router.navigateByUrl('/perfil/editar');
  }

  // Métodos para carrusel de mascotas
  get mascotasVisible(): Mascota[] {
    const start = this.mascotasCurrentIndex();
    return this.mascotas().slice(start, start + this.mascotasPerPage);
  }

  get canPrevMascotas(): boolean {
    return this.mascotasCurrentIndex() > 0;
  }

  get canNextMascotas(): boolean {
    return this.mascotasCurrentIndex() + this.mascotasPerPage < this.mascotas().length;
  }

  prevMascotas(): void {
    if (this.canPrevMascotas) {
      this.mascotasCurrentIndex.update(i => Math.max(0, i - this.mascotasPerPage));
    }
  }

  nextMascotas(): void {
    if (this.canNextMascotas) {
      this.mascotasCurrentIndex.update(i => i + this.mascotasPerPage);
    }
  }

  // Métodos para carrusel de publicaciones
  get publicacionesVisible(): Publicacion[] {
    const start = this.publicacionesCurrentIndex();
    return this.publicaciones().slice(start, start + this.publicacionesPerPage);
  }

  get canPrevPublicaciones(): boolean {
    return this.publicacionesCurrentIndex() > 0;
  }

  get canNextPublicaciones(): boolean {
    return this.publicacionesCurrentIndex() + this.publicacionesPerPage < this.publicaciones().length;
  }

  prevPublicaciones(): void {
    if (this.canPrevPublicaciones) {
      this.publicacionesCurrentIndex.update(i => Math.max(0, i - this.publicacionesPerPage));
    }
  }

  nextPublicaciones(): void {
    if (this.canNextPublicaciones) {
      this.publicacionesCurrentIndex.update(i => i + this.publicacionesPerPage);
    }
  }
}
