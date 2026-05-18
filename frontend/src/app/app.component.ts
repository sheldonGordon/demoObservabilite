import { HttpClient } from '@angular/common/http';
import { Component } from '@angular/core';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'demoObservabilite';
  apiMessage = 'Aucune réponse du backend pour le moment.';
  secureApiMessage = 'Aucune reponse de l\'endpoint securise pour le moment.';
  token = '';
  readonly username = 'demo';
  readonly password = 'demo';

  constructor(private readonly http: HttpClient) {}

  chargerBackend(): void {
    this.http
      .get<{ message?: string }>('/api/hello')
      .subscribe({
        next: (response) => {
          this.apiMessage = response.message ?? 'Réponse reçue sans message.';
        },
        error: () => {
          this.apiMessage = 'Impossible de joindre le backend. Verifie que le backend est demarre.';
        }
      });
  }

  chargerBackendSecurise(): void {
    this.http
      .post<{ token: string }>('/api/auth/token', {
        username: this.username,
        password: this.password
      })
      .subscribe({
        next: (authResponse) => {
          this.token = authResponse.token;
          this.http
            .get<{ message?: string; user?: string }>('/api/secure/hello', {
              headers: { Authorization: `Bearer ${this.token}` }
            })
            .subscribe({
              next: (secureResponse) => {
                const message = secureResponse.message ?? 'Reponse securisee recue.';
                const user = secureResponse.user ?? 'unknown';
                this.secureApiMessage = `${message} (user=${user})`;
              },
              error: () => {
                this.secureApiMessage = 'Echec appel endpoint securise.';
              }
            });
        },
        error: () => {
          this.secureApiMessage = 'Impossible d\'obtenir un token JWT.';
        }
      });
  }
}

