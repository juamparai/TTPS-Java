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
import { authRedirectGuard } from './guards/auth-redirect.guard';

export const routes: Routes = [
    { path: '', component: Home },
    { path: 'login', component: Login, canActivate: [authRedirectGuard] },
    { path: 'registro', component: Registro, canActivate: [authRedirectGuard] },
    { path: 'publicaciones/crear', component: CrearPublicacion },
    { path: 'perfil', component: Perfil },
    { path: 'perfil/editar', component: EditarPerfil },
    { path: 'cambiar-password', component: CambiarPassword },
    { path: 'mascotas/crear', component: CrearMascota },
    { path: 'mascotas/:id/editar', component: EditarMascota },
];
