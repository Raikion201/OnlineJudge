import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../services/auth.service';

interface Problem {
  id?: number;
  title: string;
  description: string;
  difficulty: string;
  tags?: string[];
}

interface Page<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
}

@Component({
  selector: 'app-problems',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './problems.component.html',
  styleUrls: ['./problems.component.scss']
})
export class ProblemsComponent implements OnInit {
  private router = inject(Router);
  private http = inject(HttpClient);
  private authService = inject(AuthService);

  problems = signal<Problem[]>([]);
  loading = signal<boolean>(true);
  error = signal<string | null>(null);

  ngOnInit() {
    this.loadProblems();
  }

  loadProblems() {
    this.loading.set(true);
    this.http.get<Page<Problem>>('http://localhost:8087/api/v1/problems')
      .subscribe({
        next: (data) => {
          this.problems.set(data.content);
          this.loading.set(false);
        },
        error: (err) => {
          console.error('Failed to load problems', err);
          this.error.set('Failed to load problems. Please try again later.');
          this.loading.set(false);
        }
      });
  }

  goToProblem(id: number) {
    this.router.navigate(['/problems', id]);
  }

  formatDifficulty(difficulty: string): string {
    return difficulty.charAt(0).toUpperCase() + difficulty.slice(1).toLowerCase();
  }
}
