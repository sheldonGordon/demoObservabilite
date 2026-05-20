import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

interface FrontendLogPayload {
  traceId: string;
  sessionId: string;
  level: 'DEBUG' | 'INFO' | 'WARN' | 'ERROR';
  event: string;
  message: string;
  timestamp: string;
  route: string;
  userAgent: string;
  context: Record<string, unknown>;
}

@Injectable({ providedIn: 'root' })
export class FrontendLogService {
  private readonly endpoint = '/api/logs/frontend';
  private readonly traceId = this.getOrCreateStorageValue('demo.trace.id', () => this.generateTraceId());
  private readonly sessionId = this.getOrCreateStorageValue('demo.session.id', () => crypto.randomUUID());

  constructor(private readonly http: HttpClient) {}

  getTraceId(): string {
    return this.traceId;
  }

  getSessionId(): string {
    return this.sessionId;
  }

  info(event: string, message: string, context: Record<string, unknown> = {}): void {
    this.send('INFO', event, message, context);
  }

  warn(event: string, message: string, context: Record<string, unknown> = {}): void {
    this.send('WARN', event, message, context);
  }

  error(event: string, message: string, context: Record<string, unknown> = {}): void {
    this.send('ERROR', event, message, context);
  }

  private send(
    level: 'DEBUG' | 'INFO' | 'WARN' | 'ERROR',
    event: string,
    message: string,
    context: Record<string, unknown>
  ): void {
    const payload: FrontendLogPayload = {
      traceId: this.traceId,
      sessionId: this.sessionId,
      level,
      event,
      message,
      timestamp: new Date().toISOString(),
      route: window.location.pathname,
      userAgent: navigator.userAgent,
      context
    };

    this.http.post(this.endpoint, payload).subscribe({
      error: () => {
        // Keep app flow stable even if log ingestion is unavailable.
      }
    });
  }

  private getOrCreateStorageValue(key: string, generator: () => string): string {
    const existing = localStorage.getItem(key);
    if (existing) {
      return existing;
    }

    const created = generator();
    localStorage.setItem(key, created);
    return created;
  }

  private generateTraceId(): string {
    const bytes = new Uint8Array(16);
    crypto.getRandomValues(bytes);
    return Array.from(bytes, (value) => value.toString(16).padStart(2, '0')).join('');
  }
}

