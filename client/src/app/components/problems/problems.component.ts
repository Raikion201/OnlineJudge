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

@Component({
  selector: 'app-problems',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="page">
      <nav class="navbar">
        <div class="nav-container">
          <div class="logo">
            <h1>OnlineJudge</h1>
          </div>
          <div class="nav-links">
            <button (click)="goHome()" class="nav-btn">Home</button>
            @if (isAdmin()) {
              <button (click)="goToAdmin()" class="nav-btn">Admin Panel</button>
            }
          </div>
        </div>
      </nav>

      <div class="content">
        <h1 class="page-title">Practice Problems</h1>

      @if (loading()) {
        <p>Loading...</p>
      } @else if (error()) {
        <div class="error">{{ error() }}</div>
      } @else {
        <div class="problems-list">
          @for (problem of problems(); track problem.id) {
            <div class="problem-card" (click)="goToProblem(problem.id!)">
              <div class="problem-header">
                <h3>{{ problem.id }}. {{ problem.title }}</h3>
                <span class="difficulty" [class]="'difficulty-' + problem.difficulty.toLowerCase()">
                  {{ formatDifficulty(problem.difficulty) }}
                </span>
              </div>
              <p class="description">{{ problem.description }}</p>
              @if (problem.tags && problem.tags.length > 0) {
                <div class="tags">
                  @for (tag of problem.tags; track tag) {
                    <span class="tag">{{ tag }}</span>
                  }
                </div>
              }
            </div>
          } @empty {
            <div class="empty">No problems found.</div>
          }
        </div>
      }
      </div>
    </div>
  `,
  styles: [`
    .page {
      min-height: 100vh;
      background: #ffffff;
    }

    .navbar {
      background: #000000;
      padding: 1rem 0;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    }

    .nav-container {
      display: flex;
      justify-content: space-between;
      align-items: center;
      max-width: 1200px;
      margin: 0 auto;
      padding: 0 2rem;
    }

    .logo h1 {
      color: white;
      margin: 0;
      font-size: 24px;
      font-weight: 700;
    }

    .nav-links {
      display: flex;
      gap: 1rem;
      align-items: center;
    }

    .nav-btn {
      background: transparent;
      border: 2px solid white;
      color: white;
      padding: 0.5rem 1rem;
      cursor: pointer;
      font-size: 14px;
      font-weight: 600;
      border-radius: 6px;
      transition: all 0.2s;
    }

    .nav-btn:hover {
      background: white;
      color: #000;
    }

    .content {
      max-width: 1200px;
      margin: 0 auto;
      padding: 3rem 2rem;
    }

    .page-title {
      margin: 0 0 2rem 0;
      font-size: 2.5rem;
      color: #000;
      font-weight: 800;
    }

    .problems-list {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
      gap: 1.5rem;
    }

    .problem-card {
      background: white;
      padding: 1.5rem;
      border: 2px solid #e0e0e0;
      border-radius: 12px;
      transition: all 0.3s;
      cursor: pointer;
    }

    .problem-card:hover {
      border-color: #000;
      box-shadow: 0 4px 12px rgba(0,0,0,0.15);
      transform: translateY(-4px);
    }

    .problem-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 1rem;
    }

    h3 {
      margin: 0;
      font-size: 1.2rem;
      color: #000;
      flex: 1;
      line-height: 1.4;
      font-weight: 700;
    }

    .difficulty {
      padding: 6px 14px;
      border-radius: 20px;
      font-size: 12px;
      font-weight: 700;
      text-transform: uppercase;
      letter-spacing: 0.5px;
    }

    .difficulty-easy {
      background: #000;
      color: #90EE90;
    }

    .difficulty-medium {
      background: #000;
      color: #FFD700;
    }

    .difficulty-hard {
      background: #000;
      color: #FF6B6B;
    }

    .description {
      margin: 0 0 12px 0;
      color: #666;
      line-height: 1.6;
    }

    .tags {
      display: flex;
      gap: 8px;
      flex-wrap: wrap;
    }

    .tag {
      background: #f5f5f5;
      color: #333;
      padding: 4px 10px;
      border-radius: 4px;
      font-size: 12px;
    }

    .empty, .error {
      text-align: center;
      padding: 3rem;
      color: #666;
      font-size: 1.1rem;
    }

    .error {
      color: #dc3545;
    }

    .tag {
      padding: 4px 10px;
      background: #f5f5f5;
      border-radius: 4px;
      font-size: 12px;
      color: #666;
    }

    .error {
      padding: 15px;
      background: #f8d7da;
      color: #721c24;
      border-radius: 6px;
      margin-bottom: 20px;
    }
  `]
})
export class ProblemsComponent implements OnInit {
  private http = inject(HttpClient);
  private router = inject(Router);
  private authService = inject(AuthService);

  problems = signal<Problem[]>([]);
  loading = signal(false);
  error = signal('');
  isAdmin = signal(false);

  ngOnInit() {
    this.checkAdmin();
    this.loadProblems();
  }

  checkAdmin() {
    const profile = this.authService.userProfile();
    // Check for Admin role (case-insensitive)
    const hasAdminRole = profile?.roles?.some(role => 
      role.toLowerCase() === 'admin'
    ) || false;
    this.isAdmin.set(hasAdminRole);
  }

  loadProblems() {
    this.loading.set(true);
    this.error.set('');

    this.http.get<any>('http://localhost:8085/api/v1/problems').subscribe({
      next: (data) => {
        console.log('Problems API Response:', data);
        // Extract content array from Page response
        const problemsArray = data.content || [];
        this.problems.set(problemsArray);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Load problems error:', err);
        this.error.set(`Failed to load problems: ${err.status} ${err.statusText}`);
        this.loading.set(false);
      }
    });
  }

  goToAdmin() {
    this.router.navigate(['/admin/problems']);
  }

  goToProblem(id: number) {
    this.router.navigate(['/problems', id]);
  }

  formatDifficulty(diff: string): string {
    return diff.charAt(0) + diff.slice(1).toLowerCase();
  }

  goHome() {
    this.router.navigate(['/home']);
  }
}
