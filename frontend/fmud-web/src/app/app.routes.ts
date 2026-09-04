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
        path: 'hojas-de-vida',
        canActivate: [roleGuard],
        data: { roles: ['SECRETARIO', 'ADMIN'] },
        loadComponent: () => import('./features/students/students.component').then((m) => m.StudentsComponent)
      },
      {
        path: 'hojas-de-vida/nueva',
        canActivate: [roleGuard],
        data: { roles: ['SECRETARIO', 'ADMIN'] },
        loadComponent: () => import('./features/resumes/resume.component').then((m) => m.ResumeComponent)
      },
      {
        path: 'hojas-de-vida/:id',
        canActivate: [roleGuard],
        data: { roles: ['SECRETARIO', 'ADMIN'] },
        loadComponent: () => import('./features/resumes/resume.component').then((m) => m.ResumeComponent)
      },
      {
        path: 'users',
        canActivate: [roleGuard],
        data: { roles: ['ADMIN'] },
        loadComponent: () => import('./features/users/users.component').then((m) => m.UsersComponent)
      },
      { path: '', pathMatch: 'full', redirectTo: 'hojas-de-vida' }
    ]
  },
  { path: '**', redirectTo: 'hojas-de-vida' }
];
