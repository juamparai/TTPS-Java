import { Component, inject } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { ToastService } from '../../shared/toast/toast.service';

const EMAIL_COM_REGEX = /^[^@\s]+@[^@\s]+\.com$/i;

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly toast = inject(ToastService);

  errorMsg = '';
  loading = false;
  submitted = false;

  form = new FormGroup({
    email: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(EMAIL_COM_REGEX)],
    }),
    password: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.minLength(7)],
    }),
  });

  submit(): void {
    this.errorMsg = '';
    this.submitted = true;
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.toast.error('Revisá el email y la contraseña.', { title: 'Datos inválidos' });
      return;
    }

    this.loading = true;
    const { email, password } = this.form.getRawValue();

    this.auth.login({ email, password }).subscribe({
      next: () => {
        this.loading = false;
        this.toast.success('Sesión iniciada correctamente', { title: 'Login' });
        this.router.navigateByUrl('/');
      },
      error: (err) => {
        this.loading = false;
        this.errorMsg =
          err?.error?.error ??
          err?.error?.message ??
          (typeof err?.error === 'string' ? err.error : null) ??
          'No se pudo iniciar sesión';

        this.form.markAllAsTouched();
        this.toast.error(this.errorMsg, { title: 'Login' });
      },
    });
  }
}
