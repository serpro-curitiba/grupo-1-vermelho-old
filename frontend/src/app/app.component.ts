import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'sifap-root',
  standalone: true,
  imports: [RouterOutlet],
  template: `
    <header>
      <h1>SIFAP 2.0 — Portal de Operações</h1>
    </header>
    <main>
      <router-outlet />
    </main>
  `,
})
export class AppComponent { }
