import {
  AfterViewInit,
  Component,
  ElementRef,
  OnInit,
  OnDestroy,
  PLATFORM_ID,
  ViewChild,
  inject,
  signal,
} from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { Location } from '@angular/common';
import { PublicacionService } from '../../services/publicacion.service';
import { MascotaService, Mascota } from '../../services/mascota.service';
import { GeorefService } from '../../services/georef.service';
import { AuthService } from '../../services/auth.service';
import { AdminService } from '../../services/admin.service';
import { ToastService } from '../../shared/toast/toast.service';

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
export class PublicacionDetalle implements OnInit, AfterViewInit, OnDestroy {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly publicacionService = inject(PublicacionService);
  private readonly mascotaService = inject(MascotaService);
  private readonly georefService = inject(GeorefService);
  private readonly authService = inject(AuthService);
  private readonly adminService = inject(AdminService);
  private readonly toastService = inject(ToastService);
  private readonly location = inject(Location);

  readonly publicacion = signal<PublicacionDetalleData | null>(null);
  readonly ubicacion = signal<string>('Cargando ubicación...');
  readonly cargando = signal<boolean>(true);
  readonly error = signal<string>('');
  readonly mostrarModal = signal<boolean>(false);
  readonly mostrarModalCancelar = signal<boolean>(false);
  readonly procesandoRecuperacion = signal<boolean>(false);
  readonly procesandoCancelacion = signal<boolean>(false);

  @ViewChild('mapContainer') private mapContainer?: ElementRef<HTMLDivElement>;
  private map: any;
  private marker: any;
  private pinIcon: any;
  private viewReady = false;
  private mapReady = false;
  private readonly platformId = inject(PLATFORM_ID);

  ngOnInit(): void {
    // Solo ejecutar en el navegador, NO en el servidor
    if (!isPlatformBrowser(this.platformId)) {
      return;
    }

    // Limpiar el mapa si existe antes de inicializar
    if (this.map) {
      try {
        this.map.off();
        this.map.remove();
      } catch (e) {
        // Ignorar errores al limpiar
      }
      this.map = null;
      this.marker = null;
      this.pinIcon = null;
    }

    // Resetear estados al inicializar
    this.viewReady = false;
    this.mapReady = false;

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.cargarPublicacion(+id);
    } else {
      this.error.set('ID de publicación no válido');
      this.cargando.set(false);
    }
  }

  async ngAfterViewInit(): Promise<void> {
    this.viewReady = true;
    await this.initMapIfReady();
  }

  private cargarPublicacion(id: number): void {
    this.publicacionService.getPublicacionById(id).subscribe({
      next: (pub: any) => {
        // El backend ya devuelve la mascota completa, no necesitamos cargarla por separado
        const publicacionCompleta = {
          ...pub,
          usuarioNombre: pub.usuario?.nombre || null,
          usuarioEmail: pub.usuario?.email || null,
          usuarioTelefono: pub.usuario?.telefono || null,
        };

        this.publicacion.set(publicacionCompleta);
        this.cargarUbicacion(pub);
        // Primero renderizamos el template (el mapa está dentro de un *ngIf)
        this.cargando.set(false);
        // Luego intentamos inicializar el mapa en el próximo tick
        setTimeout(() => {
          this.initMapIfReady();
        }, 0);
      },
      error: (err) => {
        console.error('Error al cargar publicación:', err);
        this.error.set('Error al cargar la publicación');
        this.cargando.set(false);
      }
    });
  }

  private async initMapIfReady(): Promise<void> {
    if (this.mapReady) return;
    if (!this.viewReady) return;
    if (!isPlatformBrowser(this.platformId)) return;
    if (!this.mapContainer?.nativeElement) return;

    const pub = this.publicacion();
    const lat = pub?.lat;
    const lng = pub?.lng;
    if (lat == null || lng == null) return;

    // Limpiar cualquier contenido previo del contenedor del mapa
    const container = this.mapContainer.nativeElement;
    container.innerHTML = '';
    // Resetear el ID interno de Leaflet (usando any para acceder a la propiedad dinámica)
    (container as any)._leaflet_id = undefined;

    const L = await import('leaflet');

    const pinSvg = `<?xml version="1.0" encoding="UTF-8"?>
<svg xmlns="http://www.w3.org/2000/svg" width="34" height="34" viewBox="0 0 24 24">
  <path fill="#d32f2f" d="M12 2c-3.314 0-6 2.686-6 6c0 4.5 6 14 6 14s6-9.5 6-14c0-3.314-2.686-6-6-6zm0 8.5a2.5 2.5 0 1 1 0-5a2.5 2.5 0 0 1 0 5z"/>
</svg>`;
    const pinIconUrl = `data:image/svg+xml;charset=UTF-8,${encodeURIComponent(pinSvg)}`;
    this.pinIcon = L.icon({
      iconUrl: pinIconUrl,
      iconSize: [34, 34],
      iconAnchor: [17, 34],
      popupAnchor: [0, -34],
    });

    this.map = L.map(container, {
      center: [lat, lng],
      zoom: 15,
      zoomControl: true,
    });

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '&copy; OpenStreetMap contributors',
    }).addTo(this.map);

    this.marker = L.marker([lat, lng], { icon: this.pinIcon }).addTo(this.map);

    this.mapReady = true;

    setTimeout(() => {
      try {
        this.map?.invalidateSize();
      } catch {
        // noop
      }
    }, 0);
  }

  get imageSrc(): string | null {
    const url = this.publicacion()?.mascota?.imagenUrl;
    if (!url) return null;
    if (url.startsWith('http://') || url.startsWith('https://')) return url;
    return `http://localhost:8080${url}`;
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
                // 3. Sumar puntos si fue recuperada
                if (mascotaRecuperada && pub.usuarioId) {
                  this.sumarPuntosAlUsuario(pub.usuarioId, () => {
                    this.procesandoCancelacion.set(false);
                    this.cerrarModalCancelar();
                    this.router.navigate(['/']);
                  });
                } else {
                  this.procesandoCancelacion.set(false);
                  this.cerrarModalCancelar();
                  this.router.navigate(['/']);
                }
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
                // 3. Sumar puntos al usuario
                if (pub.usuarioId) {
                  this.sumarPuntosAlUsuario(pub.usuarioId, () => {
                    this.procesandoRecuperacion.set(false);
                    this.cerrarModal();
                    this.router.navigate(['/']);
                  });
                } else {
                  this.procesandoRecuperacion.set(false);
                  this.cerrarModal();
                  this.router.navigate(['/']);
                }
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
                // 3. Sumar puntos al usuario
                if (pub.usuarioId) {
                  this.sumarPuntosAlUsuario(pub.usuarioId, () => {
                    this.procesandoRecuperacion.set(false);
                    this.cerrarModal();
                    this.router.navigate(['/']);
                  });
                } else {
                  this.procesandoRecuperacion.set(false);
                  this.cerrarModal();
                  this.router.navigate(['/']);
                }
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
                // 3. Sumar puntos solo si encontró dueño o fue adoptada
                if (nuevoEstado && pub.usuarioId) {
                  this.sumarPuntosAlUsuario(pub.usuarioId, () => {
                    this.procesandoCancelacion.set(false);
                    this.cerrarModalCancelar();
                    this.router.navigate(['/']);
                  });
                } else {
                  this.procesandoCancelacion.set(false);
                  this.cerrarModalCancelar();
                  this.router.navigate(['/']);
                }
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

  private sumarPuntosAlUsuario(usuarioId: number, onSuccess: () => void): void {
    this.adminService.agregarPuntosUsuario(usuarioId, 10).subscribe({
      next: (response) => {
        // Si el usuario que finalizó la publicación es el mismo que está logueado,
        // actualizar su información en el servicio de autenticación
        const currentUser = this.authService.getCurrentUser();
        if (currentUser && currentUser.id === usuarioId) {
          this.authService.setCurrentUser(response.usuario);
        }
        this.toastService.success('¡Has ganado 10 puntos!');
        onSuccess();
      },
      error: (err) => {
        console.error('Error al sumar puntos al usuario:', err);
        // Continuar con el flujo aunque falle la suma de puntos
        onSuccess();
      }
    });
  }

  volver(): void {
    // Preferir volver en el historial del navegador para regresar
    // a la página desde la que se llegó al detalle. Si no existe
    // historial (p. ej. link directo), navegar al home como fallback.
    if (typeof window !== 'undefined' && window.history && window.history.length > 1) {
      this.location.back();
    } else {
      this.router.navigate(['/']);
    }
  }

  ngOnDestroy(): void {
    // Limpiar recursos si es necesario
    if (this.map) {
      this.map.off();
      this.map.remove();
      this.map = null;
    }
  }

  copyToClipboard(value?: string | null): void {
    if (!value) return;
    // Prefer modern clipboard API
    if (typeof navigator !== 'undefined' && navigator.clipboard && navigator.clipboard.writeText) {
      navigator.clipboard.writeText(value).then(() => {
        this.toastService.success('Copiado al portapapeles');
      }).catch(() => {
        // Fallback a método antiguo
        this.fallbackCopyText(value);
      });
    } else {
      this.fallbackCopyText(value);
    }
  }

  private fallbackCopyText(text: string): void {
    try {
      const textarea = document.createElement('textarea');
      textarea.value = text;
      textarea.style.position = 'fixed';
      textarea.style.left = '-9999px';
      document.body.appendChild(textarea);
      textarea.select();
      document.execCommand('copy');
      document.body.removeChild(textarea);
      this.toastService.success('Copiado al portapapeles');
    } catch (e) {
      console.error('No se pudo copiar al portapapeles', e);
      try {
        this.toastService.error('No se pudo copiar al portapapeles');
      } catch {}
    }
  }
}
