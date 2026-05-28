import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { CiclosService, GerarCicloResponse } from './ciclos.service';
import { firstValueFrom } from 'rxjs';

/**
 * Formulário Reactive Forms para disparar a geração de um ciclo (REQ-PAY-001).
 * Usa signals para o estado local (resultado, loading, erro) — sem subscribes manuais.
 */
@Component({
  selector: 'sifap-gerar-ciclo',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <header class="topo">
      <div>
        <p class="topo-eyebrow">Ciclo de Pagamento</p>
        <h2>Gerar novo ciclo</h2>
      </div>
      <p class="topo-hint">Informe a competência no formato AAAAMM</p>
    </header>

    <section class="layout-grid">
      <article class="painel principal">
        <h3>Execução do ciclo</h3>
        <form [formGroup]="form" (ngSubmit)="enviar()" class="form-grid">
          <label for="competencia">Competência (AAAAMM)</label>
          <input
            id="competencia"
            formControlName="competencia"
            maxlength="6"
            placeholder="202509"
          />

          @if (form.controls.competencia.invalid && form.controls.competencia.touched) {
            <p class="field-error">Use 6 dígitos no padrão AAAAMM.</p>
          }

          <button type="submit" [disabled]="form.invalid || loading()" class="btn-primario">
            {{ loading() ? 'Gerando...' : 'Gerar ciclo' }}
          </button>
        </form>
      </article>

      <article class="painel lateral">
        <h3>Checklist rápido</h3>
        <ul>
          <li><span class="icon">✓</span> Programa ativo cadastrado</li>
          <li><span class="icon">✓</span> Beneficiários em status ATIVO</li>
          <li><span class="icon">✓</span> Competência ainda não utilizada</li>
        </ul>
      </article>
    </section>

    @if (erro(); as e) {
      <section class="feedback feedback-erro">
        <span class="feedback-icon">!</span>
        <div>
          <h4>Não foi possível gerar o ciclo</h4>
          <p>{{ e }}</p>
        </div>
      </section>
    }

    @if (resultado(); as r) {
      <section class="feedback feedback-sucesso">
        <span class="feedback-icon">✓</span>
        <div>
          <h4>Ciclo gerado com sucesso</h4>
          <p><strong>ID:</strong> {{ r.cicloId }}</p>
          <p><strong>Status:</strong> {{ r.status }}</p>
          <p><strong>Pagamentos:</strong> {{ r.totalPagamentos }}</p>
          <p><strong>Valor total:</strong> {{ r.valorTotal | currency:'BRL':'symbol':'1.2-2':'pt-BR' }}</p>
        </div>
      </section>
    }
  `,
  styles: `
    :host {
      display: block;
      color: #12213d;
    }

    .topo {
      display: flex;
      align-items: flex-end;
      justify-content: space-between;
      gap: 1rem;
      margin-bottom: 1rem;
    }

    .topo h2 {
      margin: 0;
    }

    .topo-eyebrow {
      margin: 0 0 0.2rem;
      color: #5d6b84;
      font-size: 0.82rem;
      text-transform: uppercase;
      letter-spacing: 0.06em;
      font-weight: 600;
    }

    .topo-hint {
      margin: 0;
      color: #66789a;
      font-size: 0.9rem;
    }

    .layout-grid {
      display: grid;
      grid-template-columns: 1.35fr 1fr;
      gap: 0.9rem;
      margin-bottom: 1rem;
    }

    .painel {
      border: 1px solid #d8e0ed;
      border-radius: 12px;
      padding: 0.9rem;
      background: #fff;
      box-shadow: 0 1px 2px rgba(15, 33, 66, 0.04);
    }

    .painel h3 {
      margin: 0 0 0.7rem;
      color: #2b3f62;
      font-size: 1rem;
    }

    .form-grid {
      display: grid;
      gap: 0.45rem;
    }

    .form-grid label {
      color: #3f4f6b;
      font-weight: 600;
      font-size: 0.92rem;
    }

    .form-grid input {
      border: 1px solid #cfd6e3;
      border-radius: 8px;
      padding: 0.55rem 0.65rem;
      font-size: 0.98rem;
      max-width: 260px;
    }

    .field-error {
      margin: 0;
      color: #a62c2c;
      font-size: 0.84rem;
      font-weight: 600;
    }

    .btn-primario {
      margin-top: 0.25rem;
      border: 0;
      border-radius: 8px;
      padding: 0.58rem 0.9rem;
      background: #204da7;
      color: #fff;
      font-weight: 700;
      font-size: 0.92rem;
      width: fit-content;
      cursor: pointer;
    }

    .btn-primario:disabled {
      background: #9cb2da;
      cursor: not-allowed;
    }

    .lateral ul {
      margin: 0;
      padding: 0;
      list-style: none;
      display: grid;
      gap: 0.45rem;
      color: #344866;
    }

    .icon {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      width: 1.15rem;
      color: #1f7a47;
      font-weight: 700;
    }

    .feedback {
      margin-top: 0.9rem;
      border-radius: 10px;
      padding: 0.75rem;
      display: flex;
      align-items: flex-start;
      gap: 0.55rem;
      border: 1px solid;
    }

    .feedback h4 {
      margin: 0 0 0.25rem;
      font-size: 0.96rem;
    }

    .feedback p {
      margin: 0.15rem 0;
      font-size: 0.9rem;
    }

    .feedback-icon {
      width: 1.35rem;
      height: 1.35rem;
      border-radius: 999px;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      font-weight: 800;
      font-size: 0.8rem;
      margin-top: 0.1rem;
      flex: none;
    }

    .feedback-sucesso {
      background: #eef9f2;
      border-color: #cde8d6;
      color: #1f6d43;
    }

    .feedback-sucesso .feedback-icon {
      background: #dff2e7;
      color: #1f7a47;
    }

    .feedback-erro {
      background: #fff1f1;
      border-color: #f1cdcd;
      color: #8f2b2b;
    }

    .feedback-erro .feedback-icon {
      background: #f8dcdc;
      color: #a62c2c;
    }

    @media (max-width: 900px) {
      .topo {
        align-items: flex-start;
        flex-direction: column;
      }

      .layout-grid {
        grid-template-columns: 1fr;
      }
    }
  `,
})
export class GerarCicloComponent {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(CiclosService);
  private readonly router = inject(Router);

  protected readonly form = this.fb.nonNullable.group({
    competencia: ['', [Validators.required, Validators.pattern(/^\d{6}$/)]],
  });

  protected readonly loading = signal(false);
  protected readonly resultado = signal<GerarCicloResponse | null>(null);
  protected readonly erro = signal<string | null>(null);

  async enviar(): Promise<void> {
    if (this.form.invalid) {
      return;
    }

    this.loading.set(true);
    this.erro.set(null);
    this.resultado.set(null);

    try {
      const r = await firstValueFrom(this.service.gerar(this.form.getRawValue().competencia));
      this.resultado.set(r);
      await this.router.navigate(['/ciclos', r.cicloId]);
    } catch (err: unknown) {
      const detail = (err as { error?: { detail?: string }; message?: string })?.error?.detail
        ?? (err as { message?: string })?.message
        ?? 'Erro desconhecido';
      this.erro.set(detail);
    } finally {
      this.loading.set(false);
    }
  }
}
