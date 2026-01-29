import { Component, OnInit, inject, signal, computed, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AdminService } from '../../services/admin.service';
import { UsuarioDTO, AuthService } from '../../services/auth.service';
import { GeorefService, type Localidad, type Provincia } from '../../services/georef.service';
import { ToastService } from '../../shared/toast/toast.service';

@Component({
  selector: 'app-admin-usuarios',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-usuarios.html',
  styleUrl: './admin-usuarios.css',
})
export class AdminUsuarios implements OnInit {
  private readonly adminService = inject(AdminService);
  private readonly authService = inject(AuthService);
  private readonly georef = inject(GeorefService);
  private readonly toast = inject(ToastService);
  private readonly router = inject(Router);
  private readonly platformId = inject(PLATFORM_ID);
  private readonly isBrowser = isPlatformBrowser(this.platformId);

  readonly usuarios = signal<UsuarioDTO[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  readonly provincias = signal<Provincia[]>([]);

  // Map userId -> municipio parts { loc, prov }
  readonly usuariosMunicipio = signal<Record<number, { loc?: string; prov?: string }>>({});

  // Modales
  readonly mostrarModalBloqueo = signal(false);
  readonly mostrarModalEliminar = signal(false);
  readonly usuarioSeleccionado = signal<UsuarioDTO | null>(null);

  // Filtros
  readonly filtroNombre = signal('');
  readonly filtroEmail = signal('');
  readonly filtroEstado = signal<'todos' | 'activos' | 'bloqueados'>('todos');

  // Paginación
  readonly currentPage = signal(1);
  readonly itemsPerPage = 15;

  // Filtrar usuarios: excluir admins (rolId === 0)
  readonly usuariosFiltrados = computed(() => {
    let result = this.usuarios().filter(u => u.rolId !== 0);

    const nombre = this.filtroNombre().toLowerCase().trim();
    if (nombre) {
      result = result.filter(u =>
        `${u.nombre} ${u.apellido}`.toLowerCase().includes(nombre)
      );
    }

    const email = this.filtroEmail().toLowerCase().trim();
    if (email) {
      result = result.filter(u => u.email?.toLowerCase().includes(email));
    }

    const estado = this.filtroEstado();
    if (estado === 'activos') {
      result = result.filter(u => u.estado === true);
    } else if (estado === 'bloqueados') {
      result = result.filter(u => u.estado === false);
    }

    return result;
  });

  readonly totalPages = computed(() =>
    Math.ceil(this.usuariosFiltrados().length / this.itemsPerPage)
  );

  readonly usuariosPaginados = computed(() => {
    const start = (this.currentPage() - 1) * this.itemsPerPage;
    const end = start + this.itemsPerPage;
    return this.usuariosFiltrados().slice(start, end);
  });

  readonly currentUserId = computed(() => this.authService.currentUser()?.id);

  ngOnInit(): void {
    if (this.isBrowser) {
      this.loadUsuarios();
    }
  }

  loadUsuarios(): void {
    this.loading.set(true);
    this.error.set(null);

    // Debug: show current token / user before making API call
    try {
      // eslint-disable-next-line no-console
      console.debug('[AdminUsuarios] loadUsuarios token=', this.authService.getToken(), 'currentUser=', this.authService.currentUser());
    } catch {}

    this.adminService.getUsuarios().subscribe({
      next: (usuarios) => {
        this.usuarios.set(usuarios);
        // ensure we have province names, then resolve municipios
        this.georef.getProvincias().subscribe({
          next: (provs) => {
            this.provincias.set(provs);
            this.resolveMunicipios(usuarios);
          },
          error: () => {
            this.provincias.set([]);
            this.resolveMunicipios(usuarios);
          }
        });
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error al cargar usuarios:', err);
        this.error.set('No se pudieron cargar los usuarios');
        this.loading.set(false);
      },
    });
  }

  private resolveMunicipios(usuarios: UsuarioDTO[]): void {
    const buckets = new Map<string, UsuarioDTO[]>();

    // initialize defaults
    const initialMap: Record<number, { loc?: string; prov?: string }> = {};
    usuarios.forEach(u => {
      initialMap[u.id as number] = { loc: '-', prov: '-' };
      if (u.provinciaId && u.departamentoId && u.localidadId) {
        const key = `${u.provinciaId}|${u.departamentoId}`;
        const arr = buckets.get(key) ?? [];
        arr.push(u);
        buckets.set(key, arr);
      }
    });
    this.usuariosMunicipio.set(initialMap);

    // For each provincia|departamento pair, fetch localidades once
    buckets.forEach((users, key) => {
      const [provinciaId, departamentoId] = key.split('|');
      this.georef.getLocalidades(provinciaId, departamentoId).subscribe({
        next: (localidades: Localidad[]) => {
          const updated = { ...this.usuariosMunicipio() };
          const provinciaNombre = this.provincias().find(p => p.id === provinciaId)?.nombre ?? provinciaId;
          users.forEach(u => {
            const found = localidades.find(l => l.id === u.localidadId);
            updated[u.id as number] = found ? { loc: found.nombre, prov: provinciaNombre } : { loc: (u.localidadId || '-'), prov: provinciaNombre };
          });
          this.usuariosMunicipio.set(updated);
        },
        error: () => {
          const updated = { ...this.usuariosMunicipio() };
          const provinciaNombre = this.provincias().find(p => p.id === provinciaId)?.nombre ?? provinciaId;
          users.forEach(u => { updated[u.id as number] = { loc: (u.localidadId || '-'), prov: provinciaNombre }; });
          this.usuariosMunicipio.set(updated);
        }
      });
    });
  }

  getMunicipioParts(usuario: UsuarioDTO): { loc: string; prov: string } {
    const id = usuario.id ?? -1;
    const map = this.usuariosMunicipio();
    const entry = map && map[id];
    return { loc: entry?.loc ?? '-', prov: entry?.prov ?? '-' };
  }

  onFiltroNombreChange(event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    this.filtroNombre.set(value);
    this.currentPage.set(1);
  }

  onFiltroEmailChange(event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    this.filtroEmail.set(value);
    this.currentPage.set(1);
  }

  onFiltroEstadoChange(event: Event): void {
    const value = (event.target as HTMLSelectElement).value as 'todos' | 'activos' | 'bloqueados';
    this.filtroEstado.set(value);
    this.currentPage.set(1);
  }

  limpiarFiltros(): void {
    this.filtroNombre.set('');
    this.filtroEmail.set('');
    this.filtroEstado.set('todos');
    this.currentPage.set(1);
  }

  goToPage(page: number): void {
    if (page >= 1 && page <= this.totalPages()) {
      this.currentPage.set(page);
    }
  }

  editarUsuario(usuario: UsuarioDTO): void {
    this.router.navigate(['/admin/usuarios', usuario.id, 'editar']);
  }

  // Modales
  confirmarBloqueo(usuario: UsuarioDTO): void {
    this.usuarioSeleccionado.set(usuario);
    this.mostrarModalBloqueo.set(true);
  }

  confirmarEliminar(usuario: UsuarioDTO): void {
    this.usuarioSeleccionado.set(usuario);
    this.mostrarModalEliminar.set(true);
  }

  cerrarModal(): void {
    this.mostrarModalBloqueo.set(false);
    this.mostrarModalEliminar.set(false);
    this.usuarioSeleccionado.set(null);
  }

  ejecutarBloqueo(): void {
    const usuario = this.usuarioSeleccionado();
    if (!usuario?.id) return;

    const nuevoEstado = !usuario.estado;

    this.adminService.bloquearUsuario(usuario.id, nuevoEstado).subscribe({
      next: (res) => {
        this.toast.success(res.mensaje);
        this.usuarios.update(users =>
          users.map(u => u.id === usuario.id ? { ...u, estado: nuevoEstado } : u)
        );
        this.cerrarModal();
      },
      error: (err) => {
        console.error('Error al cambiar estado:', err);
        this.toast.error(err.error?.error || 'Error al cambiar estado del usuario');
        this.cerrarModal();
      },
    });
  }

  ejecutarEliminar(): void {
    const usuario = this.usuarioSeleccionado();
    if (!usuario?.id) return;

    this.adminService.eliminarUsuario(usuario.id).subscribe({
      next: () => {
        this.toast.success('Usuario eliminado exitosamente');
        this.usuarios.update(users => users.filter(u => u.id !== usuario.id));
        this.cerrarModal();
      },
      error: (err) => {
        console.error('Error al eliminar usuario:', err);
        this.toast.error(err.error?.error || 'Error al eliminar usuario');
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

  getMunicipio(usuario: UsuarioDTO): string {
    const parts = this.getMunicipioParts(usuario);
    if (!parts.loc && !parts.prov) return '-';
    if (parts.loc && parts.prov) return `${parts.loc} (${parts.prov})`;
    return parts.loc || parts.prov || '-';
  }
}
