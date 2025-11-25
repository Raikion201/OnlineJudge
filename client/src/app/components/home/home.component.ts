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
      <nav class="navbar">
        <div class="nav-container">
          <div class="logo">
            <h1>OnlineJudge</h1>
          </div>
          <div class="nav-links">
            <button (click)="goToProblems()" class="nav-btn">Problems</button>
            <button (click)="goToSubmissions()" class="nav-btn">My Submissions</button>
            @if (isAdmin()) {
              <button (click)="goToAdmin()" class="nav-btn">Admin Panel</button>
            }
            <button (click)="logout()" class="btn-logout">Logout</button>
          </div>
        </div>
      </nav>

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
            <p>Loading...</p>
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

    .btn-logout {
      background: white;
      border: 2px solid white;
      color: #000;
      padding: 0.5rem 1rem;
      cursor: pointer;
      font-size: 14px;
      font-weight: 600;
      border-radius: 6px;
      transition: all 0.2s;
    }

    .btn-logout:hover {
      background: #f5f5f5;
    }

    .content {
      max-width: 1200px;
      margin: 0 auto;
      padding: 3rem 2rem;
    }

    .welcome-card {
      background: white;
      padding: 3rem;
      border-radius: 12px;
      box-shadow: 0 4px 12px rgba(0,0,0,0.1);
    }

    .user-info h2 {
      margin: 0 0 0.5rem 0;
      font-size: 2rem;
      color: #000;
    }

    .email {
      color: #666;
      margin-bottom: 2rem;
    }

    .roles-section {
      margin: 2rem 0;
      padding: 1.5rem;
      background: #f5f5f5;
      border-radius: 8px;
    }

    .roles-section strong {
      display: block;
      margin-bottom: 1rem;
      color: #000;
      font-size: 1.1rem;
    }

    .roles {
      display: flex;
      gap: 0.5rem;
      flex-wrap: wrap;
    }

    .badge {
      background: #000;
      color: white;
      padding: 0.4rem 1rem;
      border-radius: 20px;
      font-size: 14px;
      font-weight: 600;
    }

    .quick-actions {
      margin-top: 2rem;
    }

    .quick-actions h3 {
      margin: 0 0 1.5rem 0;
      color: #000;
      font-size: 1.5rem;
    }

    .action-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 1.5rem;
    }

    .action-card {
      background: white;
      padding: 2rem;
      border: 2px solid #e0e0e0;
      border-radius: 12px;
      text-align: center;
      cursor: pointer;
      transition: all 0.3s;
    }

    .action-card:hover {
      border-color: #000;
      box-shadow: 0 4px 12px rgba(0,0,0,0.15);
      transform: translateY(-4px);
    }

    .action-icon {
      font-size: 3rem;
      margin-bottom: 1rem;
    }

    .action-title {
      font-size: 1.1rem;
      font-weight: 700;
      color: #000;
      margin-bottom: 0.5rem;
    }

    .action-desc {
      color: #666;
      font-size: 14px;
    }

    .badge {
      display: inline-block;
      padding: 4px 12px;
      background: #333;
      color: white;
      border-radius: 4px;
      font-size: 14px;
      margin-right: 8px;
    }

    .token {
      font-family: monospace;
      font-size: 12px;
      color: #666;
      word-break: break-all;
      padding: 10px;
      background: white;
      border: 1px solid #e0e0e0;
      border-radius: 4px;
      margin-top: 8px;
    }

    .api-buttons {
      display: flex;
      gap: 10px;
      margin-top: 10px;
    }

    .api-buttons button {
      flex: 1;
      padding: 10px;
      font-size: 14px;
    }

    .api-result {
      margin-top: 10px;
      padding: 10px;
      background: white;
      border: 1px solid #e0e0e0;
      border-radius: 4px;
      font-family: monospace;
      font-size: 12px;
      color: #333;
      max-height: 200px;
      overflow-y: auto;
      white-space: pre-wrap;
    }

    .actions {
      display: flex;
      gap: 10px;
    }

    .actions button {
      margin: 0;
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

  async logout(): Promise<void> {
    await this.authService.logout();
  }
}
