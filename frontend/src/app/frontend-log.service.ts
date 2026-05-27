import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

export interface FrontendTraceContext {
  trace_id: string;
  session_id: string;
}

interface FrontendLogPayload {
  trace_id: string;
  span_id: string;
  session_id: string;
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
  private readonly sessionId = this.getOrCreateStorageValue('demo.session.id', () => crypto.randomUUID());

  constructor(private readonly http: HttpClient) {}

  createTraceContext(): FrontendTraceContext {
    return {
      trace_id: this.generateTraceId(),
      session_id: this.sessionId
    };
  }

  getSessionId(): string {
    return this.sessionId;
  }

  getSpanId(): string {
    return this.generateSpanId();
  }

  debug(
    event: string,
    message: string,
    context: Record<string, unknown> = {},
    traceContext?: FrontendTraceContext
  ): Observable<void> {
    return this.send('DEBUG', event, message, context, traceContext);
  }

  info(
    event: string,
    message: string,
    context: Record<string, unknown> = {},
    traceContext?: FrontendTraceContext
  ): Observable<void> {
    return this.send('INFO', event, message, context, traceContext);
  }

  warn(
    event: string,
    message: string,
    context: Record<string, unknown> = {},
    traceContext?: FrontendTraceContext
  ): Observable<void> {
    return this.send('WARN', event, message, context, traceContext);
  }

  error(
    event: string,
    message: string,
    context: Record<string, unknown> = {},
    traceContext?: FrontendTraceContext
  ): Observable<void> {
    return this.send('ERROR', event, message, context, traceContext);
  }

  private send(
    level: 'DEBUG' | 'INFO' | 'WARN' | 'ERROR',
    event: string,
    message: string,
    context: Record<string, unknown>,
    traceContext?: FrontendTraceContext
  ): Observable<void> {
    const resolvedTraceContext = traceContext ?? this.createTraceContext();
    const payload: FrontendLogPayload = {
      trace_id: resolvedTraceContext.trace_id,
      span_id: this.generateSpanId(),
      session_id: resolvedTraceContext.session_id,
      level,
      event,
      message,
      timestamp: new Date().toISOString(),
      route: window.location.pathname,
      userAgent: navigator.userAgent,
      context
    };

    return this.http.post(this.endpoint, payload).pipe(
      map(() => void 0),
      catchError(() => of(void 0))  // Keep app flow stable even if log ingestion is unavailable.
    );
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

  private generateSpanId(): string {
    const bytes = new Uint8Array(8);
    crypto.getRandomValues(bytes);
    return Array.from(bytes, (value) => value.toString(16).padStart(2, '0')).join('');
  }
}
