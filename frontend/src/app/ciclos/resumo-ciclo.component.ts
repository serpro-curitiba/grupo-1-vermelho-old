import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { CiclosService, EventoAuditoriaResponse, ResumoCicloResponse } from './ciclos.service';

@Component({
  selector: 'sifap-resumo-ciclo',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <header class="topo">
      <div>
        <p class="topo-eyebrow">Ciclo de Pagamento</p>
        <h2>Resumo operacional</h2>
      </div>
      <a routerLink="/ciclos/novo" class="novo-ciclo-link">Gerar novo ciclo</a>
    </header>

    @if (loading()) {
      <p class="loading">Carregando resumo operacional...</p>
    }

    @if (erro(); as e) {
      <p class="err">{{ e }}</p>
    }

    @if (resumo(); as r) {
      <section class="resumo-head">
        <div>
          <p class="meta-label">Competência</p>
          <p class="meta-value">{{ formatarCompetencia(r.competencia) }}</p>
        </div>
        <div>
          <p class="meta-label">ID do ciclo</p>
          <p class="meta-id">{{ r.cicloId }}</p>
        </div>
        <div>
          <p class="meta-label">Status</p>
          <span class="status-chip">{{ r.status }}</span>
        </div>
      </section>

      <section class="card-grid">
        <article class="card">
          <div class="card-title">
            <span class="icon icon-neutral">📦</span>
            <h3>Total processados</h3>
          </div>
          <p class="card-value">{{ r.totalCandidatos }}</p>
        </article>

        <article class="card">
          <div class="card-title">
            <span class="icon icon-success">✓</span>
            <h3>Pagamentos gerados</h3>
          </div>
          <p class="card-value card-value-success">{{ r.totalPagamentos }}</p>
        </article>

        <article class="card">
          <div class="card-title">
            <span class="icon icon-danger">!</span>
            <h3>Rejeitados</h3>
          </div>
          <p class="card-value card-value-danger">{{ r.totalRejeitados }}</p>
        </article>

        <article class="card">
          <div class="card-title">
            <span class="icon icon-neutral">↺</span>
            <h3>Ignorados</h3>
          </div>
          <p class="card-value">{{ r.totalIgnorados }}</p>
        </article>

        <article class="card">
          <div class="card-title">
            <span class="icon icon-primary">$</span>
            <h3>Valor total</h3>
          </div>
          <p class="card-value card-value-primary">{{ r.valorTotal | currency:'BRL':'symbol':'1.2-2':'pt-BR' }}</p>
        </article>
      </section>

      <section class="detalhes">
        <h3>Composição financeira</h3>
        <table class="finance-table">
          <thead>
            <tr>
              <th>Componente</th>
              <th>Valor</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td>Total bruto</td>
              <td>{{ r.totalBruto | currency:'BRL':'symbol':'1.2-2':'pt-BR' }}</td>
            </tr>
            <tr>
              <td>Total descontos</td>
              <td>{{ r.totalDesconto | currency:'BRL':'symbol':'1.2-2':'pt-BR' }}</td>
            </tr>
            <tr>
              <td>Total líquido</td>
              <td>{{ r.totalLiquido | currency:'BRL':'symbol':'1.2-2':'pt-BR' }}</td>
            </tr>
            <tr>
              <td>Total abono</td>
              <td>{{ r.totalAbono | currency:'BRL':'symbol':'1.2-2':'pt-BR' }}</td>
            </tr>
            <tr>
              <td>Total 13º</td>
              <td>{{ r.total13 | currency:'BRL':'symbol':'1.2-2':'pt-BR' }}</td>
            </tr>
          </tbody>
        </table>
      </section>

      <section class="detalhes">
        <h3>Eventos de auditoria</h3>

        <form [formGroup]="filtroForm" class="filtro-form">
          <label for="filtroBusca">Filtrar por ID, tipo, ação ou usuário</label>
          <input
            id="filtroBusca"
            type="text"
            formControlName="busca"
            placeholder="Ex.: CICLO, sistema-dev, AT"
          />
        </form>

        @if (loadingAuditoria()) {
          <p class="loading">Carregando trilha de auditoria...</p>
        }

        @if (!loadingAuditoria() && eventosFiltrados().length === 0) {
          <p>Nenhum evento de auditoria encontrado para este ciclo.</p>
        }

        @if (eventosFiltrados().length > 0) {
          <table class="audit-table">
            <thead>
              <tr>
                <th>Data/Hora</th>
                <th>Ação</th>
                <th>Tipo</th>
                <th>Usuário</th>
                <th>Sucesso</th>
              </tr>
            </thead>
            <tbody>
              @for (evento of eventosFiltrados(); track evento.id) {
                <tr>
                  <td>{{ evento.ocorridoEm | date:'dd/MM/yyyy HH:mm:ss':'UTC' }}</td>
                  <td>{{ evento.acao ?? '-' }}</td>
                  <td>{{ evento.tipo }}</td>
                  <td>{{ evento.usuarioId }}</td>
                  <td>
                    <span [class]="evento.sucesso ? 'pill pill-ok' : 'pill pill-no'">
                      {{ evento.sucesso ? 'Sim' : 'Não' }}
                    </span>
                  </td>
                </tr>
              }
            </tbody>
          </table>
        }
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

    .novo-ciclo-link {
      border: 1px solid #cfd6e3;
      border-radius: 8px;
      padding: 0.45rem 0.7rem;
      text-decoration: none;
      color: #21385f;
      font-weight: 600;
      background: #fff;
    }

    .resumo-head {
      background: linear-gradient(100deg, #eef4ff, #f9fbff);
      border: 1px solid #dbe5f4;
      border-radius: 12px;
      padding: 0.8rem;
      margin-bottom: 0.9rem;
      display: grid;
      gap: 0.7rem;
      grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
    }

    .meta-label {
      margin: 0;
      font-size: 0.8rem;
      color: #617391;
      text-transform: uppercase;
      letter-spacing: 0.04em;
      font-weight: 600;
    }

    .meta-value,
    .meta-id {
      margin: 0.2rem 0 0;
      font-weight: 700;
      font-size: 1.02rem;
    }

    .meta-id {
      font-size: 0.86rem;
      color: #2f4469;
      word-break: break-all;
    }

    .status-chip {
      display: inline-block;
      margin-top: 0.2rem;
      border-radius: 999px;
      padding: 0.2rem 0.55rem;
      background: #e8f7ef;
      color: #1f7a47;
      font-size: 0.82rem;
      font-weight: 700;
    }

    .card-grid {
      display: grid;
      gap: 0.75rem;
      grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
      margin-bottom: 1rem;
    }

    .card {
      border: 1px solid #d8e0ed;
      border-radius: 12px;
      padding: 0.8rem;
      background: #fff;
      box-shadow: 0 1px 2px rgba(15, 33, 66, 0.04);
    }

    .card-title {
      display: flex;
      align-items: center;
      gap: 0.45rem;
      margin-bottom: 0.35rem;
    }

    .card h3 {
      margin: 0;
      font-size: 0.9rem;
      font-weight: 600;
      color: #3f4f6b;
    }

    .icon {
      width: 1.4rem;
      height: 1.4rem;
      border-radius: 999px;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      font-size: 0.8rem;
      font-weight: 700;
    }

    .icon-neutral { background: #edf1f7; color: #455877; }
    .icon-success { background: #e8f7ef; color: #1f7a47; }
    .icon-danger { background: #fdeaea; color: #a62c2c; }
    .icon-primary { background: #e8efff; color: #204da7; }

    .card-value {
      margin: 0;
      font-size: 1.45rem;
      font-weight: 600;
    }

    .card-value-success { color: #1f7a47; }
    .card-value-danger { color: #a62c2c; }
    .card-value-primary { color: #204da7; }

    .detalhes {
      border: 1px solid #e2e8f2;
      border-radius: 12px;
      padding: 0.8rem;
      margin-bottom: 0.9rem;
      background: #fff;
    }

    .detalhes h3 {
      margin-top: 0;
      margin-bottom: 0.65rem;
      color: #2b3f62;
    }

    .finance-table,
    .audit-table {
      width: 100%;
      border-collapse: collapse;
      font-size: 0.9rem;
    }

    .finance-table th,
    .finance-table td,
    .audit-table th,
    .audit-table td {
      border-bottom: 1px solid #e6ebf2;
      text-align: left;
      padding: 0.52rem 0.38rem;
    }

    .finance-table td:last-child,
    .audit-table td:last-child {
      text-align: right;
    }

    .filtro-form {
      margin-bottom: 0.75rem;
      display: grid;
      gap: 0.35rem;
    }

    .filtro-form input {
      border: 1px solid #ccc;
      border-radius: 6px;
      padding: 0.5rem 0.6rem;
      font-size: 0.95rem;
      width: 100%;
      max-width: 520px;
    }

    .finance-table th,
    .audit-table th {
      color: #60728f;
      font-weight: 600;
      font-size: 0.82rem;
      text-transform: uppercase;
      letter-spacing: 0.04em;
    }

    .pill {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      min-width: 2.2rem;
      border-radius: 999px;
      padding: 0.12rem 0.45rem;
      font-size: 0.78rem;
      font-weight: 700;
    }

    .pill-ok { background: #e8f7ef; color: #1f7a47; }
    .pill-no { background: #fdeaea; color: #a62c2c; }

    .loading {
      color: #52637f;
    }

    .err {
      color: #b00020;
      font-weight: 600;
    }
  `,
})
export class ResumoCicloComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly service = inject(CiclosService);
  private readonly fb = inject(FormBuilder);

  protected readonly loading = signal(false);
  protected readonly loadingAuditoria = signal(false);
  protected readonly erro = signal<string | null>(null);
  protected readonly resumo = signal<ResumoCicloResponse | null>(null);
  protected readonly eventos = signal<readonly EventoAuditoriaResponse[]>([]);
  protected readonly cicloId = computed(() => this.route.snapshot.paramMap.get('cicloId') ?? '');

  protected readonly filtroForm = this.fb.nonNullable.group({
    busca: '',
  });

  protected readonly eventosFiltrados = computed(() => {
    const termo = this.filtroForm.controls.busca.value.trim().toLowerCase();
    if (!termo) {
      return this.eventos();
    }

    return this.eventos().filter(evento => {
      const acao = (evento.acao ?? '').toLowerCase();
      const tipo = evento.tipo.toLowerCase();
      const usuario = evento.usuarioId.toLowerCase();
      const id = evento.id.toLowerCase();
      return acao.includes(termo)
        || tipo.includes(termo)
        || usuario.includes(termo)
        || id.includes(termo);
    });
  });

  ngOnInit(): void {
    void this.carregar();
  }

  protected formatarCompetencia(competencia: string): string {
    if (competencia.length !== 6) {
      return competencia;
    }
    return `${competencia.substring(4)}/${competencia.substring(0, 4)}`;
  }

  private async carregar(): Promise<void> {
    const cicloId = this.cicloId();
    if (!cicloId) {
      this.erro.set('Ciclo inválido.');
      return;
    }

    this.loading.set(true);
    this.erro.set(null);

    try {
      const data = await firstValueFrom(this.service.resumo(cicloId));
      this.resumo.set(data);
      await this.carregarAuditoria(cicloId);
    } catch (err: unknown) {
      const detail = (err as { error?: { detail?: string }; message?: string })?.error?.detail
        ?? (err as { message?: string })?.message
        ?? 'Falha ao carregar resumo do ciclo';
      this.erro.set(detail);
    } finally {
      this.loading.set(false);
    }
  }

  private async carregarAuditoria(cicloId: string): Promise<void> {
    this.loadingAuditoria.set(true);
    try {
      const data = await firstValueFrom(this.service.eventosAuditoria(cicloId));
      this.eventos.set(data);
    } catch {
      this.eventos.set([]);
    } finally {
      this.loadingAuditoria.set(false);
    }
  }
}
