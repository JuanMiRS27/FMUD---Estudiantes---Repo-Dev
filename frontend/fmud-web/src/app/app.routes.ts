import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';
import { ShellComponent } from './core/layout/shell/shell.component';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login.component').then((m) => m.LoginComponent)
  },
  {
    path: '',
    component: ShellComponent,
    canActivate: [authGuard],
    children: [
      {
        path: 'dashboard',
        canActivate: [roleGuard],
        data: { roles: ['SECRETARIO', 'ADMIN'] },
        loadComponent: () => import('./features/dashboard/dashboard.component').then((m) => m.DashboardComponent)
      },
      {
        path: 'students',
        canActivate: [roleGuard],
        data: { roles: ['SECRETARIO', 'ADMIN'] },
        loadComponent: () => import('./features/students/students.component').then((m) => m.StudentsComponent)
      },
      {
        path: 'students/:id/resume',
        canActivate: [roleGuard],
        data: { roles: ['SECRETARIO', 'ADMIN'] },
        loadComponent: () => import('./features/resumes/resume.component').then((m) => m.ResumeComponent)
      },
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' }
    ]
  },
  { path: '**', redirectTo: 'dashboard' }
];
