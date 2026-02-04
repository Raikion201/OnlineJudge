import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="container">
      <div class="box">
        <h1>Online Judge</h1>
        <p>Keycloak Authentication Test</p>
        
        <div class="buttons">
          <button (click)="login()">Login</button>
          <button (click)="register()">Register</button>
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

    .buttons {
      display: flex;
      flex-direction: column;
      gap: 12px;
    }

    button {
      padding: 12px 24px;
      border: 2px solid #333;
      background: white;
      color: #333;
      font-size: 16px;
      font-weight: 600;
      border-radius: 6px;
      cursor: pointer;
      transition: all 0.2s;
    }

    button:hover {
      background: #333;
      color: white;
    }

    button:active {
      transform: scale(0.98);
    }
  `]
})
export class LoginComponent {
  private authService = inject(AuthService);
  private router = inject(Router);

  login(): void {
    this.authService.login();
  }

  register(): void {
    this.router.navigate(['/register']);
  }
}
