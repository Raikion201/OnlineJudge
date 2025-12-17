import { Component, inject, signal, OnInit, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';

interface Problem {
  id?: number;
  status?: 'solved' | 'attempted' | 'unsolved';
  title: string;
  description: string;
  difficulty: string;
  category?: string;
  acceptance?: number;
  likes?: number;
  submissions?: number;
  tags?: string[];
  // LeetCode-style function metadata
  functionName?: string;
  functionSignature?: string;
  codeTemplate?: string;
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
  imports: [CommonModule, RouterLink, FormsModule],
  template: `
    <div class="problems-page">
      <div class="problems-container">
        <!-- Header -->
        <div class="problems-header">
          <h1>Problems</h1>
          <p>Solve problems, master algorithms, prepare for interviews</p>
        </div>

        <!-- Filters & Search -->
        <div class="filters-card">
          <div class="filters-grid">
            <!-- Search -->
            <div class="filter-group filter-search">
              <label>Search</label>
              <input
                [(ngModel)]="searchQuery"
                type="text"
                placeholder="Search problems..."
                class="filter-input"
              />
            </div>

            <!-- Difficulty Filter -->
            <div class="filter-group">
              <label>Difficulty</label>
              <select [(ngModel)]="selectedDifficulty" class="filter-select">
                <option value="">All Levels</option>
                <option value="Easy">Easy</option>
                <option value="Medium">Medium</option>
                <option value="Hard">Hard</option>
              </select>
            </div>

            <!-- Status Filter -->
            <div class="filter-group">
              <label>Status</label>
              <select [(ngModel)]="selectedStatus" class="filter-select">
                <option value="">All Status</option>
                <option value="solved">Solved</option>
                <option value="attempted">Attempted</option>
                <option value="unsolved">Unsolved</option>
              </select>
            </div>
          </div>
        </div>

        <!-- Problems Table -->
        <div class="problems-table-container">
          <table class="problems-table">
            <thead>
              <tr>
                <th class="col-status">Status</th>
                <th class="col-title">Title</th>
                <th class="col-difficulty">Difficulty</th>
                <th class="col-category">Category</th>
                <th class="col-acceptance">Acceptance</th>
              </tr>
            </thead>
            <tbody>
              @if (loading()) {
                <tr>
                  <td colspan="5" class="loading-cell">Loading...</td>
                </tr>
              } @else if (error()) {
                <tr>
                  <td colspan="5" class="error-cell">{{ error() }}</td>
                </tr>
              } @else {
                @for (problem of filteredProblems(); track problem.id) {
                  <tr [routerLink]="['/problems', problem.id]">
                    <!-- Status -->
                    <td class="col-status">
                      @if (problem.status === 'solved') {
                        <span class="status-icon status-solved">
                          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="M5 13l4 4L19 7"></path>
                          </svg>
                        </span>
                      } @else if (problem.status === 'attempted') {
                        <span class="status-icon status-attempted">
                          <svg viewBox="0 0 24 24" fill="currentColor">
                            <circle cx="12" cy="12" r="4"></circle>
                          </svg>
                        </span>
                      } @else {
                        <span class="status-icon status-unsolved"></span>
                      }
                    </td>

                    <!-- Title -->
                    <td class="col-title">{{ problem.title }}</td>

                    <!-- Difficulty -->
                    <td class="col-difficulty">
                      <span class="difficulty-badge" [attr.data-difficulty]="problem.difficulty.toLowerCase()">
                        {{ formatDifficulty(problem.difficulty) }}
                      </span>
                    </td>

                    <!-- Category -->
                    <td class="col-category">
                      {{ problem.category || (problem.tags && problem.tags[0]) || 'Algorithms' }}
                    </td>

                    <!-- Acceptance -->
                    <td class="col-acceptance">
                      <div class="acceptance-wrapper">
                        <div class="acceptance-bar">
                          <div class="acceptance-fill" [style.width.%]="problem.acceptance || 0"></div>
                        </div>
                        <span class="acceptance-text">{{ (problem.acceptance || 0).toFixed(1) }}%</span>
                      </div>
                    </td>
                  </tr>
                }
              }
            </tbody>
          </table>

          <!-- Empty State -->
          @if (!loading() && !error() && filteredProblems().length === 0) {
            <div class="empty-state">
              <p>No problems found matching your filters.</p>
            </div>
          }
        </div>

        <!-- Pagination -->
        <div class="pagination">
          <p class="pagination-info">
            Showing {{ filteredProblems().length }} of {{ problems().length }} problems
          </p>
          <div class="pagination-buttons">
            <button class="btn-prev">Previous</button>
            <button class="btn-next">Next</button>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .problems-page {
      background-color: var(--slate-50);
      min-height: 100vh;
      padding: 32px 0;
    }

    .problems-container {
      max-width: 1200px;
      margin: 0 auto;
      padding: 0 24px;
    }

    /* Header */
    .problems-header {
      margin-bottom: 32px;
    }

    .problems-header h1 {
      font-size: 28px;
      font-weight: 700;
      color: var(--slate-900);
      margin: 0 0 8px 0;
    }

    .problems-header p {
      font-size: 16px;
      color: var(--slate-500);
      margin: 0;
    }

    /* Filters */
    .filters-card {
      background: white;
      border-radius: 12px;
      border: 1px solid var(--slate-200);
      padding: 24px;
      margin-bottom: 24px;
    }

    .filters-grid {
      display: grid;
      grid-template-columns: 2fr 1fr 1fr;
      gap: 16px;
    }

    .filter-group {
      display: flex;
      flex-direction: column;
    }

    .filter-group label {
      font-size: 13px;
      font-weight: 500;
      color: var(--slate-700);
      margin-bottom: 8px;
    }

    .filter-input,
    .filter-select {
      width: 100%;
      padding: 10px 14px;
      font-size: 14px;
      font-family: inherit;
      background-color: white;
      border: 1px solid var(--slate-300);
      border-radius: 8px;
      color: var(--slate-900);
      transition: border-color 0.15s, box-shadow 0.15s;
    }

    .filter-select {
      background-color: white;
      color: var(--slate-900);
      cursor: pointer;
      appearance: none;
      background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='16' height='16' viewBox='0 0 24 24' fill='none' stroke='%23475569' stroke-width='2'%3E%3Cpath d='M6 9l6 6 6-6'/%3E%3C/svg%3E");
      background-repeat: no-repeat;
      background-position: right 12px center;
      padding-right: 40px;
    }

    .filter-select option {
      background-color: white;
      color: var(--slate-900);
      padding: 10px;
    }

    .filter-input::placeholder {
      color: var(--slate-400);
    }

    .filter-input:focus,
    .filter-select:focus {
      outline: none;
      border-color: var(--primary-500);
      box-shadow: 0 0 0 3px var(--primary-100);
    }

    /* Table */
    .problems-table-container {
      background: white;
      border-radius: 12px;
      border: 1px solid var(--slate-200);
      overflow: hidden;
    }

    .problems-table {
      width: 100%;
      border-collapse: collapse;
    }

    .problems-table thead {
      background-color: var(--slate-50);
      border-bottom: 1px solid var(--slate-200);
    }

    .problems-table th {
      padding: 16px 20px;
      text-align: left;
      font-size: 13px;
      font-weight: 600;
      color: var(--slate-600);
    }

    .problems-table tbody tr {
      border-bottom: 1px solid var(--slate-100);
      cursor: pointer;
      transition: background-color 0.15s;
    }

    .problems-table tbody tr:hover {
      background-color: var(--slate-50);
    }

    .problems-table tbody tr:last-child {
      border-bottom: none;
    }

    .problems-table td {
      padding: 16px 20px;
      font-size: 14px;
      color: var(--slate-900);
    }

    /* Column widths */
    .col-status { width: 80px; }
    .col-title { width: auto; }
    .col-difficulty { width: 100px; }
    .col-category { width: 160px; color: var(--slate-600); }
    .col-acceptance { width: 150px; }

    /* Status Icons */
    .status-icon {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 24px;
      height: 24px;
      border-radius: 50%;
    }

    .status-icon svg {
      width: 14px;
      height: 14px;
    }

    .status-solved {
      background-color: var(--success-500);
      color: white;
    }

    .status-attempted {
      background-color: var(--warning-500);
      color: white;
    }

    .status-unsolved {
      border: 2px solid var(--slate-300);
      background: transparent;
    }

    /* Difficulty Badge */
    .difficulty-badge {
      display: inline-block;
      padding: 4px 10px;
      font-size: 12px;
      font-weight: 600;
      border-radius: 4px;
    }

    .difficulty-badge[data-difficulty="easy"] {
      background-color: var(--success-100);
      color: var(--success-700);
    }

    .difficulty-badge[data-difficulty="medium"] {
      background-color: var(--warning-100);
      color: var(--warning-700);
    }

    .difficulty-badge[data-difficulty="hard"] {
      background-color: var(--danger-100);
      color: var(--danger-700);
    }

    /* Acceptance */
    .acceptance-wrapper {
      display: flex;
      align-items: center;
      justify-content: flex-end;
      gap: 12px;
    }

    .acceptance-bar {
      width: 80px;
      height: 6px;
      background-color: var(--slate-200);
      border-radius: 3px;
      overflow: hidden;
    }

    .acceptance-fill {
      height: 100%;
      background: linear-gradient(90deg, var(--primary-400), var(--accent-500));
      border-radius: 3px;
    }

    .acceptance-text {
      font-size: 13px;
      font-weight: 500;
      color: var(--slate-700);
      min-width: 45px;
      text-align: right;
    }

    /* Loading & Error states */
    .loading-cell,
    .error-cell {
      text-align: center;
      padding: 48px 24px !important;
      color: var(--slate-500);
    }

    .empty-state {
      text-align: center;
      padding: 64px 24px;
      color: var(--slate-500);
      font-size: 16px;
    }

    /* Pagination */
    .pagination {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-top: 24px;
    }

    .pagination-info {
      font-size: 14px;
      color: var(--slate-500);
      margin: 0;
    }

    .pagination-buttons {
      display: flex;
      gap: 8px;
    }

    .btn-prev,
    .btn-next {
      padding: 10px 20px;
      font-size: 14px;
      font-weight: 500;
      font-family: inherit;
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.15s;
    }

    .btn-prev {
      background-color: white;
      color: var(--slate-700);
      border: 1px solid var(--slate-300);
    }

    .btn-prev:hover {
      background-color: var(--slate-50);
    }

    .btn-next {
      background-color: var(--primary-600);
      color: white;
      border: none;
    }

    .btn-next:hover {
      background-color: var(--primary-700);
    }

    /* Responsive */
    @media (max-width: 768px) {
      .filters-grid {
        grid-template-columns: 1fr;
      }

      .problems-table th,
      .problems-table td {
        padding: 12px 16px;
      }

      .col-category {
        display: none;
      }
    }
  `],
})
export class ProblemsComponent implements OnInit {
  private router = inject(Router);
  private http = inject(HttpClient);
  private authService = inject(AuthService);

  problems = signal<Problem[]>([]);
  loading = signal<boolean>(true);
  error = signal<string | null>(null);
  searchQuery = signal<string>('');
  selectedDifficulty = signal<string>('');
  selectedStatus = signal<string>('');

  filteredProblems = computed(() => {
    return this.problems().filter((problem) => {
      const matchesSearch =
        problem.title.toLowerCase().includes(this.searchQuery().toLowerCase()) ||
        (problem.category && problem.category.toLowerCase().includes(this.searchQuery().toLowerCase())) ||
        (problem.tags && problem.tags.some(tag => tag.toLowerCase().includes(this.searchQuery().toLowerCase())));
      const matchesDifficulty =
        !this.selectedDifficulty() ||
        problem.difficulty === this.selectedDifficulty();
      const matchesStatus =
        !this.selectedStatus() || problem.status === this.selectedStatus();

      return matchesSearch && matchesDifficulty && matchesStatus;
    });
  });

  ngOnInit() {
    this.loadProblems();
  }

  loadProblems() {
    this.loading.set(true);
    this.http.get<Page<Problem>>('http://localhost:8087/api/v1/problems')
      .subscribe({
        next: (data) => {
          this.problems.set(data.content.map(p => ({
            ...p,
            status: 'unsolved' as const,
            acceptance: Math.random() * 100
          })));
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

  getDifficultyClass(difficulty: string): string {
    const classes: Record<string, string> = {
      Easy: 'badge-success',
      Medium: 'badge-warning',
      Hard: 'badge-danger',
    };
    return classes[difficulty] || 'badge-primary';
  }
}
