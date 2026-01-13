import { Routes } from '@angular/router';
import { Home } from './component/home/home';
import { Login } from './component/login/login';
import { Registro } from './component/registro/registro';
import { CrearPublicacion } from './component/crear-publicacion/crear-publicacion';
import { Perfil } from './component/perfil/perfil';
import { EditarPerfil } from './component/editar-perfil/editar-perfil';
import { CambiarPassword } from './component/cambiar-password/cambiar-password';
import { CrearMascota } from './component/crear-mascota/crear-mascota';
import { EditarMascota } from './component/editar-mascota/editar-mascota';
import { PublicacionDetalle } from './component/publicacion-detalle/publicacion-detalle';
import { NotFound } from './component/not-found/not-found';
import { authRedirectGuard } from './guards/auth-redirect.guard';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
    { path: '', component: Home },
    { path: 'login', component: Login, canActivate: [authRedirectGuard] },
    { path: 'registro', component: Registro, canActivate: [authRedirectGuard] },
    { path: 'publicaciones/crear', component: CrearPublicacion, canActivate: [authGuard] },
    { path: 'publicacion/:id', component: PublicacionDetalle },
    { path: 'perfil', component: Perfil, canActivate: [authGuard] },
    { path: 'perfil/editar', component: EditarPerfil, canActivate: [authGuard] },
    { path: 'cambiar-password', component: CambiarPassword, canActivate: [authGuard] },
    { path: 'mascotas/crear', component: CrearMascota, canActivate: [authGuard] },
    { path: 'mascotas/:id/editar', component: EditarMascota, canActivate: [authGuard] },
    { path: '**', component: NotFound }, // Ruta 404 - debe ser la última
];
