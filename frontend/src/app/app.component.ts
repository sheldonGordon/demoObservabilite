import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';

interface FilmSummary {
  id: number;
  title: string;
  release_year: number;
  genre: string;
}

interface FilmDetails {
  id: number;
  title: string;
  originalTitle?: string;
  release_year: number;
  genre: string;
  director: string;
  durationMinutes: number;
  language: string;
  country?: string;
  ageRating?: string;
  imdbScore?: number;
  synopsis?: string;
}

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  username = 'demo';
  password = 'demo';
  loginError = '';
  loadingLogin = false;
  authToken = '';

  films: FilmSummary[] = [];
  selectedFilm?: FilmDetails;
  loadingFilms = false;
  loadingDetails = false;
  filmsError = '';
  detailsError = '';

  constructor(private readonly http: HttpClient) {}

  ngOnInit(): void {
    const storedToken = localStorage.getItem('demo.jwt.token');
    if (storedToken) {
      this.authToken = storedToken;
      this.loadFilms();
    }
  }

  get isAuthenticated(): boolean {
    return this.authToken.length > 0;
  }

  login(): void {
    this.loadingLogin = true;
    this.loginError = '';

    this.http
      .post<{ token: string }>('/api/auth/token', {
        username: this.username,
        password: this.password
      })
      .subscribe({
        next: (response) => {
          this.authToken = response.token;
          localStorage.setItem('demo.jwt.token', this.authToken);
          this.loadingLogin = false;
          this.loadFilms();
        },
        error: () => {
          this.loginError = 'Connexion impossible. Verifie username/password.';
          this.loadingLogin = false;
        }
      });
  }

  logout(): void {
    this.authToken = '';
    localStorage.removeItem('demo.jwt.token');
    this.films = [];
    this.selectedFilm = undefined;
    this.filmsError = '';
    this.detailsError = '';
  }

  loadFilms(): void {
    if (!this.isAuthenticated) {
      return;
    }

    this.loadingFilms = true;
    this.filmsError = '';

    this.http
      .get<FilmSummary[]>('/api/films', { headers: this.authHeaders() })
      .subscribe({
        next: (response) => {
          this.films = response;
          if (this.films.length > 0) {
            this.selectFilm(this.films[0]);
          } else {
            this.selectedFilm = undefined;
          }
          this.loadingFilms = false;
        },
        error: (error) => {
          if (error?.status === 401 || error?.status === 403) {
            this.logout();
            this.loginError = 'Session expiree. Reconnecte-toi.';
          } else {
            this.filmsError = 'Impossible de charger les films.';
          }
          this.loadingFilms = false;
        }
      });
  }

  selectFilm(film: FilmSummary): void {
    if (!this.isAuthenticated) {
      return;
    }

    this.loadingDetails = true;
    this.detailsError = '';

    this.http
      .get<FilmDetails>(`/api/films/${film.id}`, { headers: this.authHeaders() })
      .subscribe({
        next: (response) => {
          this.selectedFilm = response;
          this.loadingDetails = false;
        },
        error: (error) => {
          if (error?.status === 401 || error?.status === 403) {
            this.logout();
            this.loginError = 'Session expiree. Reconnecte-toi.';
          } else {
            this.detailsError = 'Impossible de charger le detail du film.';
          }
          this.loadingDetails = false;
        }
      });
  }

  private authHeaders(): HttpHeaders {
    return new HttpHeaders({
      Authorization: `Bearer ${this.authToken}`
    });
  }
}

