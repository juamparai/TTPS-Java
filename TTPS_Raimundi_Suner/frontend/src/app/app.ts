import { Component, signal, OnInit, OnDestroy, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser, DOCUMENT } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import { Navbar } from './component/navbar/navbar';
import { AdminSidebar } from './component/admin-sidebar/admin-sidebar';
import { ToastHost } from './shared/toast/toast';
import { Title } from '@angular/platform-browser';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, Navbar, AdminSidebar, ToastHost],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit, OnDestroy {
  protected readonly title = signal('frontend');

  private readonly defaultTitle = 'Volvé a casa';
  private readonly hiddenTitle = '¿Dónde estás?';
  private readonly isBrowser: boolean;

  private readonly onVisibilityChange = () => {
    if (!this.isBrowser) return;
    this.titleService.setTitle(this.document.hidden ? this.hiddenTitle : this.defaultTitle);
  };

  constructor(
    private readonly titleService: Title,
    @Inject(PLATFORM_ID) platformId: object,
    @Inject(DOCUMENT) private readonly document: Document
  ) {
    this.isBrowser = isPlatformBrowser(platformId);
  }

  ngOnInit(): void {
    if (!this.isBrowser) return;
    // Set inicial (por si el usuario refresca con la pestaña no activa)
    this.onVisibilityChange();
    this.document.addEventListener('visibilitychange', this.onVisibilityChange);
  }

  ngOnDestroy(): void {
    if (!this.isBrowser) return;
    this.document.removeEventListener('visibilitychange', this.onVisibilityChange);
  }
}
