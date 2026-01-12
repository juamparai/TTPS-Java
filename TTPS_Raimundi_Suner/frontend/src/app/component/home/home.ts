import { Component, inject, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { PublicacionCard, type Publicacion } from '../publicacion-card/publicacion-card';
import { ToastService } from '../../shared/toast/toast.service';
import { GeorefService, type Provincia, type Departamento, type Localidad } from '../../services/georef.service';
import { HttpClient } from '@angular/common/http';

const API_BASE = 'http://localhost:8080/api';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, PublicacionCard, FormsModule],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class Home implements OnInit {
  private readonly toast = inject(ToastService);
  private readonly georefService = inject(GeorefService);
  private readonly router = inject(Router);
  private readonly http = inject(HttpClient);

  readonly publicaciones = signal<Publicacion[]>([]);
  readonly loading = signal(true);

  // Filtros
  readonly tipoFiltro = signal<string>('');
  readonly provinciaIdFiltro = signal<string>('');
  readonly departamentoIdFiltro = signal<string>('');
  readonly localidadIdFiltro = signal<string>('');

  // Datos para los selects
  readonly provincias = signal<Provincia[]>([]);
  readonly departamentos = signal<Departamento[]>([]);
  readonly localidades = signal<Localidad[]>([]);

  // Tipos de mascotas únicos
  readonly tiposMascota = computed(() => {
    const tipos = new Set(this.publicaciones().map(p => p.mascotaTipo).filter(Boolean));
    return Array.from(tipos).sort();
  });

  // Publicaciones filtradas
  readonly publicacionesFiltradas = computed(() => {
    let resultado = this.publicaciones();

    // Filtrar por tipo
    if (this.tipoFiltro()) {
      resultado = resultado.filter(p => p.mascotaTipo === this.tipoFiltro());
    }

    // TODO: Filtrar por ubicación cuando tengamos esos datos en Publicacion
    // Por ahora solo filtramos por tipo

    return resultado;
  });

  ngOnInit(): void {
    this.cargarPublicaciones();
    this.cargarProvincias();
  }

  cargarPublicaciones(): void {
    this.loading.set(true);
    this.http.get<any[]>(`${API_BASE}/publicaciones`).subscribe({
      next: async (data) => {
        // Filtrar solo publicaciones activas
        const publicacionesActivas = data.filter(p => p.estadoPublicacion === 'ACTIVA');

        // Enriquecer con nombres de ubicación
        const publicacionesEnriquecidas = await Promise.all(
          publicacionesActivas.map(async (pub) => {
            let nombreLocalidad = 'Ubicación desconocida';

            // Obtener el nombre de la localidad desde Georef si tenemos los IDs
            if (pub.usuario?.provinciaId && pub.usuario?.departamentoId && pub.usuario?.localidadId) {
              try {
                const localidades = await this.georefService.getLocalidades(
                  pub.usuario.provinciaId,
                  pub.usuario.departamentoId
                ).toPromise();

                const localidad = localidades?.find(l => l.id === pub.usuario.localidadId);
                if (localidad) {
                  nombreLocalidad = localidad.nombre;
                }
              } catch (err) {
                console.error('Error al obtener nombre de localidad:', err);
              }
            }

            return {
              ...pub,
              mascotaNombre: pub.mascota?.nombre || 'Sin nombre',
              mascotaTipo: pub.mascota?.tipo || 'Desconocido',
              localidad: nombreLocalidad,
            };
          })
        );

        this.publicaciones.set(publicacionesEnriquecidas);
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        console.error('Error al cargar publicaciones:', err);
        this.toast.error('No se pudieron cargar las publicaciones', { title: 'Error' });
      },
    });
  }

  private async enrichPublicacion(pub: any): Promise<Publicacion> {
    // Este método ya no es necesario, pero lo dejamos por compatibilidad
    return {
      ...pub,
      mascotaNombre: pub.mascota?.nombre || 'Sin nombre',
      mascotaTipo: pub.mascota?.tipo || 'Desconocido',
      localidad: pub.usuario?.localidadId || 'Ubicación desconocida',
    };
  }

  cargarProvincias(): void {
    this.georefService.getProvincias().subscribe({
      next: (data) => this.provincias.set(data),
      error: (err) => {
        console.error('Error al cargar provincias:', err);
        this.toast.error('No se pudieron cargar las provincias', { title: 'Error' });
      },
    });
  }

  onProvinciaChange(): void {
    this.departamentoIdFiltro.set('');
    this.localidadIdFiltro.set('');
    this.departamentos.set([]);
    this.localidades.set([]);

    if (this.provinciaIdFiltro()) {
      this.georefService.getDepartamentos(this.provinciaIdFiltro()).subscribe({
        next: (data) => this.departamentos.set(data),
        error: (err) => {
          console.error('Error al cargar departamentos:', err);
          this.toast.error('No se pudieron cargar los departamentos', { title: 'Error' });
        },
      });
    }
  }

  onDepartamentoChange(): void {
    this.localidadIdFiltro.set('');
    this.localidades.set([]);

    if (this.provinciaIdFiltro() && this.departamentoIdFiltro()) {
      this.georefService.getLocalidades(this.provinciaIdFiltro(), this.departamentoIdFiltro()).subscribe({
        next: (data) => this.localidades.set(data),
        error: (err) => {
          console.error('Error al cargar localidades:', err);
          this.toast.error('No se pudieron cargar las localidades', { title: 'Error' });
        },
      });
    }
  }

  limpiarFiltros(): void {
    this.tipoFiltro.set('');
    this.provinciaIdFiltro.set('');
    this.departamentoIdFiltro.set('');
    this.localidadIdFiltro.set('');
    this.departamentos.set([]);
    this.localidades.set([]);
  }

  crearPublicacion(): void {
    this.router.navigate(['/publicaciones/crear']);
  }
}
