import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { MascotaService, Mascota } from '../../services/mascota.service';

@Component({
  selector: 'app-editar-mascota',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './editar-mascota.html',
  styleUrl: './editar-mascota.css',
})
export class EditarMascota implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly mascotaService = inject(MascotaService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  readonly loading = signal(false);
  readonly loadingMascota = signal(true);
  readonly error = signal<string | null>(null);
  readonly showDeleteModal = signal(false);
  readonly deleting = signal(false);

  mascotaId: number | null = null;

  // Modelo del formulario
  mascota = {
    nombre: '',
    tipo: '',
    raza: '',
    color: '',
    tamanio: '',
    descripcion: '',
    foto: ''
  };

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
      return;
    }

    // Obtener el ID de la mascota desde la URL
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      this.router.navigateByUrl('/perfil');
      return;
    }

    this.mascotaId = parseInt(id, 10);
    this.cargarMascota();
  }

  cargarMascota(): void {
    if (!this.mascotaId) return;

    this.loadingMascota.set(true);
    this.mascotaService.getMascotaById(this.mascotaId).subscribe({
      next: (mascota) => {
        console.log('Mascota cargada para editar:', mascota);
        console.log('Usuario actual:', this.auth.currentUser());

        // Verificar que la mascota pertenece al usuario actual
        const currentUser = this.auth.currentUser();

        // Si no viene usuarioId en la respuesta, asumimos que es del usuario actual
        // ya que el backend solo debería devolver mascotas del usuario autenticado
        if (mascota.usuarioId && mascota.usuarioId !== currentUser?.id) {
          this.error.set('No tienes permiso para editar esta mascota');
          this.loadingMascota.set(false);
          return;
        }

        // Cargar los datos en el formulario
        this.mascota = {
          nombre: mascota.nombre || '',
          tipo: mascota.tipo || '',
          raza: mascota.raza || '',
          color: mascota.color || '',
          tamanio: mascota.tamanio || '',
          descripcion: mascota.descripcion || '',
          foto: ''
        };
        this.loadingMascota.set(false);
      },
      error: (err) => {
        console.error('Error al cargar mascota:', err);
        this.error.set('No se pudo cargar la mascota');
        this.loadingMascota.set(false);
      }
    });
  }

  onSubmit(): void {
    // Validación básica
    if (!this.mascota.nombre || !this.mascota.tipo) {
      this.error.set('Por favor completa los campos obligatorios (Nombre y Tipo)');
      return;
    }

    if (!this.mascotaId) {
      this.error.set('Error: ID de mascota no válido');
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
      usuarioId: currentUser?.id
    };

    this.mascotaService.updateMascota(this.mascotaId, mascotaData).subscribe({
      next: () => {
        this.loading.set(false);
        this.router.navigateByUrl('/perfil');
      },
      error: (err) => {
        console.error('Error al actualizar mascota:', err);
        this.error.set('No se pudo actualizar la mascota. Por favor intenta de nuevo.');
        this.loading.set(false);
      }
    });
  }

  cancelar(): void {
    this.router.navigateByUrl('/perfil');
  }

  volver(): void {
    this.router.navigateByUrl('/perfil');
  }

  abrirModalEliminar(): void {
    this.showDeleteModal.set(true);
  }

  cerrarModalEliminar(): void {
    this.showDeleteModal.set(false);
  }

  confirmarEliminar(): void {
    if (!this.mascotaId) return;

    this.deleting.set(true);
    this.mascotaService.deleteMascota(this.mascotaId).subscribe({
      next: () => {
        this.deleting.set(false);
        this.showDeleteModal.set(false);
        this.router.navigateByUrl('/perfil');
      },
      error: (err: any) => {
        console.error('Error al eliminar mascota:', err);
        this.error.set('No se pudo eliminar la mascota. Por favor intenta de nuevo.');
        this.deleting.set(false);
        this.showDeleteModal.set(false);
      }
    });
  }
}
