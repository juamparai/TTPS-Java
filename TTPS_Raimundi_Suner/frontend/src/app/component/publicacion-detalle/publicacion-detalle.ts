import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { PublicacionService } from '../../services/publicacion.service';
import { MascotaService, Mascota } from '../../services/mascota.service';
import { GeorefService } from '../../services/georef.service';
import { AuthService } from '../../services/auth.service';

type PublicacionDetalleData = {
  id: number;
  descripcion: string;
  fecha: string;
  fechaCierre?: string;
  estadoPublicacion: string;
  lat?: number;
  lng?: number;
  municipioId?: string;
  mascotaId: number;
  usuarioId: number;
  // Datos de mascota
  mascota?: Mascota;
  // Datos de usuario
  usuarioNombre?: string;
  usuarioEmail?: string;
  usuarioTelefono?: string;
};

@Component({
  selector: 'app-publicacion-detalle',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './publicacion-detalle.html',
  styleUrl: './publicacion-detalle.css',
})
export class PublicacionDetalle implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly publicacionService = inject(PublicacionService);
  private readonly mascotaService = inject(MascotaService);
  private readonly georefService = inject(GeorefService);
  private readonly authService = inject(AuthService);

  readonly publicacion = signal<PublicacionDetalleData | null>(null);
  readonly ubicacion = signal<string>('Cargando ubicación...');
  readonly cargando = signal<boolean>(true);
  readonly error = signal<string>('');
  readonly mostrarModal = signal<boolean>(false);
  readonly mostrarModalCancelar = signal<boolean>(false);
  readonly procesandoRecuperacion = signal<boolean>(false);
  readonly procesandoCancelacion = signal<boolean>(false);

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.cargarPublicacion(+id);
    } else {
      this.error.set('ID de publicación no válido');
      this.cargando.set(false);
    }
  }

  private cargarPublicacion(id: number): void {
    this.publicacionService.getPublicacionById(id).subscribe({
      next: (pub: any) => {
        console.log('Publicación recibida:', pub);
        console.log('Estado:', pub.estadoPublicacion);
        console.log('Fecha cierre:', pub.fechaCierre);
        console.log('Usuario:', pub.usuario);
        console.log('Mascota:', pub.mascota);

        // El backend ya devuelve la mascota completa, no necesitamos cargarla por separado
        const publicacionCompleta = {
          ...pub,
          usuarioNombre: pub.usuario?.nombre || null,
          usuarioEmail: pub.usuario?.email || null,
          usuarioTelefono: pub.usuario?.telefono || null,
        };

        this.publicacion.set(publicacionCompleta);
        console.log('Publicación completa:', this.publicacion());
        console.log('Es perdido?', this.esPerdido);
        console.log('Es perdida propia?', this.esPerdidaPropia);
        this.cargarUbicacion(pub);
        this.cargando.set(false);
      },
      error: (err) => {
        console.error('Error al cargar publicación:', err);
        this.error.set('Error al cargar la publicación');
        this.cargando.set(false);
      }
    });
  }

  private cargarUbicacion(pub: any): void {
    if (pub.municipioId) {
      this.georefService.getMunicipioPorId(pub.municipioId).subscribe({
        next: (municipio) => {
          this.ubicacion.set(municipio.nombre);
        },
        error: () => {
          this.ubicacion.set('Ubicación desconocida');
        }
      });
    } else if (pub.lat && pub.lng) {
      this.georefService.getUbicacionPorCoordenadas(pub.lat, pub.lng).subscribe({
        next: (ubicacion) => {
          const nombre = ubicacion.municipio?.nombre || ubicacion.departamento?.nombre || 'Ubicación desconocida';
          this.ubicacion.set(nombre);
        },
        error: () => {
          this.ubicacion.set('Ubicación desconocida');
        }
      });
    } else {
      this.ubicacion.set('Ubicación no especificada');
    }
  }

  get inicialNombre(): string {
    return this.publicacion()?.mascota?.nombre?.charAt(0).toUpperCase() || '?';
  }

  get esPerdidaPropia(): boolean {
    const estadoMascota = this.publicacion()?.mascota?.estadoMascota;
    return estadoMascota === 'PERDIDA_PROPIA';
  }

  get esPerdido(): boolean {
    const estadoMascota = this.publicacion()?.mascota?.estadoMascota;
    const estadoPub = this.publicacion()?.estadoPublicacion;
    return (estadoMascota === 'PERDIDA_PROPIA' || estadoMascota === 'PERDIDA_AJENA') &&
           estadoPub === 'ACTIVA';
  }

  get esFinalizada(): boolean {
    return this.publicacion()?.estadoPublicacion === 'FINALIZADA';
  }

  get esCancelada(): boolean {
    return this.publicacion()?.estadoPublicacion === 'CANCELADA';
  }

  get estadoClase(): string {
    const estado = this.publicacion()?.estadoPublicacion;
    if (estado === 'FINALIZADA') return 'estado-finalizada';
    if (estado === 'CANCELADA') return 'estado-cancelada';
    return 'estado-activa';
  }

  get tagTexto(): string {
    const estado = this.publicacion()?.estadoPublicacion;
    if (estado === 'FINALIZADA') return 'RECUPERADA';
    if (estado === 'CANCELADA') return 'CANCELADA';
    return 'PERDIDO';
  }

  get mostrarTag(): boolean {
    const estado = this.publicacion()?.estadoPublicacion;
    return estado === 'FINALIZADA' || estado === 'CANCELADA' || this.esPerdido;
  }

  get esMiPublicacion(): boolean {
    const currentUser = this.authService.getCurrentUser();
    const publicacionUserId = this.publicacion()?.usuarioId;
    return currentUser != null && publicacionUserId != null && currentUser.id === publicacionUserId;
  }

  abrirModal(): void {
    this.mostrarModal.set(true);
  }

  cerrarModal(): void {
    this.mostrarModal.set(false);
  }

  abrirModalCancelar(): void {
    this.mostrarModalCancelar.set(true);
  }

  cerrarModalCancelar(): void {
    this.mostrarModalCancelar.set(false);
  }

  confirmarCancelacion(mascotaRecuperada: boolean): void {
    const pub = this.publicacion();
    if (!pub || !pub.id || !pub.mascotaId) return;

    this.procesandoCancelacion.set(true);

    // 1. Actualizar el estado de la mascota según si fue recuperada o no
    this.mascotaService.getMascotaById(pub.mascotaId).subscribe({
      next: (mascota) => {
        const nuevoEstadoMascota = mascotaRecuperada ? 'ADOPTADA' : mascota.estadoMascota;
        const mascotaActualizada = {
          ...mascota,
          estadoMascota: nuevoEstadoMascota
        };

        this.mascotaService.updateMascota(pub.mascotaId, mascotaActualizada).subscribe({
          next: () => {
            // 2. Cancelar la publicación
            const publicacionActualizada = {
              ...pub,
              estadoPublicacion: 'CANCELADA',
              fechaCierre: new Date().toISOString().split('T')[0]
            };

            this.publicacionService.updatePublicacion(pub.id, publicacionActualizada).subscribe({
              next: () => {
                this.procesandoCancelacion.set(false);
                this.cerrarModalCancelar();
                this.router.navigate(['/']);
              },
              error: (err) => {
                console.error('Error al cancelar publicación:', err);
                this.procesandoCancelacion.set(false);
                alert('Error al cancelar la publicación');
              }
            });
          },
          error: (err) => {
            console.error('Error al actualizar mascota:', err);
            this.procesandoCancelacion.set(false);
            alert('Error al actualizar el estado de la mascota');
          }
        });
      },
      error: (err) => {
        console.error('Error al obtener mascota:', err);
        this.procesandoCancelacion.set(false);
        alert('Error al obtener los datos de la mascota');
      }
    });
  }

  confirmarRecuperacion(): void {
    const pub = this.publicacion();
    if (!pub || !pub.id || !pub.mascotaId) return;

    this.procesandoRecuperacion.set(true);

    // 1. Actualizar el estado de la mascota a ADOPTADA
    this.mascotaService.getMascotaById(pub.mascotaId).subscribe({
      next: (mascota) => {
        const mascotaActualizada = {
          ...mascota,
          estadoMascota: 'ADOPTADA'
        };

        this.mascotaService.updateMascota(pub.mascotaId, mascotaActualizada).subscribe({
          next: () => {
            // 2. Finalizar la publicación
            const publicacionActualizada = {
              ...pub,
              estadoPublicacion: 'FINALIZADA',
              fechaCierre: new Date().toISOString().split('T')[0]
            };

            this.publicacionService.updatePublicacion(pub.id, publicacionActualizada).subscribe({
              next: () => {
                this.procesandoRecuperacion.set(false);
                this.cerrarModal();
                this.router.navigate(['/']);
              },
              error: (err) => {
                console.error('Error al finalizar publicación:', err);
                this.procesandoRecuperacion.set(false);
                alert('Error al finalizar la publicación');
              }
            });
          },
          error: (err) => {
            console.error('Error al actualizar mascota:', err);
            this.procesandoRecuperacion.set(false);
            alert('Error al actualizar el estado de la mascota');
          }
        });
      },
      error: (err) => {
        console.error('Error al obtener mascota:', err);
        this.procesandoRecuperacion.set(false);
        alert('Error al obtener los datos de la mascota');
      }
    });
  }

  confirmarEncontroDueno(nuevoEstado: 'ENCONTRADA' | 'ADOPTADA'): void {
    const pub = this.publicacion();
    if (!pub || !pub.id || !pub.mascotaId) return;

    this.procesandoRecuperacion.set(true);

    // 1. Actualizar el estado de la mascota
    this.mascotaService.getMascotaById(pub.mascotaId).subscribe({
      next: (mascota) => {
        const mascotaActualizada = {
          ...mascota,
          estadoMascota: nuevoEstado
        };

        this.mascotaService.updateMascota(pub.mascotaId, mascotaActualizada).subscribe({
          next: () => {
            // 2. Finalizar la publicación
            const publicacionActualizada = {
              ...pub,
              estadoPublicacion: 'FINALIZADA',
              fechaCierre: new Date().toISOString().split('T')[0]
            };

            this.publicacionService.updatePublicacion(pub.id, publicacionActualizada).subscribe({
              next: () => {
                this.procesandoRecuperacion.set(false);
                this.cerrarModal();
                this.router.navigate(['/']);
              },
              error: (err) => {
                console.error('Error al finalizar publicación:', err);
                this.procesandoRecuperacion.set(false);
                alert('Error al finalizar la publicación');
              }
            });
          },
          error: (err) => {
            console.error('Error al actualizar mascota:', err);
            this.procesandoRecuperacion.set(false);
            alert('Error al actualizar el estado de la mascota');
          }
        });
      },
      error: (err) => {
        console.error('Error al obtener mascota:', err);
        this.procesandoRecuperacion.set(false);
        alert('Error al obtener los datos de la mascota');
      }
    });
  }

  confirmarCancelacionAjena(nuevoEstado: 'ENCONTRADA' | 'ADOPTADA' | null): void {
    const pub = this.publicacion();
    if (!pub || !pub.id || !pub.mascotaId) return;

    this.procesandoCancelacion.set(true);

    // 1. Actualizar el estado de la mascota según la opción elegida
    this.mascotaService.getMascotaById(pub.mascotaId).subscribe({
      next: (mascota) => {
        const estadoFinal = nuevoEstado || mascota.estadoMascota; // Si es null, mantener el estado actual
        const mascotaActualizada = {
          ...mascota,
          estadoMascota: estadoFinal
        };

        this.mascotaService.updateMascota(pub.mascotaId, mascotaActualizada).subscribe({
          next: () => {
            // 2. Cancelar la publicación
            const publicacionActualizada = {
              ...pub,
              estadoPublicacion: 'CANCELADA',
              fechaCierre: new Date().toISOString().split('T')[0]
            };

            this.publicacionService.updatePublicacion(pub.id, publicacionActualizada).subscribe({
              next: () => {
                this.procesandoCancelacion.set(false);
                this.cerrarModalCancelar();
                this.router.navigate(['/']);
              },
              error: (err) => {
                console.error('Error al cancelar publicación:', err);
                this.procesandoCancelacion.set(false);
                alert('Error al cancelar la publicación');
              }
            });
          },
          error: (err) => {
            console.error('Error al actualizar mascota:', err);
            this.procesandoCancelacion.set(false);
            alert('Error al actualizar el estado de la mascota');
          }
        });
      },
      error: (err) => {
        console.error('Error al obtener mascota:', err);
        this.procesandoCancelacion.set(false);
        alert('Error al obtener los datos de la mascota');
      }
    });
  }

  volver(): void {
    this.router.navigate(['/']);
  }
}
