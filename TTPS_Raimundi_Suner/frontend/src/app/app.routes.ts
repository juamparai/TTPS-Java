import { Routes } from '@angular/router';
import { Home } from './component/home/home';
import { Login } from './component/login/login';
import { Registro } from './component/registro/registro';
import { CrearPublicacion } from './component/crear-publicacion/crear-publicacion';
import { authRedirectGuard } from './guards/auth-redirect.guard';

export const routes: Routes = [
    { path: '', component: Home },
    { path: 'login', component: Login, canActivate: [authRedirectGuard] },
    { path: 'registro', component: Registro, canActivate: [authRedirectGuard] },
    { path: 'publicaciones/crear', component: CrearPublicacion },
];
