import { Component, inject, OnInit, signal } from '@angular/core';
import { AbstractControl, FormControl, FormGroup, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';
import { ToastService } from '../../shared/toast/toast.service';
import { GeorefService, type Provincia, type Departamento, type Localidad } from '../../services/georef.service';

const EMAIL_COM_REGEX = /^[^@\s]+@[^@\s]+\.com$/i;

// Validador personalizado para contraseñas coincidentes
function passwordsMatchValidator(control: AbstractControl): ValidationErrors | null {
  const password = control.get('password');
  const repeatPassword = control.get('repeatPassword');

  if (!password || !repeatPassword) {
    return null;
  }

  if (repeatPassword.value === '') {
    return null;
  }

  return password.value === repeatPassword.value ? null : { passwordsMismatch: true };
}

@Component({
  selector: 'app-registro',
  imports: [ReactiveFormsModule, RouterLink, CommonModule],
  templateUrl: './registro.html',
  styleUrl: './registro.css',
})
export class Registro implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly toast = inject(ToastService);
  private readonly georef = inject(GeorefService);

  errorMsg = '';
  loading = false;
  submitted = false;

  readonly provincias = signal<Provincia[]>([]);
  readonly departamentos = signal<Departamento[]>([]);
  readonly localidades = signal<Localidad[]>([]);
  readonly loadingProvincias = signal(true);
  readonly loadingDepartamentos = signal(false);
  readonly loadingLocalidades = signal(false);

  // Guardar el ID de la provincia seleccionada para usarlo en la carga de localidades
  private provinciaSeleccionadaId = '';

  form = new FormGroup({
    nombre: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    apellido: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    email: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(EMAIL_COM_REGEX)],
    }),
    password: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.minLength(7)],
    }),
    repeatPassword: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required],
    }),
    telefono: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(/^\d+$/)],
    }),
    provincia: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    ciudad: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    barrio: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
  }, { validators: passwordsMatchValidator });

  ngOnInit(): void {
    this.cargarProvincias();

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
        // Buscar el ID del departamento por su nombre
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

  cargarProvincias(): void {
    this.loadingProvincias.set(true);
    this.georef.getProvincias().subscribe({
      next: (data) => {
        this.provincias.set(data);
        this.loadingProvincias.set(false);
      },
      error: (err) => {
        this.loadingProvincias.set(false);
        console.error('Error al cargar provincias:', err);
        this.toast.error('No se pudieron cargar las provincias', { title: 'Error' });
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
        // Remove only the server error, keep the rest
        const { server, ...rest } = errors as Record<string, unknown>;
        control.setErrors(Object.keys(rest).length ? rest : null);
      }
    }
  }

  private applyServerErrors(message: string): void {
    const msg = (message ?? '').toString();
    const lower = msg.toLowerCase();

    const setServerError = (controlName: keyof Registro['form']['controls']) => {
      const control = this.form.controls[controlName];
      const nextErrors = { ...(control.errors ?? {}), server: msg };
      control.setErrors(nextErrors);
    };

    if (lower.includes('email') && lower.includes('registr')) {
      setServerError('email');
      return;
    }
    if (lower.includes('email') && lower.includes('formato')) {
      setServerError('email');
      return;
    }
    if (lower.includes('contraseña')) {
      setServerError('password');
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

  submit(): void {
    this.errorMsg = '';
    this.submitted = true;
    this.clearServerErrors();
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.toast.error('Revisá los campos marcados.', { title: 'Datos inválidos' });
      return;
    }

    this.loading = true;
    const { nombre, apellido, email, password, telefono, provincia, ciudad, barrio } = this.form.getRawValue();

    // Buscar el ID del departamento seleccionado
    const departamento = this.departamentos().find(d => d.nombre === ciudad);
    const localidad = this.localidades().find(l => l.nombre === barrio);

    this.auth.registro({
      nombre,
      apellido,
      email,
      password,
      telefono,
      provinciaId: provincia,
      departamentoId: departamento?.id,
      localidadId: localidad?.id
    }).subscribe({
      next: () => {
        this.loading = false;
        this.toast.success('Usuario registrado exitosamente', { title: 'Registro' });
        this.router.navigateByUrl('/login');
      },
      error: (err) => {
        this.loading = false;
        this.errorMsg =
          err?.error?.error ??
          err?.error?.message ??
          (typeof err?.error === 'string' ? err.error : null) ??
          'No se pudo registrar';

        this.applyServerErrors(this.errorMsg);
        this.form.markAllAsTouched();
        this.toast.error(this.errorMsg, { title: 'Registro' });
      },
    });
  }
}
