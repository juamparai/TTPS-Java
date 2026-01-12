import { Component, inject } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { ToastService } from '../../shared/toast/toast.service';

const EMAIL_COM_REGEX = /^[^@\s]+@[^@\s]+\.com$/i;

@Component({
  selector: 'app-registro',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './registro.html',
  styleUrl: './registro.css',
})
export class Registro {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly toast = inject(ToastService);

  errorMsg = '';
  loading = false;
  submitted = false;

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
    telefono: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(/^\d+$/)],
    }),
    barrio: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    ciudad: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
  });

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
    if (lower.includes('ciudad')) {
      setServerError('ciudad');
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
    const { nombre, apellido, email, password, telefono, barrio, ciudad } = this.form.getRawValue();

    this.auth.registro({ nombre, apellido, email, password, telefono, barrio, ciudad }).subscribe({
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
