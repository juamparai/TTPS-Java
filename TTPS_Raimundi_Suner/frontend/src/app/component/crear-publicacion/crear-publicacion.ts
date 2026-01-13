import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { GeorefService } from '../../services/georef.service';
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

  readonly estadosPublicacion = ['ACTIVA', 'FINALIZADA', 'CANCELADA'];

  readonly mascotas = signal<any[]>([]);
  readonly loadingMascotas = signal(false);

  constructor() {
    const currentUser = this.authService.getCurrentUser();

    this.form = this.fb.group({
      descripcion: ['', [Validators.required, Validators.minLength(10)]],
      estadoPublicacion: ['ACTIVA', Validators.required],
      esMascotaExistente: [true],
      mascotaId: ['', Validators.required],
      // Campos para nueva mascota
      nuevaMascotaTipo: [{ value: '', disabled: true }],
      nuevaMascotaColor: [{ value: '', disabled: true }],
      nuevaMascotaDescripcion: [{ value: '', disabled: true }],
      nuevaMascotaEsPropia: [{ value: false, disabled: true }],
      nuevaMascotaNombre: [{ value: '', disabled: true }],
      nuevaMascotaRaza: [{ value: '', disabled: true }],
      nuevaMascotaTamanio: [{ value: '', disabled: true }],
      // Ubicación (solo coordenadas)
      lat: ['', [Validators.required, Validators.min(-90), Validators.max(90)]],
      lng: ['', [Validators.required, Validators.min(-180), Validators.max(180)]],
      usuarioId: [currentUser?.id || null, Validators.required],
    });

    this.form.get('esMascotaExistente')?.valueChanges.subscribe((value) => {
      const mascotaControl = this.form.get('mascotaId');
      const tipoControl = this.form.get('nuevaMascotaTipo');
      const colorControl = this.form.get('nuevaMascotaColor');
      const descripcionMascotaControl = this.form.get('nuevaMascotaDescripcion');
      const esPropiaControl = this.form.get('nuevaMascotaEsPropia');
      const nombreControl = this.form.get('nuevaMascotaNombre');
      const razaControl = this.form.get('nuevaMascotaRaza');
      const tamanioControl = this.form.get('nuevaMascotaTamanio');

      if (value) {
        // Es mascota existente
        mascotaControl?.enable();
        mascotaControl?.setValidators(Validators.required);

        // Deshabilitar campos de nueva mascota
        tipoControl?.disable();
        tipoControl?.clearValidators();
        colorControl?.disable();
        colorControl?.clearValidators();
        descripcionMascotaControl?.disable();
        descripcionMascotaControl?.clearValidators();
        esPropiaControl?.disable();
        nombreControl?.disable();
        nombreControl?.clearValidators();
        razaControl?.disable();
        tamanioControl?.disable();
        tamanioControl?.clearValidators();
      } else {
        // Es mascota nueva
        mascotaControl?.disable();
        mascotaControl?.clearValidators();
        mascotaControl?.setValue('');

        // Habilitar campos de nueva mascota
        tipoControl?.enable();
        tipoControl?.setValidators(Validators.required);
        colorControl?.enable();
        colorControl?.setValidators(Validators.required);
        descripcionMascotaControl?.enable();
        descripcionMascotaControl?.setValidators([Validators.required, Validators.minLength(5)]);
        esPropiaControl?.enable();
        razaControl?.enable();
        tamanioControl?.enable();
        tamanioControl?.setValidators(Validators.required);
      }

      mascotaControl?.updateValueAndValidity();
      tipoControl?.updateValueAndValidity();
      colorControl?.updateValueAndValidity();
      descripcionMascotaControl?.updateValueAndValidity();
      esPropiaControl?.updateValueAndValidity();
      nombreControl?.updateValueAndValidity();
      razaControl?.updateValueAndValidity();
      tamanioControl?.updateValueAndValidity();
    });

    // Manejar el toggle de "es propia"
    this.form.get('nuevaMascotaEsPropia')?.valueChanges.subscribe((value) => {
      const nombreControl = this.form.get('nuevaMascotaNombre');
      if (value) {
        nombreControl?.enable();
        nombreControl?.setValidators(Validators.required);
      } else {
        nombreControl?.disable();
        nombreControl?.clearValidators();
        nombreControl?.setValue('');
      }
      nombreControl?.updateValueAndValidity();
    });
  }

  ngOnInit(): void {
    const currentUser = this.authService.getCurrentUser();
    if (!currentUser) {
      this.toast.error('Debes iniciar sesión para crear una publicación', { title: 'Error' });
      this.router.navigate(['/login']);
      return;
    }

    this.cargarMascotas();
  }

  cargarMascotas(): void {
    const currentUser = this.authService.getCurrentUser();
    if (!currentUser?.id) return;

    this.loadingMascotas.set(true);
    this.http.get<any[]>(`${API_BASE}/mascotas/usuario/${currentUser.id}`).subscribe({
      next: (data) => {
        console.log('Mascotas del usuario:', data);
        this.mascotas.set(data);
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

    // Si no es mascota existente, primero crear la mascota
    if (!this.form.value.esMascotaExistente) {
      this.crearMascotaYPublicacion();
    } else {
      this.crearPublicacion(this.form.value.mascotaId);
    }
  }

  private crearMascotaYPublicacion(): void {
    const currentUser = this.authService.getCurrentUser();
    const esPropia = this.form.value.nuevaMascotaEsPropia;

    const mascotaPayload = {
      nombre: esPropia ? this.form.value.nuevaMascotaNombre : 'Desconocido',
      tipo: this.form.value.nuevaMascotaTipo,
      color: this.form.value.nuevaMascotaColor,
      descripcion: this.form.value.nuevaMascotaDescripcion,
      raza: this.form.value.nuevaMascotaRaza || null,
      tamanio: this.form.value.nuevaMascotaTamanio,
      estadoMascota: esPropia ? 'PERDIDA_PROPIA' : 'PERDIDA_AJENA',
      usuarioId: esPropia ? currentUser?.id : null,
    };

    this.http.post<any>(`${API_BASE}/mascotas`, mascotaPayload).subscribe({
      next: (mascotaResponse) => {
        const mascotaId = mascotaResponse.mascota?.id || mascotaResponse.id;
        this.crearPublicacion(mascotaId);
      },
      error: (err) => {
        this.loading.set(false);
        console.error('Error al crear mascota:', err);
        const errorMsg = err.error?.error || 'No se pudo crear la mascota';
        this.toast.error(errorMsg, { title: 'Error' });
      },
    });
  }

  private crearPublicacion(mascotaId: number | null): void {
    const lat = parseFloat(this.form.value.lat);
    const lng = parseFloat(this.form.value.lng);

    // Obtener el municipio usando las coordenadas
    this.georefService.getUbicacionPorCoordenadas(lat, lng).subscribe({
      next: (ubicacion) => {
        const municipioId = ubicacion.municipio?.id || ubicacion.departamento?.id || '';
        const fechaActual = new Date().toISOString().split('T')[0]; // Formato YYYY-MM-DD

        const payload = {
          descripcion: this.form.value.descripcion,
          estadoPublicacion: 'ACTIVA',
          fecha: fechaActual,
          lat: lat,
          lng: lng,
          municipioId: municipioId,
          usuarioId: this.form.value.usuarioId,
          mascotaId: mascotaId,
        };

        this.http.post<any>(`${API_BASE}/publicaciones`, payload).subscribe({
          next: () => {
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
      },
      error: (err) => {
        this.loading.set(false);
        console.error('Error al obtener ubicación:', err);
        this.toast.error('No se pudo obtener la ubicación de las coordenadas ingresadas', { title: 'Error' });
      },
    });
  }

  cancelar(): void {
    this.router.navigate(['/']);
  }
}
