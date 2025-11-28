import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="landing">
      <section class="hero">
        <div class="container">
          <h1 class="hero-title">Master Your Coding Skills</h1>
          <p class="hero-subtitle">Practice algorithmic problems, compete with others, and track your progress</p>
          <div class="hero-actions">
            <button (click)="goToProblems()" class="btn-large btn-primary">Start Practicing</button>
            <button (click)="register()" class="btn-large btn-secondary">Create Account</button>
          </div>
        </div>
      </section>

      <section class="features">
        <div class="container">
          <h2>Why Choose OnlineJudge?</h2>
          <div class="feature-grid">
            <div class="feature-card">
              <div class="feature-icon">📝</div>
              <h3>Rich Problem Set</h3>
              <p>Hundreds of problems across all difficulty levels and topics</p>
            </div>
            <div class="feature-card">
              <div class="feature-icon">⚡</div>
              <h3>Instant Feedback</h3>
              <p>Get immediate results on your submissions with detailed test cases</p>
            </div>
            <div class="feature-card">
              <div class="feature-icon">🏆</div>
              <h3>Compete & Learn</h3>
              <p>Challenge yourself with contests and climb the leaderboard</p>
            </div>
            <div class="feature-card">
              <div class="feature-icon">📊</div>
              <h3>Track Progress</h3>
              <p>Monitor your improvement with detailed statistics and analytics</p>
            </div>
          </div>
        </div>
      </section>

      <section class="stats">
        <div class="container">
          <div class="stats-grid">
            <div class="stat-item">
              <div class="stat-number">1000+</div>
              <div class="stat-label">Problems</div>
            </div>
            <div class="stat-item">
              <div class="stat-number">10k+</div>
              <div class="stat-label">Users</div>
            </div>
            <div class="stat-item">
              <div class="stat-number">100k+</div>
              <div class="stat-label">Submissions</div>
            </div>
          </div>
        </div>
      </section>

      <section class="cta">
        <div class="container">
          <h2>Ready to Start?</h2>
          <p>Join thousands of developers improving their skills</p>
          <button (click)="register()" class="btn-large btn-primary">Get Started Free</button>
        </div>
      </section>

      <footer class="footer">
        <div class="container">
          <p>&copy; 2025 OnlineJudge. All rights reserved.</p>
        </div>
      </footer>
    </div>
  `,
  styles: [`
    .landing {
      min-height: 100vh;
      background: #ffffff;
    }

    .hero {
      padding: 8rem 2rem;
      text-align: center;
      color: #000;
      background: linear-gradient(180deg, #ffffff 0%, #f5f5f5 100%);
    }

    .hero-title {
      font-size: 4rem;
      font-weight: 800;
      margin: 0 0 1rem 0;
      color: #000;
    }

    .hero-subtitle {
      font-size: 1.5rem;
      margin: 0 0 3rem 0;
      color: #333;
    }

    .hero-actions {
      display: flex;
      gap: 1rem;
      justify-content: center;
      flex-wrap: wrap;
    }

    .btn-large {
      padding: 1rem 2.5rem;
      font-size: 18px;
      font-weight: 600;
      border-radius: 8px;
      border: 2px solid #000;
      cursor: pointer;
      transition: all 0.3s;
    }

    .btn-large:hover {
      transform: translateY(-2px);
      box-shadow: 0 8px 16px rgba(0,0,0,0.15);
    }

    .btn-primary {
      background: #000;
      color: white;
    }

    .btn-primary:hover {
      background: #333;
      border-color: #333;
    }

    .btn-secondary {
      background: white;
      color: #000;
    }

    .btn-secondary:hover {
      background: #f5f5f5;
    }

    .features {
      background: white;
      padding: 6rem 2rem;
    }

    .features h2 {
      text-align: center;
      font-size: 2.5rem;
      margin: 0 0 4rem 0;
      color: #000;
    }

    .feature-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
      gap: 2rem;
      max-width: 1200px;
      margin: 0 auto;
    }

    .feature-card {
      text-align: center;
      padding: 2rem;
      border: 1px solid #e0e0e0;
      border-radius: 8px;
      transition: all 0.3s;
    }

    .feature-card:hover {
      box-shadow: 0 4px 12px rgba(0,0,0,0.1);
      transform: translateY(-4px);
    }

    .feature-icon {
      font-size: 3rem;
      margin-bottom: 1rem;
    }

    .feature-card h3 {
      font-size: 1.5rem;
      margin: 0 0 1rem 0;
      color: #000;
    }

    .feature-card p {
      color: #666;
      line-height: 1.6;
    }

    .stats {
      background: #000;
      padding: 4rem 2rem;
    }

    .stats-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 2rem;
      max-width: 1200px;
      margin: 0 auto;
      text-align: center;
    }

    .stat-number {
      font-size: 3rem;
      font-weight: 800;
      color: white;
      margin-bottom: 0.5rem;
    }

    .stat-label {
      font-size: 1.2rem;
      color: #ccc;
    }

    .cta {
      background: #f5f5f5;
      padding: 6rem 2rem;
      text-align: center;
      color: #000;
    }

    .cta h2 {
      font-size: 2.5rem;
      margin: 0 0 1rem 0;
      color: #000;
    }

    .cta p {
      font-size: 1.2rem;
      margin: 0 0 2rem 0;
      color: #333;
    }

    .footer {
      background: #000;
      padding: 2rem;
      text-align: center;
      color: white;
    }

    .container {
      max-width: 1200px;
      margin: 0 auto;
      padding: 0 2rem;
    }

    @media (max-width: 768px) {
      .hero-title {
        font-size: 2.5rem;
      }

      .hero-subtitle {
        font-size: 1.2rem;
      }

      .hero-actions {
        flex-direction: column;
      }

      .btn-large {
        width: 100%;
      }
    }
  `]
})
export class LandingComponent {
  private router = inject(Router);
  private authService = inject(AuthService);

  isAuthenticated = this.authService.isAuthenticated;

  async login() {
    await this.authService.login();
  }

  async register() {
    await this.authService.register();
  }

  goToProblems() {
    this.router.navigate(['/problems']);
  }

  goToHome() {
    this.router.navigate(['/home']);
  }
}
