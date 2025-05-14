import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

/**
 * Service for interacting with DataSource configurations.
 */
@Injectable({ providedIn: 'root' })
export class DataSourceService {
  private http = inject(HttpClient);

  /** List all DataSource configurations. */
  list(): Observable<any[]> {
    return this.http.get<any[]>('/api/datasources');
  }

  /** Import metadata for a given DataSource ID. */
  import(id: number): Observable<any[]> {
    return this.http.post<any[]>(`/api/datasources/${id}/import`, null);
  }
}