import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

export interface GerarCicloResponse {
  cicloId: string;
  status: string;
  totalPagamentos: number;
  valorTotal: number;
}

/**
 * Service que chama POST /api/v1/ciclos (REQ-PAY-001).
 *
 * Base URL configurável por ambiente — em produção apontará para o gateway atrás do Entra ID.
 */
@Injectable({ providedIn: 'root' })
export class CiclosService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = (window as unknown as { SIFAP_API_BASE_URL?: string })
    .SIFAP_API_BASE_URL ?? '/api/v1';

  gerar(competencia: string): Observable<GerarCicloResponse> {
    return this.http.post<GerarCicloResponse>(`${this.baseUrl}/ciclos`, { competencia });
  }
}
