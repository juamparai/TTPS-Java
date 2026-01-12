import { Component, inject, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MascotaCard, type Mascota } from '../mascota-card/mascota-card';
import { MascotaService } from '../../services/mascota.service';
import { ToastService } from '../../shared/toast/toast.service';
import { GeorefService, type Provincia, type Departamento, type Localidad } from '../../services/georef.service';

@Component({
  selector: 'app-home',
  imports: [CommonModule, MascotaCard, FormsModule],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class Home implements OnInit {
  private readonly mascotaService = inject(MascotaService);
  private readonly toast = inject(ToastService);
  private readonly georefService = inject(GeorefService);
  private readonly router = inject(Router);

  readonly mascotas = signal<Mascota[]>([]);
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
    const tipos = new Set(this.mascotas().map(m => m.tipo).filter(Boolean));
    return Array.from(tipos).sort();
  });

  // Mascotas filtradas
  readonly mascotasFiltradas = computed(() => {
    let resultado = this.mascotas();

    // Filtrar por tipo
    if (this.tipoFiltro()) {
      resultado = resultado.filter(m => m.tipo === this.tipoFiltro());
    }

    // TODO: Filtrar por ubicación cuando tengamos esos datos en Mascota
    // Por ahora solo filtramos por tipo

    return resultado;
  });

  ngOnInit(): void {
    this.cargarMascotas();
    this.cargarProvincias();
  }

  cargarMascotas(): void {
    this.loading.set(true);
    this.mascotaService.getMascotas().subscribe({
      next: (data) => {
        this.mascotas.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        console.error('Error al cargar mascotas:', err);
        this.toast.error('No se pudieron cargar las mascotas', { title: 'Error' });
      },
    });
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
