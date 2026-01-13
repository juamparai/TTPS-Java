import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { MascotaService } from '../../services/mascota.service';
import { MascotaCard, Mascota } from '../mascota-card/mascota-card';

@Component({
  selector: 'app-perfil',
  standalone: true,
  imports: [CommonModule, MascotaCard],
  templateUrl: './perfil.html',
  styleUrl: './perfil.css',
})
export class Perfil implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly mascotaService = inject(MascotaService);
  private readonly router = inject(Router);

  readonly user = this.auth.currentUser;
  readonly mascotas = signal<Mascota[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  ngOnInit(): void {
    const currentUser = this.user();
    if (!currentUser?.id) {
      this.router.navigateByUrl('/login');
      return;
    }

    this.loadMascotas(currentUser.id);
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
}
