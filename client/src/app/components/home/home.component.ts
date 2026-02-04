import { Component, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="page">
      <div class="content">
        <div class="welcome-card">
          @if (isAuthenticated()) {
            <div class="user-info">
              <h2>Welcome back, {{ username() }}! 👋</h2>
              <p class="email">{{ email() }}</p>
              
              <div class="roles-section">
                <strong>Your Roles:</strong>
                <div class="roles">
                  @for (role of roles(); track role) {
                    <span class="badge">{{ role }}</span>
                  }
                </div>
              </div>

              <div class="quick-actions">
                <h3>Quick Actions</h3>
                <div class="action-grid">
                  <div class="action-card" (click)="goToProblems()">
                    <div class="action-icon">📝</div>
                    <div class="action-title">Browse Problems</div>
                    <div class="action-desc">Start solving problems</div>
                  </div>
                  <div class="action-card" (click)="goToSubmissions()">
                    <div class="action-icon">📊</div>
                    <div class="action-title">My Submissions</div>
                    <div class="action-desc">View your progress</div>
                  </div>
                  @if (isAdmin()) {
                    <div class="action-card" (click)="goToAdmin()">
                      <div class="action-icon">⚙️</div>
                      <div class="action-title">Admin Panel</div>
                      <div class="action-desc">Manage problems</div>
                    </div>
                  }
                </div>
              </div>
            </div>
          } @else {
            <div class="guest-welcome">
              <h2>Welcome to OnlineJudge</h2>
              <p>Please sign in to start solving problems.</p>
              <button (click)="login()" class="btn-primary">Sign In</button>
            </div>
          }
        </div>
      </div>
    </div>
  `,
  styles: [`
    .page {
      min-height: 100vh;
      background: #ffffff;
    }

    .content {
      max-width: 1200px;
      margin: 0 auto;
      padding: 2rem;
    }

    .welcome-card {
      background: white;
      border-radius: 12px;
      padding: 2rem;
      box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
      border: 1px solid #e5e7eb;
    }

    .user-info h2 {
      margin: 0 0 0.5rem 0;
      color: #111827;
    }

    .email {
      color: #6b7280;
      margin-bottom: 1.5rem;
    }

    .roles-section {
      margin-bottom: 2rem;
    }

    .roles {
      display: flex;
      gap: 0.5rem;
      margin-top: 0.5rem;
      flex-wrap: wrap;
    }

    .badge {
      background: #e0e7ff;
      color: #4338ca;
      padding: 0.25rem 0.75rem;
      border-radius: 9999px;
      font-size: 0.875rem;
      font-weight: 500;
    }

    .quick-actions h3 {
      color: #374151;
      margin-bottom: 1rem;
    }

    .action-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 1rem;
    }

    .action-card {
      border: 1px solid #e5e7eb;
      border-radius: 8px;
      padding: 1.5rem;
      cursor: pointer;
      transition: all 0.2s;
      background: #f9fafb;
    }

    .action-card:hover {
      border-color: #4f46e5;
      background: #eef2ff;
      transform: translateY(-2px);
    }

    .action-icon {
      font-size: 2rem;
      margin-bottom: 0.5rem;
    }

    .action-title {
      font-weight: 600;
      color: #111827;
      margin-bottom: 0.25rem;
    }

    .action-desc {
      font-size: 0.875rem;
      color: #6b7280;
    }

    .guest-welcome {
      text-align: center;
      padding: 3rem 1rem;
    }

    .btn-primary {
      background: #4f46e5;
      color: white;
      border: none;
      padding: 0.75rem 1.5rem;
      border-radius: 6px;
      font-weight: 600;
      cursor: pointer;
      margin-top: 1rem;
      transition: background 0.2s;
    }

    .btn-primary:hover {
      background: #4338ca;
    }
  `]
})
export class HomeComponent {
  private authService = inject(AuthService);
  private router = inject(Router);
  private http = inject(HttpClient);

  isAuthenticated = this.authService.isAuthenticated;
  userProfile = this.authService.userProfile;
  token = this.authService.token;
  apiResult = signal<string>('');

  username = computed(() => this.userProfile()?.username || 'User');
  email = computed(() => this.userProfile()?.email || 'N/A');
  roles = computed(() => this.userProfile()?.roles || []);
  isAdmin = computed(() => {
    const roles = this.userProfile()?.roles || [];
    return roles.some(role => role.toLowerCase() === 'admin');
  });
  tokenPreview = computed(() => {
    const t = this.token();
    return t ? t.substring(0, 50) + '...' : 'N/A';
  });

  goToProblems(): void {
    this.router.navigate(['/problems']);
  }

  goToSubmissions(): void {
    this.router.navigate(['/submissions']);
  }

  goToAdmin(): void {
    this.router.navigate(['/admin/problems']);
  }

  goToLeaderboard(): void {
    this.router.navigate(['/leaderboard']);
  }

  async logout(): Promise<void> {
    await this.authService.logout();
  }

  async login(): Promise<void> {
    await this.authService.login();
  }
}
