import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { MascotaService, Mascota } from '../../services/mascota.service';

@Component({
  selector: 'app-crear-mascota',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './crear-mascota.html',
  styleUrl: './crear-mascota.css',
})
export class CrearMascota implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly mascotaService = inject(MascotaService);
  private readonly router = inject(Router);

  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  // Modelo del formulario
  mascota = {
    nombre: '',
    tipo: '',
    raza: '',
    color: '',
    tamanio: '',
    descripcion: ''
  };

  private selectedImageFile: File | null = null;
  readonly imagePreviewUrl = signal<string | null>(null);

  // Opciones para los selects
  readonly tiposOptions = [
    { value: 'Perro', label: 'Perro' },
    { value: 'Gato', label: 'Gato' },
    { value: 'Otro', label: 'Otro' }
  ];

  readonly tamaniosOptions = [
    { value: 'Pequeño', label: 'Pequeño' },
    { value: 'Mediano', label: 'Mediano' },
    { value: 'Grande', label: 'Grande' }
  ];

  ngOnInit(): void {
    const currentUser = this.auth.currentUser();
    if (!currentUser?.id) {
      this.router.navigateByUrl('/login');
    }
  }

  onSubmit(): void {
    // Validación básica
    if (!this.mascota.nombre || !this.mascota.tipo) {
      this.error.set('Por favor completa los campos obligatorios (Nombre y Tipo)');
      return;
    }

    this.loading.set(true);
    this.error.set(null);

    const currentUser = this.auth.currentUser();

    const mascotaData: Mascota = {
      nombre: this.mascota.nombre,
      tipo: this.mascota.tipo,
      raza: this.mascota.raza || undefined,
      color: this.mascota.color || undefined,
      tamanio: this.mascota.tamanio || undefined,
      descripcion: this.mascota.descripcion || undefined,
      estadoMascota: 'ADOPTADA',
      usuarioId: currentUser?.id
    };

    const request$ = this.selectedImageFile
      ? this.mascotaService.createMascotaWithImage(mascotaData, this.selectedImageFile)
      : this.mascotaService.createMascota(mascotaData);

    request$.subscribe({
      next: () => {
        this.loading.set(false);
        this.router.navigateByUrl('/perfil');
      },
      error: (err) => {
        console.error('Error al crear mascota:', err);
        this.error.set('No se pudo crear la mascota. Por favor intenta de nuevo.');
        this.loading.set(false);
      }
    });
  }

  onImageSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] || null;

    this.error.set(null);
    this.selectedImageFile = null;
    this.imagePreviewUrl.set(null);

    if (!file) return;

    const allowedTypes = ['image/jpeg', 'image/png'];
    const extension = (file.name.split('.').pop() || '').toLowerCase();
    const allowedExt = ['jpg', 'jpeg', 'png'];

    if (!allowedTypes.includes(file.type) || !allowedExt.includes(extension)) {
      this.error.set('La imagen debe ser JPG/JPEG o PNG');
      input.value = '';
      return;
    }

    this.selectedImageFile = file;
    this.imagePreviewUrl.set(URL.createObjectURL(file));
  }

  cancelar(): void {
    this.router.navigateByUrl('/perfil');
  }
}
