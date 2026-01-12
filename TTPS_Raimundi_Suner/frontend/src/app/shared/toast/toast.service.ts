import { Injectable, signal } from '@angular/core';

export type ToastVariant = 'success' | 'error';

export type ToastItem = {
  id: number;
  message: string;
  variant: ToastVariant;
  title?: string;
  timeoutMs: number;
};

@Injectable({ providedIn: 'root' })
export class ToastService {
  private nextId = 1;
  readonly toasts = signal<ToastItem[]>([]);

  success(message: string, opts?: { title?: string; timeoutMs?: number }): void {
    this.show('success', message, opts);
  }

  error(message: string, opts?: { title?: string; timeoutMs?: number }): void {
    this.show('error', message, opts);
  }

  dismiss(id: number): void {
    this.toasts.update((items) => items.filter((t) => t.id !== id));
  }

  private show(variant: ToastVariant, message: string, opts?: { title?: string; timeoutMs?: number }): void {
    const id = this.nextId++;
    const timeoutMs = opts?.timeoutMs ?? 2000;

    this.toasts.update((items) => [
      ...items,
      {
        id,
        variant,
        message,
        title: opts?.title,
        timeoutMs,
      },
    ]);

    setTimeout(() => this.dismiss(id), timeoutMs);
  }
}
