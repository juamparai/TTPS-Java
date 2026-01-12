import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { GeorefService, type Provincia, type Departamento, type Localidad } from '../../services/georef.service';
import { ToastService } from '../../shared/toast/toast.service';
import { AuthService } from '../../services/auth.service';
import { HttpClient } from '@angular/common/http';

const API_BASE = 'http://localhost:8080/api';

@Component({
  selector: 'app-crear-publicacion',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './crear-publicacion.html',
  styleUrl: './crear-publicacion.css',
})
export class CrearPublicacion implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly georefService = inject(GeorefService);
  private readonly toast = inject(ToastService);
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);
  private readonly http = inject(HttpClient);

  readonly form: FormGroup;
  readonly submitted = signal(false);
  readonly loading = signal(false);

  // Opciones para los dropdowns
  readonly estadosPublicacion = ['ACTIVA', 'FINALIZADA', 'CANCELADA'];

  readonly provincias = signal<Provincia[]>([]);
  readonly departamentos = signal<Departamento[]>([]);
  readonly localidades = signal<Localidad[]>([]);

  readonly loadingProvincias = signal(false);
  readonly loadingDepartamentos = signal(false);
  readonly loadingLocalidades = signal(false);

  readonly mascotas = signal<any[]>([]);
  readonly loadingMascotas = signal(false);

  constructor() {
    const currentUser = this.authService.getCurrentUser();

    this.form = this.fb.group({
      descripcion: ['', [Validators.required, Validators.minLength(10)]],
      estadoPublicacion: ['ACTIVA', Validators.required],
      esMascotaExistente: [false],
      mascotaId: [{ value: '', disabled: true }],
      provinciaId: ['', Validators.required],
      departamentoId: ['', Validators.required],
      localidadId: ['', Validators.required],
      lat: [''],
      lng: [''],
      usuarioId: [currentUser?.id || null, Validators.required],
    });

    // Manejar el toggle del switch
    this.form.get('esMascotaExistente')?.valueChanges.subscribe((value) => {
      const mascotaControl = this.form.get('mascotaId');
      if (value) {
        mascotaControl?.enable();
        mascotaControl?.setValidators(Validators.required);
      } else {
        mascotaControl?.disable();
        mascotaControl?.clearValidators();
        mascotaControl?.setValue('');
      }
      mascotaControl?.updateValueAndValidity();
    });

    // Manejar cambios en provincia
    this.form.get('provinciaId')?.valueChanges.subscribe((provinciaId) => {
      this.form.patchValue({ departamentoId: '', localidadId: '' });
      this.departamentos.set([]);
      this.localidades.set([]);

      if (provinciaId) {
        this.cargarDepartamentos(provinciaId);
      }
    });

    // Manejar cambios en departamento
    this.form.get('departamentoId')?.valueChanges.subscribe((departamentoId) => {
      this.form.patchValue({ localidadId: '' });
      this.localidades.set([]);

      const provinciaId = this.form.get('provinciaId')?.value;
      if (provinciaId && departamentoId) {
        this.cargarLocalidades(provinciaId, departamentoId);
      }
    });
  }

  ngOnInit(): void {
    // Verificar que el usuario esté logueado
    const currentUser = this.authService.getCurrentUser();
    if (!currentUser) {
      this.toast.error('Debes iniciar sesión para crear una publicación', { title: 'Error' });
      this.router.navigate(['/login']);
      return;
    }

    this.cargarProvincias();
    this.cargarMascotas();
  }

  cargarProvincias(): void {
    this.loadingProvincias.set(true);
    this.georefService.getProvincias().subscribe({
      next: (data) => {
        this.provincias.set(data);
        this.loadingProvincias.set(false);
      },
      error: (err) => {
        console.error('Error al cargar provincias:', err);
        this.toast.error('No se pudieron cargar las provincias', { title: 'Error' });
        this.loadingProvincias.set(false);
      },
    });
  }

  cargarDepartamentos(provinciaId: string): void {
    this.loadingDepartamentos.set(true);
    this.georefService.getDepartamentos(provinciaId).subscribe({
      next: (data) => {
        this.departamentos.set(data);
        this.loadingDepartamentos.set(false);
      },
      error: (err) => {
        console.error('Error al cargar departamentos:', err);
        this.toast.error('No se pudieron cargar los departamentos', { title: 'Error' });
        this.loadingDepartamentos.set(false);
      },
    });
  }

  cargarLocalidades(provinciaId: string, departamentoId: string): void {
    this.loadingLocalidades.set(true);
    this.georefService.getLocalidades(provinciaId, departamentoId).subscribe({
      next: (data) => {
        this.localidades.set(data);
        this.loadingLocalidades.set(false);
      },
      error: (err) => {
        console.error('Error al cargar localidades:', err);
        this.toast.error('No se pudieron cargar las localidades', { title: 'Error' });
        this.loadingLocalidades.set(false);
      },
    });
  }

  cargarMascotas(): void {
    const currentUser = this.authService.getCurrentUser();
    if (!currentUser?.id) return;

    this.loadingMascotas.set(true);
    this.http.get<any[]>(`${API_BASE}/mascotas`).subscribe({
      next: (data) => {
        // Filtrar solo las mascotas del usuario actual
        this.mascotas.set(data.filter(m => m.usuarioId === currentUser.id));
        this.loadingMascotas.set(false);
      },
      error: (err) => {
        console.error('Error al cargar mascotas:', err);
        this.toast.error('No se pudieron cargar tus mascotas', { title: 'Error' });
        this.loadingMascotas.set(false);
      },
    });
  }

  submit(): void {
    this.submitted.set(true);

    if (this.form.invalid) {
      this.toast.error('Por favor completá todos los campos requeridos', { title: 'Error' });
      return;
    }

    this.loading.set(true);

    const payload = {
      descripcion: this.form.value.descripcion,
      estadoPublicacion: this.form.value.estadoPublicacion,
      lat: this.form.value.lat || null,
      lng: this.form.value.lng || null,
      usuarioId: this.form.value.usuarioId,
      mascotaId: this.form.value.esMascotaExistente ? this.form.value.mascotaId : null,
    };

    this.http.post<any>(`${API_BASE}/publicaciones`, payload).subscribe({
      next: (res) => {
        this.loading.set(false);
        this.toast.success('Publicación creada exitosamente', { title: 'Éxito' });
        this.router.navigate(['/']);
      },
      error: (err) => {
        this.loading.set(false);
        console.error('Error al crear publicación:', err);
        const errorMsg = err.error?.error || 'No se pudo crear la publicación';
        this.toast.error(errorMsg, { title: 'Error' });
      },
    });
  }

  cancelar(): void {
    this.router.navigate(['/']);
  }
}

