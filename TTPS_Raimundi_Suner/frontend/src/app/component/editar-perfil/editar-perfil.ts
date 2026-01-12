import { Component, inject, OnInit, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService, UsuarioDTO } from '../../services/auth.service';
import { ToastService } from '../../shared/toast/toast.service';
import { GeorefService, type Provincia, type Departamento, type Localidad } from '../../services/georef.service';
import { HttpClient } from '@angular/common/http';
import { tap } from 'rxjs';

const EMAIL_COM_REGEX = /^[^@\s]+@[^@\s]+\.com$/i;
const API_BASE = 'http://localhost:8080/api';

@Component({
  selector: 'app-editar-perfil',
  imports: [ReactiveFormsModule, CommonModule],
  templateUrl: './editar-perfil.html',
  styleUrl: './editar-perfil.css',
})
export class EditarPerfil implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly toast = inject(ToastService);
  private readonly georef = inject(GeorefService);
  private readonly http = inject(HttpClient);

  errorMsg = '';
  loading = false;
  submitted = false;

  readonly provincias = signal<Provincia[]>([]);
  readonly departamentos = signal<Departamento[]>([]);
  readonly localidades = signal<Localidad[]>([]);
  readonly loadingProvincias = signal(true);
  readonly loadingDepartamentos = signal(false);
  readonly loadingLocalidades = signal(false);

  private provinciaSeleccionadaId = '';

  form = new FormGroup({
    nombre: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    apellido: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    email: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(EMAIL_COM_REGEX)],
    }),
    telefono: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(/^\d+$/)],
    }),
    provincia: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    ciudad: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    barrio: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
  });

  ngOnInit(): void {
    const currentUser = this.auth.currentUser();
    if (!currentUser?.id) {
      this.router.navigateByUrl('/login');
      return;
    }

    this.cargarProvincias();
    this.cargarDatosUsuario(currentUser);

    // Escuchar cambios en provincia para cargar departamentos
    this.form.controls.provincia.valueChanges.subscribe((provinciaId) => {
      if (provinciaId) {
        this.provinciaSeleccionadaId = provinciaId;
        this.cargarDepartamentos(provinciaId);
      } else {
        this.provinciaSeleccionadaId = '';
        this.departamentos.set([]);
        this.localidades.set([]);
        this.form.controls.ciudad.setValue('');
        this.form.controls.barrio.setValue('');
      }
    });

    // Escuchar cambios en ciudad/departamento para cargar localidades
    this.form.controls.ciudad.valueChanges.subscribe((departamentoNombre) => {
      if (departamentoNombre && this.provinciaSeleccionadaId) {
        const departamento = this.departamentos().find(d => d.nombre === departamentoNombre);
        if (departamento) {
          this.cargarLocalidades(this.provinciaSeleccionadaId, departamento.id);
        }
      } else {
        this.localidades.set([]);
        this.form.controls.barrio.setValue('');
      }
    });
  }

  cargarDatosUsuario(user: UsuarioDTO): void {
    this.form.patchValue({
      nombre: user.nombre || '',
      apellido: user.apellido || '',
      email: user.email || '',
      telefono: user.telefono || '',
      provincia: user.provinciaId || '',
      // Los demás se cargan después de obtener departamentos y localidades
    });

    // Si hay provincia, cargar sus datos geográficos
    if (user.provinciaId) {
      this.provinciaSeleccionadaId = user.provinciaId;
    }
  }

  cargarProvincias(): void {
    this.loadingProvincias.set(true);
    this.georef.getProvincias().subscribe({
      next: (data) => {
        this.provincias.set(data);
        this.loadingProvincias.set(false);

        // Si el usuario tiene provincia, cargar departamentos
        const user = this.auth.currentUser();
        if (user?.provinciaId) {
          this.cargarDepartamentosInicial(user.provinciaId, user.departamentoId, user.localidadId);
        }
      },
      error: (err) => {
        this.loadingProvincias.set(false);
        console.error('Error al cargar provincias:', err);
        this.toast.error('No se pudieron cargar las provincias', { title: 'Error' });
      },
    });
  }

  cargarDepartamentosInicial(provinciaId: string, departamentoId?: string, localidadId?: string): void {
    this.georef.getDepartamentos(provinciaId).subscribe({
      next: (data) => {
        this.departamentos.set(data);

        // Encontrar el nombre del departamento por ID
        if (departamentoId) {
          const depto = data.find(d => d.id === departamentoId);
          if (depto) {
            this.form.controls.ciudad.setValue(depto.nombre, { emitEvent: false });

            // Cargar localidades
            if (localidadId) {
              this.cargarLocalidadesInicial(provinciaId, departamentoId, localidadId);
            }
          }
        }
      },
    });
  }

  cargarLocalidadesInicial(provinciaId: string, departamentoId: string, localidadId: string): void {
    this.georef.getLocalidades(provinciaId, departamentoId).subscribe({
      next: (data) => {
        this.localidades.set(data);

        // Encontrar el nombre de la localidad por ID
        const localidad = data.find(l => l.id === localidadId);
        if (localidad) {
          this.form.controls.barrio.setValue(localidad.nombre, { emitEvent: false });
        }
      },
    });
  }

  cargarDepartamentos(provinciaId: string): void {
    this.loadingDepartamentos.set(true);
    this.form.controls.ciudad.setValue('');
    this.form.controls.barrio.setValue('');
    this.departamentos.set([]);
    this.localidades.set([]);

    this.georef.getDepartamentos(provinciaId).subscribe({
      next: (data) => {
        this.departamentos.set(data);
        this.loadingDepartamentos.set(false);
      },
      error: (err) => {
        this.loadingDepartamentos.set(false);
        console.error('Error al cargar departamentos:', err);
        this.toast.error('No se pudieron cargar los departamentos', { title: 'Error' });
      },
    });
  }

  cargarLocalidades(provinciaId: string, departamentoId: string): void {
    this.loadingLocalidades.set(true);
    this.form.controls.barrio.setValue('');
    this.localidades.set([]);

    this.georef.getLocalidades(provinciaId, departamentoId).subscribe({
      next: (data) => {
        this.localidades.set(data);
        this.loadingLocalidades.set(false);
      },
      error: (err) => {
        this.loadingLocalidades.set(false);
        console.error('Error al cargar localidades:', err);
        this.toast.error('No se pudieron cargar las localidades', { title: 'Error' });
      },
    });
  }

  sanitizeTelefono(): void {
    const current = this.form.controls.telefono.value;
    const onlyDigits = current.replace(/\D+/g, '');
    if (onlyDigits !== current) {
      this.form.controls.telefono.setValue(onlyDigits);
    }
  }

  private clearServerErrors(): void {
    for (const control of Object.values(this.form.controls)) {
      const errors = control.errors;
      if (errors && 'server' in errors) {
        const { server, ...rest } = errors as Record<string, unknown>;
        control.setErrors(Object.keys(rest).length ? rest : null);
      }
    }
  }

  private applyServerErrors(message: string): void {
    const msg = (message ?? '').toString();
    const lower = msg.toLowerCase();

    const setServerError = (controlName: keyof EditarPerfil['form']['controls']) => {
      const control = this.form.controls[controlName];
      const nextErrors = { ...(control.errors ?? {}), server: msg };
      control.setErrors(nextErrors);
    };

    if (lower.includes('email')) {
      setServerError('email');
      return;
    }
    if (lower.includes('nombre')) {
      setServerError('nombre');
      return;
    }
    if (lower.includes('apellido')) {
      setServerError('apellido');
      return;
    }
    if (lower.includes('teléfono') || lower.includes('telefono')) {
      setServerError('telefono');
      return;
    }
    if (lower.includes('barrio')) {
      setServerError('barrio');
      return;
    }
    if (lower.includes('ciudad') || lower.includes('departamento')) {
      setServerError('ciudad');
      return;
    }
    if (lower.includes('provincia')) {
      setServerError('provincia');
      return;
    }
  }

  cancelar(): void {
    this.router.navigateByUrl('/perfil');
  }

  cambiarPassword(): void {
    this.router.navigateByUrl('/cambiar-password');
  }

  submit(): void {
    this.errorMsg = '';
    this.submitted = true;
    this.clearServerErrors();
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.toast.error('Revisá los campos marcados.', { title: 'Datos inválidos' });
      return;
    }

    const currentUser = this.auth.currentUser();
    if (!currentUser?.id) {
      this.router.navigateByUrl('/login');
      return;
    }

    this.loading = true;
    const { nombre, apellido, email, telefono, provincia, ciudad, barrio } = this.form.getRawValue();

    const departamento = this.departamentos().find(d => d.nombre === ciudad);
    const localidad = this.localidades().find(l => l.nombre === barrio);

    const payload = {
      id: currentUser.id,
      nombre,
      apellido,
      email,
      telefono,
      provinciaId: provincia,
      departamentoId: departamento?.id,
      localidadId: localidad?.id,
    };

    this.http.put<{ mensaje: string; usuario: UsuarioDTO }>(`${API_BASE}/usuarios/${currentUser.id}`, payload)
      .pipe(tap((res) => this.auth['setCurrentUser'](res.usuario)))
      .subscribe({
        next: () => {
          this.loading = false;
          this.toast.success('Perfil actualizado exitosamente', { title: 'Perfil' });
          this.router.navigateByUrl('/perfil');
        },
        error: (err) => {
          this.loading = false;
          this.errorMsg =
            err?.error?.error ??
            err?.error?.message ??
            (typeof err?.error === 'string' ? err.error : null) ??
            'No se pudo actualizar el perfil';

          this.applyServerErrors(this.errorMsg);
          this.form.markAllAsTouched();
          this.toast.error(this.errorMsg, { title: 'Error' });
        },
      });
  }
}
