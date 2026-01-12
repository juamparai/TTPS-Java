import { Component, computed, inject } from '@angular/core';
import { ToastService } from './toast.service';

@Component({
  selector: 'app-toast-host',
  templateUrl: './toast.html',
  styleUrl: './toast.css',
})
export class ToastHost {
  private readonly toastService = inject(ToastService);
  readonly toasts = computed(() => this.toastService.toasts());

  dismiss(id: number): void {
    this.toastService.dismiss(id);
  }
}
