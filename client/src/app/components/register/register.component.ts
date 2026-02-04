import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="container">
      <div class="box">
        <h1>Create Account</h1>
        <p>Register with Keycloak</p>
        
        <button (click)="register()">Sign Up</button>
        
        <div class="link">
          Already have an account?
          <a (click)="goToLogin()">Login</a>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .container {
      min-height: 100vh;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #f5f5f5;
    }

    .box {
      background: white;
      padding: 40px;
      border-radius: 8px;
      box-shadow: 0 2px 10px rgba(0,0,0,0.1);
      text-align: center;
      min-width: 300px;
    }

    h1 {
      margin: 0 0 8px 0;
      font-size: 24px;
      color: #333;
    }

    p {
      margin: 0 0 24px 0;
      color: #666;
      font-size: 14px;
    }

    button {
      width: 100%;
      padding: 12px 24px;
      border: 2px solid #333;
      background: #333;
      color: white;
      font-size: 16px;
      font-weight: 600;
      border-radius: 6px;
      cursor: pointer;
      transition: all 0.2s;
      margin-bottom: 16px;
    }

    button:hover {
      background: #222;
      border-color: #222;
    }

    button:active {
      transform: scale(0.98);
    }

    .link {
      font-size: 14px;
      color: #666;
    }

    .link a {
      color: #333;
      font-weight: 600;
      text-decoration: none;
      cursor: pointer;
      margin-left: 4px;
    }

    .link a:hover {
      text-decoration: underline;
    }
  `]
})
export class RegisterComponent {
  private authService = inject(AuthService);
  private router = inject(Router);

  register(): void {
    this.authService.register();
  }

  goToLogin(): void {
    this.router.navigate(['/login']);
  }
}
