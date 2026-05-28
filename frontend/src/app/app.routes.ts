import { Routes } from '@angular/router';
import { GerarCicloComponent } from './ciclos/gerar-ciclo.component';
import { ResumoCicloComponent } from './ciclos/resumo-ciclo.component';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'ciclos/novo' },
  { path: 'ciclos/novo', component: GerarCicloComponent },
  { path: 'ciclos/:cicloId', component: ResumoCicloComponent },
];
