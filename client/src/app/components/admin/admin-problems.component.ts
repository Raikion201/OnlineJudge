import { Component, inject, signal, OnInit, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
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
  selector: 'app-admin-problems',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="container">
      <div class="header">
        <h1>Admin - Problem Management</h1>
        <button (click)="goBack()">Back to Problems</button>
      </div>

      <div class="form-section">
        <h2>{{ editingId() ? 'Edit Problem' : 'Add New Problem' }}</h2>
        <form (ngSubmit)="saveProblem()">
          <div class="form-group">
            <label>Title *</label>
            <input type="text" [(ngModel)]="form.title" name="title" required>
          </div>

          <div class="form-group">
            <label>Description *</label>
            <textarea [(ngModel)]="form.description" name="description" rows="5" required></textarea>
          </div>

          <div class="form-group">
            <label>Difficulty *</label>
            <select [(ngModel)]="form.difficulty" name="difficulty" required>
              <option value="EASY">Easy</option>
              <option value="MEDIUM">Medium</option>
              <option value="HARD">Hard</option>
            </select>
          </div>

          <div class="form-group">
            <label>Tags (comma separated)</label>
            <input type="text" [(ngModel)]="tagsInput" name="tags" placeholder="Array, String, Math">
          </div>

          @if (message()) {
            <div class="message" [class.error]="isError()">{{ message() }}</div>
          }

          <div class="form-actions">
            <button type="submit" class="btn-primary">
              {{ editingId() ? 'Update' : 'Create' }} Problem
            </button>
            @if (editingId()) {
              <button type="button" (click)="cancelEdit()">Cancel</button>
            }
          </div>
        </form>
      </div>

      <div class="list-section">
        <h2>Existing Problems</h2>
        @if (loading()) {
          <p>Loading...</p>
        } @else {
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Title</th>
                <th>Difficulty</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              @for (problem of problems(); track problem.id) {
                <tr>
                  <td>{{ problem.id }}</td>
                  <td>{{ problem.title }}</td>
                  <td>
                    <span class="difficulty" [class]="'difficulty-' + problem.difficulty.toLowerCase()">
                      {{ formatDifficulty(problem.difficulty) }}
                    </span>
                  </td>
                  <td class="actions">
                    <button (click)="editProblem(problem)" class="btn-edit">Edit</button>
                    <button (click)="deleteProblem(problem.id!)" class="btn-delete">Delete</button>
                  </td>
                </tr>
              } @empty {
                <tr>
                  <td colspan="4">No problems found.</td>
                </tr>
              }
            </tbody>
          </table>
        }
      </div>
    </div>
  `,
  styles: [`
    .container {
      padding: 40px 20px;
      max-width: 1200px;
      margin: 0 auto;
    }

    .header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 30px;
      padding-bottom: 20px;
      border-bottom: 2px solid #e0e0e0;
    }

    h1 {
      margin: 0;
      font-size: 28px;
      color: #333;
    }

    h2 {
      margin: 0 0 20px 0;
      font-size: 20px;
      color: #333;
    }

    button {
      padding: 10px 20px;
      border: 2px solid #333;
      background: white;
      color: #333;
      font-size: 14px;
      font-weight: 600;
      border-radius: 6px;
      cursor: pointer;
      transition: all 0.2s;
    }

    button:hover {
      background: #333;
      color: white;
    }

    .form-section {
      background: white;
      padding: 30px;
      border-radius: 8px;
      border: 1px solid #e0e0e0;
      margin-bottom: 30px;
    }

    .form-group {
      margin-bottom: 20px;
    }

    label {
      display: block;
      margin-bottom: 8px;
      font-weight: 600;
      color: #333;
    }

    input, textarea, select {
      width: 100%;
      padding: 10px;
      border: 1px solid #ddd;
      border-radius: 6px;
      font-size: 14px;
      font-family: inherit;
    }

    input:focus, textarea:focus, select:focus {
      outline: none;
      border-color: #333;
    }

    .form-actions {
      display: flex;
      gap: 10px;
    }

    .btn-primary {
      background: #333;
      color: white;
      border-color: #333;
    }

    .btn-primary:hover {
      background: #222;
    }

    .message {
      padding: 12px;
      border-radius: 6px;
      margin-bottom: 15px;
      background: #d4edda;
      color: #155724;
    }

    .message.error {
      background: #f8d7da;
      color: #721c24;
    }

    .list-section {
      background: white;
      padding: 30px;
      border-radius: 8px;
      border: 1px solid #e0e0e0;
    }

    table {
      width: 100%;
      border-collapse: collapse;
    }

    th, td {
      padding: 12px;
      text-align: left;
      border-bottom: 1px solid #e0e0e0;
    }

    th {
      font-weight: 600;
      color: #333;
      background: #f5f5f5;
    }

    .actions {
      display: flex;
      gap: 8px;
    }

    .btn-edit, .btn-delete {
      padding: 6px 12px;
      font-size: 12px;
    }

    .btn-edit:hover {
      background: #007bff;
      border-color: #007bff;
      color: white;
    }

    .btn-delete:hover {
      background: #dc3545;
      border-color: #dc3545;
      color: white;
    }

    .difficulty {
      padding: 4px 12px;
      border-radius: 4px;
      font-size: 12px;
      font-weight: 600;
    }

    .difficulty-easy {
      background: #d4edda;
      color: #155724;
    }

    .difficulty-medium {
      background: #fff3cd;
      color: #856404;
    }

    .difficulty-hard {
      background: #f8d7da;
      color: #721c24;
    }
  `]
})
export class AdminProblemsComponent implements OnInit {
  private http = inject(HttpClient);
  private router = inject(Router);
  private authService = inject(AuthService);

  problems = signal<Problem[]>([]);
  loading = signal(false);
  message = signal('');
  isError = signal(false);
  editingId = signal<number | null>(null);
  hasCheckedAuth = signal(false);

  form: Problem = {
    title: '',
    description: '',
    difficulty: 'EASY'
  };
  tagsInput = '';

  constructor() {
    // Watch for auth state changes
    effect(() => {
      const profile = this.authService.userProfile();
      const isAuthenticated = this.authService.isAuthenticated();
      
      console.log('Effect run - authenticated:', isAuthenticated, 'profile:', profile, 'hasChecked:', this.hasCheckedAuth());
      
      // Only run once
      if (this.hasCheckedAuth()) return;
      
      // Wait until auth has completed initialization (either true or false, not just initial false)
      if (isAuthenticated && profile) {
        this.hasCheckedAuth.set(true);
        
        // Check for Admin role (case-insensitive)
        const hasAdminRole = profile.roles?.some(role => 
          role.toLowerCase() === 'admin'
        );
        
        console.log('User roles:', profile.roles, 'hasAdminRole:', hasAdminRole);
        
        if (hasAdminRole) {
          console.log('Admin role confirmed, loading problems...');
          this.loadProblems();
        } else {
          console.log('No admin role found');
          this.showMessage('Access denied: Admin role required', true);
          setTimeout(() => this.router.navigate(['/problems']), 2000);
        }
      }
      // If clearly not authenticated (not just initializing), redirect
      else if (isAuthenticated === false && !this.authService.token()) {
        // Check if we've waited long enough (auth service should have tried to init)
        setTimeout(() => {
          if (!this.authService.isAuthenticated() && !this.hasCheckedAuth()) {
            this.hasCheckedAuth.set(true);
            console.log('Not authenticated after timeout, redirecting to login');
            this.router.navigate(['/login']);
          }
        }, 1000);
      }
    });
  }

  ngOnInit() {
    // Effect will handle the logic
  }

  loadProblems() {
    this.loading.set(true);
    this.http.get<any>('http://localhost:8085/api/v1/problems').subscribe({
      next: (data) => {
        console.log('API Response:', data);
        // Extract content array from Page response
        const problemsArray = data.content || [];
        this.problems.set(problemsArray);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Load error:', err);
        this.showMessage(`Failed to load: ${err.statusText}`, true);
        this.loading.set(false);
      }
    });
  }

  saveProblem() {
    const problem: any = {
      title: this.form.title,
      description: this.form.description,
      difficulty: this.form.difficulty.toUpperCase(),
      timeLimit: 1000,
      memoryLimit: 256,
      isPublic: false,
      testCases: [],
      examples: [],
      tagIds: []
    };

    if (this.editingId()) {
      // Update
      this.http.put(`http://localhost:8085/api/v1/problems/${this.editingId()}`, problem).subscribe({
        next: () => {
          this.showMessage('Problem updated successfully!', false);
          this.loadProblems();
          this.resetForm();
        },
        error: (err) => {
          console.error('Update error:', err);
          this.showMessage(`Update failed: ${err.error?.message || err.statusText}`, true);
        }
      });
    } else {
      // Create
      this.http.post('http://localhost:8085/api/v1/problems', problem).subscribe({
        next: () => {
          this.showMessage('Problem created successfully!', false);
          this.loadProblems();
          this.resetForm();
        },
        error: (err) => {
          console.error('Create error:', err);
          this.showMessage(`Create failed: ${err.error?.message || err.statusText}`, true);
        }
      });
    }
  }

  editProblem(problem: Problem) {
    this.editingId.set(problem.id || null);
    this.form = { ...problem };
    this.tagsInput = problem.tags?.join(', ') || '';
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  deleteProblem(id: number) {
    if (!confirm('Are you sure you want to delete this problem?')) return;

    this.http.delete(`http://localhost:8085/api/v1/problems/${id}`).subscribe({
      next: () => {
        this.showMessage('Problem deleted successfully!', false);
        this.loadProblems();
      },
      error: (err) => this.showMessage(`Delete failed: ${err.statusText}`, true)
    });
  }

  cancelEdit() {
    this.resetForm();
  }

  resetForm() {
    this.form = {
      title: '',
      description: '',
      difficulty: 'EASY'
    };
    this.tagsInput = '';
    this.editingId.set(null);
  }

  showMessage(msg: string, error: boolean) {
    this.message.set(msg);
    this.isError.set(error);
    setTimeout(() => this.message.set(''), 5000);
  }

  formatDifficulty(diff: string): string {
    return diff.charAt(0) + diff.slice(1).toLowerCase();
  }

  goBack() {
    this.router.navigate(['/problems']);
  }
}
