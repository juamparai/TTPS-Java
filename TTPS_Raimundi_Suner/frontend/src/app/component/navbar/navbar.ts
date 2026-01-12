import { Component, computed, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-navbar',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './navbar.html',
  styleUrl: './navbar.css',
})
export class Navbar {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly user = this.auth.currentUser;
  readonly displayName = computed(() => {
    const u = this.user();
    if (!u) return '';
    const name = [u.nombre, u.apellido].filter(Boolean).join(' ').trim();
    return name || u.email || 'Usuario';
  });

  logout(): void {
    this.auth.logout();
    this.router.navigateByUrl('/login');
  }
}
