import { Component, inject, OnInit } from '@angular/core';
import { AbstractControl, FormControl, FormGroup, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';
import { ToastService } from '../../shared/toast/toast.service';
import { HttpClient } from '@angular/common/http';

const API_BASE = 'http://localhost:8080/api';

// Validador personalizado para contraseñas coincidentes
function passwordsMatchValidator(control: AbstractControl): ValidationErrors | null {
  const newPassword = control.get('newPassword');
  const repeatPassword = control.get('repeatPassword');

  if (!newPassword || !repeatPassword) {
    return null;
  }

  if (repeatPassword.value === '') {
    return null;
  }

  return newPassword.value === repeatPassword.value ? null : { passwordsMismatch: true };
}

@Component({
  selector: 'app-cambiar-password',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule],
  templateUrl: './cambiar-password.html',
  styleUrl: './cambiar-password.css',
})
export class CambiarPassword implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly toast = inject(ToastService);
  private readonly http = inject(HttpClient);

  errorMsg = '';
  loading = false;
  submitted = false;

  form = new FormGroup({
    currentPassword: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required],
    }),
    newPassword: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.minLength(7)],
    }),
    repeatPassword: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required],
    }),
  }, { validators: passwordsMatchValidator });

  ngOnInit(): void {
    const currentUser = this.auth.currentUser();
    if (!currentUser?.id) {
      this.router.navigateByUrl('/login');
      return;
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

    const setServerError = (controlName: keyof CambiarPassword['form']['controls']) => {
      const control = this.form.controls[controlName];
      const nextErrors = { ...(control.errors ?? {}), server: msg };
      control.setErrors(nextErrors);
    };

    if (lower.includes('actual') || lower.includes('current') || lower.includes('incorrecta') || lower.includes('incorrect')) {
      setServerError('currentPassword');
      return;
    }
    if (lower.includes('nueva') || lower.includes('contraseña') || lower.includes('password')) {
      setServerError('newPassword');
      return;
    }
  }

  cancelar(): void {
    this.router.navigateByUrl('/perfil');
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
    const { currentPassword, newPassword } = this.form.getRawValue();

    const payload = {
      currentPassword,
      newPassword,
    };

    this.http.put<{ mensaje: string }>(`${API_BASE}/usuarios/${currentUser.id}/cambiar-password`, payload)
      .subscribe({
        next: () => {
          this.loading = false;
          this.toast.success('Contraseña actualizada exitosamente', { title: 'Contraseña' });
          this.router.navigateByUrl('/perfil');
        },
        error: (err) => {
          this.loading = false;

          const errorMsg =
            err?.error?.error ??
            err?.error?.message ??
            (typeof err?.error === 'string' ? err.error : null);

          // Mensaje específico para contraseña incorrecta
          if (errorMsg && (
            errorMsg.toLowerCase().includes('actual') ||
            errorMsg.toLowerCase().includes('current') ||
            errorMsg.toLowerCase().includes('incorrecta') ||
            errorMsg.toLowerCase().includes('incorrect')
          )) {
            this.errorMsg = 'Contraseña incorrecta. Si olvidó su contraseña póngase en contacto con un administrador.';
            this.applyServerErrors(this.errorMsg);
          } else {
            this.errorMsg = errorMsg || 'No se pudo cambiar la contraseña';
            this.applyServerErrors(this.errorMsg);
          }

          this.form.markAllAsTouched();
          this.toast.error(this.errorMsg, { title: 'Error' });
        },
      });
  }
}
