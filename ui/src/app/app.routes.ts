import { Routes } from '@angular/router';

export const routes: Routes = [
  // Redirect root to DataSources list
  { path: '', redirectTo: '/datasources', pathMatch: 'full' },
  {
    path: 'datasources',
    loadComponent: () => import('./datasources/datasources-list.component').then(m => m.DatasourcesListComponent)
  }
];
