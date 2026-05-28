import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

export interface GerarCicloResponse {
  cicloId: string;
  status: string;
  totalPagamentos: number;
  valorTotal: number;
}

export interface ResumoCicloResponse {
  cicloId: string;
  competencia: string;
  status: string;
  totalCandidatos: number;
  totalPagamentos: number;
  totalIgnorados: number;
  totalRejeitados: number;
  valorTotal: number;
  totalBruto: number;
  totalDesconto: number;
  totalLiquido: number;
  totalAbono: number;
  total13: number;
}

export interface EventoAuditoriaResponse {
  id: string;
  ocorridoEm: string;
  acao: string | null;
  tipo: string;
  agregado: string;
  agregadoId: string;
  usuarioId: string;
  ipOrigem: string | null;
  idCorrelacao: string | null;
  sucesso: boolean;
}

/**
 * Service que chama POST /api/v1/ciclos (REQ-PAY-001).
 *
 * Base URL configurável por ambiente — em produção apontará para o gateway atrás do Entra ID.
 */
@Injectable({ providedIn: 'root' })
export class CiclosService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = (globalThis as { SIFAP_API_BASE_URL?: string })
    .SIFAP_API_BASE_URL ?? '/api/v1';

  gerar(competencia: string): Observable<GerarCicloResponse> {
    return this.http.post<GerarCicloResponse>(`${this.baseUrl}/ciclos`, { competencia });
  }

  resumo(cicloId: string): Observable<ResumoCicloResponse> {
    return this.http.get<ResumoCicloResponse>(`${this.baseUrl}/ciclos/${cicloId}/resumo`);
  }

  eventosAuditoria(cicloId: string): Observable<EventoAuditoriaResponse[]> {
    return this.http.get<EventoAuditoriaResponse[]>('/api/v1/auditoria/eventos', {
      params: {
        agregadoId: cicloId,
      },
    });
  }
}
