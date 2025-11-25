import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { SubmissionApiService, SubmissionResponse } from '../../services/submission-api.service';

@Component({
  selector: 'app-submissions',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="container">
      <div class="header">
        <h1>My Submissions</h1>
        <button (click)="goBack()" class="btn-back">← Back</button>
      </div>

      @if (loading()) {
        <div class="loading">Loading submissions...</div>
      } @else if (error()) {
        <div class="error">{{ error() }}</div>
      } @else {
        <div class="submissions-table">
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Problem</th>
                <th>Language</th>
                <th>Status</th>
                <th>Result</th>
                <th>Submitted</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              @for (submission of submissions(); track submission.id) {
                <tr>
                  <td>{{ submission.id }}</td>
                  <td>
                    <a (click)="goToProblem(submission.problemId)" class="problem-link">
                      {{ formatProblemTitle(submission) }}
                    </a>
                  </td>
                  <td>{{ submission.language }}</td>
                  <td>
                    <span class="verdict" [class]="'verdict-' + getVerdictClass(submission.status)">
                      {{ submission.status }}
                    </span>
                  </td>
                  <td>{{ submission.resultMessage || '-' }}</td>
                  <td>{{ formatDate(submission.createdAt) }}</td>
                  <td>
                    <button (click)="viewCode(submission.id)" class="btn-view">View Code</button>
                  </td>
                </tr>
              } @empty {
                <tr>
                  <td colspan="7" class="empty">No submissions yet. Start solving problems!</td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      }
    </div>
  `,
  styles: [`
    .container {
      padding: 40px 20px;
      max-width: 1400px;
      margin: 0 auto;
    }

    .header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 30px;
    }

    .header h1 {
      margin: 0;
      font-size: 32px;
      color: #333;
    }

    .btn-back {
      padding: 10px 20px;
      background: white;
      border: 2px solid #333;
      border-radius: 6px;
      cursor: pointer;
      font-size: 14px;
      font-weight: 600;
      transition: all 0.2s;
    }

    .btn-back:hover {
      background: #333;
      color: white;
    }

    .loading, .error {
      text-align: center;
      padding: 40px;
      font-size: 18px;
    }

    .error {
      color: #dc3545;
    }

    .submissions-table {
      background: white;
      border-radius: 8px;
      overflow: hidden;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    }

    table {
      width: 100%;
      border-collapse: collapse;
    }

    thead {
      background: #f8f9fa;
    }

    th {
      padding: 15px;
      text-align: left;
      font-weight: 600;
      color: #333;
      border-bottom: 2px solid #e0e0e0;
    }

    td {
      padding: 15px;
      border-bottom: 1px solid #f0f0f0;
      color: #555;
    }

    tbody tr:hover {
      background: #f8f9fa;
    }

    .problem-link {
      color: #667eea;
      cursor: pointer;
      text-decoration: none;
      font-weight: 500;
    }

    .problem-link:hover {
      text-decoration: underline;
    }

    .verdict {
      padding: 4px 12px;
      border-radius: 4px;
      font-size: 13px;
      font-weight: 600;
      display: inline-block;
    }

    .verdict-accepted {
      background: #d4edda;
      color: #155724;
    }

    .verdict-wrong {
      background: #f8d7da;
      color: #721c24;
    }

    .verdict-pending {
      background: #fff3cd;
      color: #856404;
    }

    .verdict-error {
      background: #f8d7da;
      color: #721c24;
    }

    .btn-view {
      padding: 6px 16px;
      background: #667eea;
      color: white;
      border: none;
      border-radius: 4px;
      cursor: pointer;
      font-size: 13px;
      font-weight: 600;
      transition: background 0.2s;
    }

    .btn-view:hover {
      background: #5568d3;
    }

    .empty {
      text-align: center;
      padding: 40px !important;
      color: #999;
    }

    @media (max-width: 768px) {
      .submissions-table {
        overflow-x: auto;
      }

      table {
        min-width: 800px;
      }
    }
  `]
})
export class SubmissionsComponent implements OnInit {
  private submissionApi = inject(SubmissionApiService);
  private router = inject(Router);

  submissions = signal<SubmissionResponse[]>([]);
  loading = signal(false);
  error = signal('');

  ngOnInit() {
    this.loadSubmissions();
  }

  loadSubmissions() {
    this.loading.set(true);
    this.submissionApi.getMySubmissions().subscribe({
      next: (data) => {
        this.submissions.set(data || []);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(`Failed to load submissions: ${err.error?.message || err.statusText || 'Unknown error'}`);
        this.loading.set(false);
      }
    });
  }

  getVerdictClass(status: string): string {
    const v = (status || '').toLowerCase();
    if (v.includes('accept')) return 'accepted';
    if (v.includes('queue') || v.includes('pending') || v.includes('run')) return 'pending';
    if (v.includes('wrong') || v.includes('fail') || v.includes('reject') || v.includes('error')) return 'wrong';
    return 'error';
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleString();
  }

  formatProblemTitle(submission: SubmissionResponse): string {
    return submission.problemTitle || `Problem #${submission.problemId}`;
  }

  goToProblem(problemId: number) {
    this.router.navigate(['/problems', problemId]);
  }

  viewCode(submissionId: number) {
    // Navigate to submission detail page
    this.router.navigate(['/submissions', submissionId]);
  }

  goBack() {
    this.router.navigate(['/home']);
  }
}
