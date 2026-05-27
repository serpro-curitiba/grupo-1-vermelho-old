import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CiclosService, GerarCicloResponse } from './ciclos.service';

/**
 * Formulário Reactive Forms para disparar a geração de um ciclo (REQ-PAY-001).
 * Usa signals para o estado local (resultado, loading, erro) — sem subscribes manuais.
 */
@Component({
  selector: 'sifap-gerar-ciclo',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <h2>Gerar ciclo de pagamento</h2>
    <form [formGroup]="form" (ngSubmit)="enviar()">
      <label>
        Competência (AAAAMM)
        <input formControlName="competencia" maxlength="6" placeholder="202504" />
      </label>
      <button type="submit" [disabled]="form.invalid || loading()">Gerar</button>
    </form>

    @if (erro(); as e) {
      <p class="err">{{ e }}</p>
    }

    @if (resultado(); as r) {
      <div class="card">
        <h3>Ciclo {{ r.cicloId }}</h3>
        <p>Status: <strong>{{ r.status }}</strong></p>
        <p>Pagamentos gerados: {{ r.totalPagamentos }}</p>
        <p>Valor total: R$ {{ r.valorTotal | number:'1.2-2' }}</p>
      </div>
    }
  `,
})
export class GerarCicloComponent {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(CiclosService);

  protected readonly form = this.fb.nonNullable.group({
    competencia: ['', [Validators.required, Validators.pattern(/^[0-9]{6}$/)]],
  });

  protected readonly loading = signal(false);
  protected readonly resultado = signal<GerarCicloResponse | null>(null);
  protected readonly erro = signal<string | null>(null);

  enviar(): void {
    if (this.form.invalid) return;
    this.loading.set(true);
    this.erro.set(null);
    this.resultado.set(null);

    this.service.gerar(this.form.getRawValue().competencia).subscribe({
      next: r => { this.resultado.set(r); this.loading.set(false); },
      error: err => {
        const detail = err?.error?.detail ?? err?.message ?? 'Erro desconhecido';
        this.erro.set(detail);
        this.loading.set(false);
      },
    });
  }
}
