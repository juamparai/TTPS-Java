import { Component, inject, OnInit, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { CommonModule, Location } from '@angular/common';
import { AdminService } from '../../services/admin.service';
import { AuthService, UsuarioDTO } from '../../services/auth.service';
import { ToastService } from '../../shared/toast/toast.service';
import { GeorefService, type Provincia, type Departamento, type Localidad } from '../../services/georef.service';
import { HttpClient } from '@angular/common/http';
import { tap } from 'rxjs';

@Component({
  selector: 'app-editar-usuario',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule],
  templateUrl: './editar-usuario.html',
  styleUrl: './editar-usuario.css',
})
export class EditarUsuario implements OnInit {
  private readonly adminService = inject(AdminService);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly toast = inject(ToastService);
  private readonly georef = inject(GeorefService);
  private readonly http = inject(HttpClient);
  public readonly location = inject(Location);

  loading = false;
  submitted = false;
  errorMsg = '';

  readonly provincias = signal<Provincia[]>([]);
  readonly departamentos = signal<Departamento[]>([]);
  readonly localidades = signal<Localidad[]>([]);
  readonly loadingProvincias = signal(true);
  readonly loadingDepartamentos = signal(false);
  readonly loadingLocalidades = signal(false);

  private provinciaSeleccionadaId = '';
  private editingUserId: number | null = null;

  form = new FormGroup({
    nombre: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    apellido: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    email: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    telefono: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.pattern(/^\d+$/)] }),
    provincia: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    ciudad: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    barrio: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    puntos: new FormControl(0, { nonNullable: true }),
  });

  ngOnInit(): void {
    const currentUser = this.auth.currentUser();
    if (!currentUser?.id) {
      this.router.navigateByUrl('/login');
      return;
    }

    const idStr = this.route.snapshot.paramMap.get('id');
    if (!idStr) {
      this.toast.error('ID de usuario inválido', { title: 'Error' });
      this.router.navigateByUrl('/admin/usuarios');
      return;
    }
    this.editingUserId = parseInt(idStr, 10);

    // if not admin and editing other user's profile, redirect
    const isAdmin = currentUser?.rolId === 0;
    if (!isAdmin && this.editingUserId !== currentUser.id) {
      this.toast.error('No tienes permisos para editar a este usuario', { title: 'Acceso' });
      this.router.navigateByUrl('/');
      return;
    }

    // disable puntos field if not admin
    if (!isAdmin) this.form.controls.puntos.disable();

    this.cargarProvincias();
    this.cargarUsuario();

    this.form.controls.provincia.valueChanges.subscribe((provinciaId) => {
      if (provinciaId) {
        this.provinciaSeleccionadaId = provinciaId;
        this.cargarDepartamentos(provinciaId);
      } else {
        this.provinciaSeleccionadaId = '';
        this.departamentos.set([]);
        this.localidades.set([]);
        this.form.controls.ciudad.setValue('');
        this.form.controls.ciudad.disable();
        this.form.controls.barrio.setValue('');
        this.form.controls.barrio.disable();
      }
    });

    this.form.controls.ciudad.valueChanges.subscribe((departamentoNombre) => {
      if (departamentoNombre && this.provinciaSeleccionadaId) {
        const departamento = this.departamentos().find(d => d.nombre === departamentoNombre);
        if (departamento) {
          this.cargarLocalidades(this.provinciaSeleccionadaId, departamento.id);
        }
      } else {
        this.localidades.set([]);
        this.form.controls.barrio.setValue('');
        this.form.controls.barrio.disable();
      }
    });
  }

  private cargarUsuario(): void {
    if (!this.editingUserId) return;
    this.adminService.getUsuarioById(this.editingUserId).subscribe({
      next: (user) => {
        this.form.patchValue({
          nombre: user.nombre || '',
          apellido: user.apellido || '',
          email: user.email || '',
          telefono: user.telefono || '',
          provincia: user.provinciaId || '',
          puntos: user.puntos ?? 0,
        });

        if (user.provinciaId) {
          this.provinciaSeleccionadaId = user.provinciaId;
        }
      },
      error: (err) => {
        console.error('Error al cargar usuario:', err);
        this.toast.error('No se pudo cargar el usuario', { title: 'Error' });
        this.router.navigateByUrl('/admin/usuarios');
      }
    });
  }

  cargarProvincias(): void {
    this.loadingProvincias.set(true);
    this.georef.getProvincias().subscribe({
      next: (data) => {
        this.provincias.set(data);
        this.loadingProvincias.set(false);
        const user = this.auth.currentUser();
        if (user?.provinciaId) {
          this.cargarDepartamentosInicial(user.provinciaId, user.departamentoId, user.localidadId);
        }
      },
      error: (err) => {
        this.loadingProvincias.set(false);
        console.error('Error al cargar provincias:', err);
        this.toast.error('No se pudieron cargar las provincias', { title: 'Error' });
      }
    });
  }

  cargarDepartamentosInicial(provinciaId: string, departamentoId?: string, localidadId?: string): void {
    this.georef.getDepartamentos(provinciaId).subscribe({
      next: (data) => {
        this.departamentos.set(data);
        this.form.controls.ciudad.enable();
        if (departamentoId) {
          const depto = data.find(d => d.id === departamentoId);
          if (depto) {
            this.form.controls.ciudad.setValue(depto.nombre, { emitEvent: false });
            if (localidadId) this.cargarLocalidadesInicial(provinciaId, departamentoId, localidadId);
          }
        }
      },
    });
  }

  cargarLocalidadesInicial(provinciaId: string, departamentoId: string, localidadId: string): void {
    this.georef.getLocalidades(provinciaId, departamentoId).subscribe({
      next: (data) => {
        this.localidades.set(data);
        this.form.controls.barrio.enable();
        const localidad = data.find(l => l.id === localidadId);
        if (localidad) this.form.controls.barrio.setValue(localidad.nombre, { emitEvent: false });
      },
    });
  }

  cargarDepartamentos(provinciaId: string): void {
    this.loadingDepartamentos.set(true);
    this.form.controls.ciudad.setValue('');
    this.form.controls.ciudad.disable();
    this.form.controls.barrio.setValue('');
    this.form.controls.barrio.disable();
    this.departamentos.set([]);
    this.localidades.set([]);

    this.georef.getDepartamentos(provinciaId).subscribe({
      next: (data) => {
        this.departamentos.set(data);
        this.loadingDepartamentos.set(false);
        this.form.controls.ciudad.enable();
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
    this.form.controls.barrio.disable();
    this.localidades.set([]);

    this.georef.getLocalidades(provinciaId, departamentoId).subscribe({
      next: (data) => {
        this.localidades.set(data);
        this.loadingLocalidades.set(false);
        this.form.controls.barrio.enable();
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

  submit(): void {
    this.errorMsg = '';
    this.submitted = true;
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.toast.error('Revisá los campos marcados.', { title: 'Datos inválidos' });
      return;
    }

    if (!this.editingUserId) {
      this.toast.error('ID de usuario inválido', { title: 'Error' });
      return;
    }

    this.loading = true;
    const { nombre, apellido, email, telefono, provincia, ciudad, barrio, puntos } = this.form.getRawValue();

    const departamento = this.departamentos().find(d => d.nombre === ciudad);
    const localidad = this.localidades().find(l => l.nombre === barrio);

    const payload: any = {
      id: this.editingUserId,
      nombre,
      apellido,
      email,
      telefono,
      provinciaId: provincia,
      departamentoId: departamento?.id,
      localidadId: localidad?.id,
    };

    const currentUser = this.auth.currentUser();
    const isAdmin = currentUser?.rolId === 0;
    if (isAdmin) payload.puntos = puntos;

    this.adminService.actualizarUsuario(this.editingUserId, payload).subscribe({
      next: (res) => {
        this.loading = false;
        this.toast.success(res.mensaje || 'Usuario actualizado', { title: 'Usuario' });
        // if the admin edited themselves, update auth state
        if (currentUser && currentUser.id === this.editingUserId) {
          this.auth['setCurrentUser'](res.usuario);
        }
        this.location.back();
      },
      error: (err) => {
        this.loading = false;
        console.error('Error al actualizar usuario:', err);
        const msg = err?.error?.error ?? err?.error?.message ?? 'No se pudo actualizar el usuario';
        this.errorMsg = msg;
        this.form.markAllAsTouched();
        this.toast.error(msg, { title: 'Error' });
      }
    });
  }
}
