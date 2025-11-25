import { Routes } from '@angular/router';
import { LandingComponent } from './components/landing/landing.component';
import { LoginComponent } from './components/login/login.component';
import { RegisterComponent } from './components/register/register.component';
import { HomeComponent } from './components/home/home.component';
import { ProblemsComponent } from './components/problems/problems.component';
import { ProblemDetailComponent } from './components/problem-detail/problem-detail.component';
import { SubmissionsComponent } from './components/submissions/submissions.component';
import { AdminProblemsComponent } from './components/admin/admin-problems.component';

export const routes: Routes = [
  { path: '', component: LandingComponent },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'home', component: HomeComponent },
  { path: 'problems', component: ProblemsComponent },
  { path: 'problems/:id', component: ProblemDetailComponent },
  { path: 'submissions', component: SubmissionsComponent },
  { path: 'admin/problems', component: AdminProblemsComponent },
  { path: '**', redirectTo: '/' }
];
