import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AdminService } from '../../services/admin.service';
import { Mascota } from '../../services/mascota.service';
import { UsuarioDTO } from '../../services/auth.service';
import { ToastService } from '../../shared/toast/toast.service';

@Component({
  selector: 'app-admin-mascotas',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-mascotas.html',
  styleUrl: './admin-mascotas.css',
})
export class AdminMascotas implements OnInit {
  private readonly adminService = inject(AdminService);
  private readonly toast = inject(ToastService);
  private readonly router = inject(Router);

  readonly mascotas = signal<Mascota[]>([]);
  readonly usuarios = signal<Map<number, UsuarioDTO>>(new Map());
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  // Modal de eliminar
  readonly mostrarModalEliminar = signal(false);
  readonly mascotaAEliminar = signal<Mascota | null>(null);

  // Filtros
  readonly filtroNombre = signal('');
  readonly filtroTipo = signal<string>('todos');
  readonly filtroEstado = signal<string>('todos');

  // Paginación
  readonly currentPage = signal(1);
  readonly itemsPerPage = 15;

  readonly tiposUnicos = computed(() => {
    const tipos = new Set(this.mascotas().map(m => m.tipo).filter(Boolean));
    return Array.from(tipos).sort();
  });

  readonly estadosUnicos = computed(() => {
    const estados = new Set(this.mascotas().map(m => m.estadoMascota).filter(Boolean));
    return Array.from(estados).sort();
  });

  readonly mascotasFiltradas = computed(() => {
    let result = this.mascotas();

    const nombre = this.filtroNombre().toLowerCase().trim();
    if (nombre) {
      result = result.filter(m =>
        m.nombre?.toLowerCase().includes(nombre)
      );
    }

    const tipo = this.filtroTipo();
    if (tipo !== 'todos') {
      result = result.filter(m => m.tipo === tipo);
    }

    const estado = this.filtroEstado();
    if (estado !== 'todos') {
      result = result.filter(m => m.estadoMascota === estado);
    }

    return result;
  });

  readonly totalPages = computed(() =>
    Math.ceil(this.mascotasFiltradas().length / this.itemsPerPage)
  );

  readonly mascotasPaginadas = computed(() => {
    const start = (this.currentPage() - 1) * this.itemsPerPage;
    const end = start + this.itemsPerPage;
    return this.mascotasFiltradas().slice(start, end);
  });

  ngOnInit(): void {
    this.loadMascotas();
    this.loadUsuarios();
  }

  loadMascotas(): void {
    this.loading.set(true);
    this.error.set(null);

    this.adminService.getMascotas().subscribe({
      next: (mascotas) => {
        this.mascotas.set(mascotas);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error al cargar mascotas:', err);
        this.error.set('No se pudieron cargar las mascotas');
        this.loading.set(false);
      },
    });
  }

  private loadUsuarios(): void {
    this.adminService.getUsuarios().subscribe({
      next: (usuarios) => {
        const map = new Map<number, UsuarioDTO>();
        usuarios.forEach(u => {
          if (u.id) map.set(u.id, u);
        });
        this.usuarios.set(map);
      },
      error: (err) => {
        console.error('Error al cargar usuarios:', err);
      },
    });
  }

  getNombreDueno(mascota: Mascota): string {
    if (!mascota.usuarioId) return 'Desconocido';
    const usuario = this.usuarios().get(mascota.usuarioId);
    if (!usuario) return 'Desconocido';
    const nombre = [usuario.nombre, usuario.apellido].filter(Boolean).join(' ').trim();
    return nombre || usuario.email || 'Desconocido';
  }

  onFiltroNombreChange(event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    this.filtroNombre.set(value);
    this.currentPage.set(1);
  }

  onFiltroTipoChange(event: Event): void {
    const value = (event.target as HTMLSelectElement).value;
    this.filtroTipo.set(value);
    this.currentPage.set(1);
  }

  onFiltroEstadoChange(event: Event): void {
    const value = (event.target as HTMLSelectElement).value;
    this.filtroEstado.set(value);
    this.currentPage.set(1);
  }

  limpiarFiltros(): void {
    this.filtroNombre.set('');
    this.filtroTipo.set('todos');
    this.filtroEstado.set('todos');
    this.currentPage.set(1);
  }

  goToPage(page: number): void {
    if (page >= 1 && page <= this.totalPages()) {
      this.currentPage.set(page);
    }
  }

  editarMascota(mascota: Mascota): void {
    if (mascota.id) {
      this.router.navigate(['/mascotas', mascota.id, 'editar']);
    }
  }

  confirmarEliminar(mascota: Mascota): void {
    this.mascotaAEliminar.set(mascota);
    this.mostrarModalEliminar.set(true);
  }

  cerrarModal(): void {
    this.mostrarModalEliminar.set(false);
    this.mascotaAEliminar.set(null);
  }

  confirmarEliminarMascota(): void {
    const mascota = this.mascotaAEliminar();
    if (!mascota?.id) return;

    this.adminService.eliminarMascota(mascota.id).subscribe({
      next: () => {
        this.toast.success('Mascota eliminada exitosamente');
        this.mascotas.update(list => list.filter(m => m.id !== mascota.id));
        this.cerrarModal();
      },
      error: (err) => {
        console.error('Error al eliminar mascota:', err);
        this.toast.error(err.error?.error || 'Error al eliminar mascota');
        this.cerrarModal();
      },
    });
  }

  getPages(): number[] {
    const total = this.totalPages();
    const current = this.currentPage();
    const pages: number[] = [];

    let start = Math.max(1, current - 2);
    let end = Math.min(total, current + 2);

    if (end - start < 4) {
      if (start === 1) {
        end = Math.min(total, 5);
      } else {
        start = Math.max(1, total - 4);
      }
    }

    for (let i = start; i <= end; i++) {
      pages.push(i);
    }

    return pages;
  }

  getEstadoClass(estado: string | undefined): string {
    if (!estado) return '';
    const estadoLower = estado.toLowerCase();
    if (estadoLower.includes('perdid')) return 'perdida';
    if (estadoLower.includes('encontrad')) return 'encontrada';
    if (estadoLower.includes('casa') || estadoLower.includes('hogar')) return 'en-casa';
    return '';
  }

  formatEstado(estado: string | undefined): string {
    if (!estado) return '-';
    return estado.replace(/_/g, ' ');
  }
}
