import { Component, computed, inject, signal } from '@angular/core';
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

  readonly dropdownOpen = signal(false);

  readonly isAdmin = computed(() => {
    const u = this.user();
    return u?.rolId === 0;
  });

  toggleDropdown(): void {
    this.dropdownOpen.update(v => !v);
  }

  closeDropdown(): void {
    this.dropdownOpen.set(false);
  }

  goToProfile(): void {
    this.closeDropdown();
    this.router.navigateByUrl('/perfil');
  }

  logout(): void {
    this.closeDropdown();
    this.auth.logout();
    this.router.navigateByUrl('/login');
  }
}
