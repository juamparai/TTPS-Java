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
  readonly municipioIdFiltro = signal<string>('');
  readonly fechaFiltro = signal<string>('todas');

  // Datos para los selects
  readonly provincias = signal<Provincia[]>([]);
  readonly municipios = signal<{ id: string; nombre: string }[]>([]);

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

    // Filtrar por provincia
    if (this.provinciaIdFiltro()) {
      resultado = resultado.filter(p => {
        // Obtener la provincia del municipioId de la publicación
        // El municipioId tiene formato que podemos usar para verificar
        const municipio = this.municipios().find(m => m.id === p.municipioId);
        // Si encontramos el municipio en la lista cargada, significa que es de la provincia seleccionada
        return municipio !== undefined;
      });
    }

    // Filtrar por municipio
    if (this.municipioIdFiltro()) {
      resultado = resultado.filter(p => p.municipioId === this.municipioIdFiltro());
    }

    // Filtrar por fecha
    if (this.fechaFiltro() !== 'todas') {
      const hoy = new Date();
      hoy.setHours(0, 0, 0, 0);

      resultado = resultado.filter(p => {
        if (!p.fecha) return false;

        // Parsear la fecha correctamente (puede venir como string "YYYY-MM-DD")
        const fechaStr = p.fecha;
        let fechaPublicacion: Date;

        if (typeof fechaStr === 'string') {
          // Si viene como string "YYYY-MM-DD", parsearlo correctamente
          const partes = fechaStr.split('-');
          if (partes.length === 3) {
            fechaPublicacion = new Date(parseInt(partes[0]), parseInt(partes[1]) - 1, parseInt(partes[2]));
          } else {
            fechaPublicacion = new Date(fechaStr);
          }
        } else {
          fechaPublicacion = new Date(fechaStr);
        }

        fechaPublicacion.setHours(0, 0, 0, 0);

        switch (this.fechaFiltro()) {
          case 'hoy': {
            // Comparar año, mes y día
            const esHoy = fechaPublicacion.getFullYear() === hoy.getFullYear() &&
                          fechaPublicacion.getMonth() === hoy.getMonth() &&
                          fechaPublicacion.getDate() === hoy.getDate();
            console.log('Comparando fecha hoy:', {
              fecha: fechaStr,
              fechaPublicacion,
              hoy,
              esHoy
            });
            return esHoy;
          }

          case 'semana': {
            // Esta semana incluye hoy
            const inicioSemana = new Date(hoy);
            inicioSemana.setDate(hoy.getDate() - hoy.getDay()); // Inicio de la semana (domingo)
            return fechaPublicacion >= inicioSemana && fechaPublicacion <= hoy;
          }

          case 'mes': {
            // Este mes incluye esta semana
            const inicioMes = new Date(hoy.getFullYear(), hoy.getMonth(), 1);
            return fechaPublicacion >= inicioMes && fechaPublicacion <= hoy;
          }

          case 'anteriores': {
            // Anteriores: hace más de un mes
            const haceUnMes = new Date(hoy);
            haceUnMes.setMonth(hoy.getMonth() - 1);
            return fechaPublicacion < haceUnMes;
          }

          default:
            return true;
        }
      });
    }

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
    this.municipioIdFiltro.set('');
    this.municipios.set([]);

    if (this.provinciaIdFiltro()) {
      this.georefService.getMunicipios(this.provinciaIdFiltro()).subscribe({
        next: (data) => this.municipios.set(data),
        error: (err) => {
          console.error('Error al cargar municipios:', err);
          this.toast.error('No se pudieron cargar los municipios', { title: 'Error' });
        },
      });
    }
  }

  limpiarFiltros(): void {
    this.tipoFiltro.set('');
    this.provinciaIdFiltro.set('');
    this.municipioIdFiltro.set('');
    this.fechaFiltro.set('todas');
    this.municipios.set([]);
  }

  crearPublicacion(): void {
    this.router.navigate(['/publicaciones/crear']);
  }
}
