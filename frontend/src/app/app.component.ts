import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FrontendLogService } from './frontend-log.service';

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
  standalone: false,
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

  constructor(
    private readonly http: HttpClient,
    private readonly frontendLogService: FrontendLogService
  ) {}

  ngOnInit(): void {
    const storedToken = localStorage.getItem('demo.jwt.token');
    this.frontendLogService.info('APP_INIT', 'Initialisation de l\'application frontend', {
      hadStoredToken: !!storedToken
    });

    if (storedToken) {
      this.authToken = storedToken;
      this.frontendLogService.info('AUTH_RESTORE', 'Session JWT restauree depuis le stockage local');
      this.loadFilms();
    }
  }

  get isAuthenticated(): boolean {
    return this.authToken.length > 0;
  }

  login(): void {
    this.loadingLogin = true;
    this.loginError = '';
    this.frontendLogService.info('AUTH_LOGIN_ATTEMPT', 'Tentative de connexion utilisateur', {
      username: this.username
    });

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
          this.frontendLogService.info('AUTH_LOGIN_SUCCESS', 'Connexion reussie');
          this.loadFilms();
        },
        error: (error) => {
          this.loginError = 'Connexion impossible. Verifie username/password.';
          this.frontendLogService.warn('AUTH_LOGIN_FAILED', 'Echec de connexion', {
            status: error?.status ?? 'n/a'
          });
          this.loadingLogin = false;
        }
      });
  }

  logout(): void {
    this.frontendLogService.info('AUTH_LOGOUT', 'Deconnexion utilisateur');
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
          this.frontendLogService.info('FILMS_LIST_SUCCESS', 'Liste des films chargee', {
            count: response.length
          });
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
            this.frontendLogService.warn('FILMS_LIST_UNAUTHORIZED', 'Session expiree pendant chargement des films', {
              status: error?.status
            });
          } else {
            this.filmsError = 'Impossible de charger les films.';
            this.frontendLogService.error('FILMS_LIST_ERROR', 'Erreur de chargement de la liste des films', {
              status: error?.status ?? 'n/a'
            });
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
    this.frontendLogService.info('FILM_DETAILS_REQUEST', 'Chargement du detail film', {
      filmId: film.id
    });

    this.http
      .get<FilmDetails>(`/api/films/${film.id}`, { headers: this.authHeaders() })
      .subscribe({
        next: (response) => {
          this.selectedFilm = response;
          this.frontendLogService.info('FILM_DETAILS_SUCCESS', 'Detail film charge', {
            filmId: film.id
          });
          this.loadingDetails = false;
        },
        error: (error) => {
          if (error?.status === 401 || error?.status === 403) {
            this.logout();
            this.loginError = 'Session expiree. Reconnecte-toi.';
            this.frontendLogService.warn('FILM_DETAILS_UNAUTHORIZED', 'Session expiree pendant chargement du detail film', {
              status: error?.status,
              filmId: film.id
            });
          } else {
            this.detailsError = 'Impossible de charger le detail du film.';
            this.frontendLogService.error('FILM_DETAILS_ERROR', 'Erreur de chargement du detail film', {
              status: error?.status ?? 'n/a',
              filmId: film.id
            });
          }
          this.loadingDetails = false;
        }
      });
  }

  private authHeaders(): HttpHeaders {
    return new HttpHeaders({
      Authorization: `Bearer ${this.authToken}`,
      'X-Trace-Id': this.frontendLogService.getTraceId(),
      'X-Span-Id': this.frontendLogService.getSpanId(),
      'X-Session-Id': this.frontendLogService.getSessionId()
    });
  }
}
