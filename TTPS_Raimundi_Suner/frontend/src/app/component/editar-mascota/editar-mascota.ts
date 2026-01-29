import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule, Location } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { MascotaService, Mascota } from '../../services/mascota.service';
import { ToastService } from '../../shared/toast/toast.service';

@Component({
  selector: 'app-editar-mascota',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './editar-mascota.html',
  styleUrl: './editar-mascota.css',
})
export class EditarMascota implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly toast = inject(ToastService);
  private readonly mascotaService = inject(MascotaService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly location = inject(Location);

  readonly user = this.auth.currentUser;
  readonly isAdmin = computed(() => {
    const u = this.user();
    return u?.rolId === 0;
  });

  estadoSeleccionado: string | null = null;

  readonly loading = signal(false);
  readonly loadingMascota = signal(true);
  readonly error = signal<string | null>(null);
  readonly showDeleteModal = signal(false);
  readonly deleting = signal(false);

  mascotaId: number | null = null;

  // preserve original owner id so edits don't reassign the mascota
  originalUsuarioId: number | null = null;

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
  readonly currentImageUrl = signal<string | null>(null);
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
          descripcion: mascota.descripcion || ''
        };
        // guardar estado actual para que el admin pueda modificarlo
        this.estadoSeleccionado = (mascota.estadoMascota as string) ?? null;
        // preservar owner
        this.originalUsuarioId = (mascota.usuarioId as number) ?? null;

        const url = mascota.imagenUrl;
        if (url) {
          this.currentImageUrl.set(url.startsWith('http') ? url : `http://localhost:8080${url}`);
        } else {
          this.currentImageUrl.set(null);
        }

        this.selectedImageFile = null;
        this.imagePreviewUrl.set(null);
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
      // do NOT reassign owner on edit; preserve original owner if present
      usuarioId: this.originalUsuarioId ?? undefined,
      estadoMascota: this.isAdmin() && this.estadoSeleccionado ? this.estadoSeleccionado : undefined
    };

    const request$ = this.selectedImageFile
      ? this.mascotaService.updateMascotaWithImage(this.mascotaId, mascotaData, this.selectedImageFile)
      : this.mascotaService.updateMascota(this.mascotaId, mascotaData);

    request$.subscribe({
      next: () => {
        this.loading.set(false);
        this.toast.success('Mascota actualizada exitosamente', { title: 'Mascota' });
        this.location.back();
      },
      error: (err) => {
        console.error('Error al actualizar mascota:', err);
        this.error.set('No se pudo actualizar la mascota. Por favor intenta de nuevo.');
        this.toast.error('No se pudo actualizar la mascota', { title: 'Error' });
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
    this.location.back();
  }

  volver(): void {
    this.location.back();
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
        this.toast.success('Mascota eliminada exitosamente');
        this.location.back();
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
