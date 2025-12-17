import { Component, inject, signal, OnInit, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { SubmissionApiService, SubmissionResponse } from '../../services/submission-api.service';

@Component({
  selector: 'app-submissions',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="bg-slate-50 min-h-screen py-8">
      <div class="container-max">
        <!-- Header -->
        <div class="mb-8">
          <h1 class="text-4xl font-bold text-slate-900 mb-2">My Submissions</h1>
          <p class="text-lg text-slate-600">
            Track all your submissions and their results
          </p>
        </div>

        <!-- Filters & Search -->
        <div class="bg-white rounded-lg border border-slate-200 p-6 mb-8">
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-4">
            <!-- Search -->
            <div class="lg:col-span-2">
              <label class="block text-sm font-medium text-slate-700 mb-2"
                >Problem</label
              >
              <input
                [(ngModel)]="searchQuery"
                type="text"
                placeholder="Search problems..."
                class="input-field"
              />
            </div>

            <!-- Status Filter -->
            <div>
              <label class="block text-sm font-medium text-slate-700 mb-2"
                >Status</label
              >
              <select [(ngModel)]="selectedStatus" class="input-field">
                <option value="">All Status</option>
                <option value="ACCEPTED">Accepted</option>
                <option value="WRONG_ANSWER">Wrong Answer</option>
                <option value="TIME_LIMIT_EXCEEDED">Time Limit Exceeded</option>
                <option value="RUNTIME_ERROR">Runtime Error</option>
                <option value="COMPILATION_ERROR">Compilation Error</option>
                <option value="MEMORY_LIMIT_EXCEEDED">Memory Limit Exceeded</option>
              </select>
            </div>

            <!-- Language Filter -->
            <div>
              <label class="block text-sm font-medium text-slate-700 mb-2"
                >Language</label
              >
              <select [(ngModel)]="selectedLanguage" class="input-field">
                <option value="">All Languages</option>
                <option value="JAVA">Java</option>
                <option value="PYTHON">Python</option>
                <option value="CPP">C++</option>
                <option value="C">C</option>
                <option value="JAVASCRIPT">JavaScript</option>
              </select>
            </div>

            <!-- Sort -->
            <div>
              <label class="block text-sm font-medium text-slate-700 mb-2"
                >Sort</label
              >
              <select [(ngModel)]="sortBy" class="input-field">
                <option value="recent">Most Recent</option>
                <option value="oldest">Oldest</option>
              </select>
            </div>
          </div>
        </div>

        <!-- Stats -->
        <div class="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
          <div class="card p-6 text-center">
            <div class="text-3xl font-bold text-success-600">
              {{ acceptedCount() }}
            </div>
            <p class="text-slate-600 mt-2">Accepted</p>
          </div>
          <div class="card p-6 text-center">
            <div class="text-3xl font-bold text-danger-600">
              {{ wrongAnswerCount() }}
            </div>
            <p class="text-slate-600 mt-2">Wrong Answer</p>
          </div>
          <div class="card p-6 text-center">
            <div class="text-3xl font-bold text-warning-600">
              {{ tleCount() }}
            </div>
            <p class="text-slate-600 mt-2">Time Limit Exceeded</p>
          </div>
          <div class="card p-6 text-center">
            <div class="text-3xl font-bold text-slate-600">
              {{ filteredSubmissions().length }}
            </div>
            <p class="text-slate-600 mt-2">Total Submissions</p>
          </div>
        </div>

        <!-- Submissions List -->
        <div class="space-y-4">
          @if (loading()) {
            <div class="card p-12 text-center">
              <p class="text-slate-600 text-lg">Loading submissions...</p>
            </div>
          } @else if (error()) {
            <div class="card p-12 text-center">
              <p class="text-danger-600 text-lg">{{ error() }}</p>
            </div>
          } @else if (filteredSubmissions().length === 0) {
            <div class="card p-12 text-center">
              <p class="text-slate-600 text-lg mb-4">
                No submissions found matching your filters.
              </p>
            </div>
          } @else {
            @for (submission of filteredSubmissions(); track submission.id) {
              <div
                class="card p-6 hover:shadow-lg transition-all cursor-pointer"
              >
                <div class="grid grid-cols-1 lg:grid-cols-12 gap-6">
                  <!-- Left: Status & Problem -->
                  <div class="lg:col-span-5">
                    <div class="flex items-start gap-4">
                      <div class="flex-shrink-0 pt-1">
                        <div
                          [ngClass]="getStatusIconClass(submission.status)"
                          class="w-10 h-10 rounded-full flex items-center justify-center text-white font-bold"
                        >
                          <span *ngIf="submission.status === 'ACCEPTED'">✓</span>
                          <span *ngIf="submission.status !== 'ACCEPTED'">✗</span>
                        </div>
                      </div>
                      <div>
                        <h3 class="text-lg font-semibold text-slate-900">
                          {{ formatProblemTitle(submission) }}
                        </h3>
                        <p class="text-sm text-slate-600 mt-1">
                          <span
                            [ngClass]="getStatusBadgeClass(submission.status)"
                            class="badge text-xs font-bold px-2.5 py-0.5"
                          >
                            {{ submission.status }}
                          </span>
                          <span class="ml-3 text-slate-500">{{
                            formatDate(submission.createdAt)
                          }}</span>
                        </p>
                      </div>
                    </div>
                  </div>

                  <!-- Middle: Code & Stats -->
                  <div class="lg:col-span-4">
                    <div class="flex items-center gap-4 text-sm">
                      <div class="flex items-center gap-1">
                        <span class="text-slate-500">Language:</span>
                        <span class="font-semibold text-slate-900">{{
                          submission.language
                        }}</span>
                      </div>
                    </div>
                  </div>

                  <!-- Right: Performance Metrics -->
                  <div class="lg:col-span-3">
                    <div class="space-y-2">
                      <div
                        *ngIf="submission.executionTime"
                        class="flex justify-between"
                      >
                        <span class="text-slate-600">Runtime:</span>
                        <span class="font-semibold text-slate-900"
                          >{{ submission.executionTime }}ms</span
                        >
                      </div>
                      <div
                        class="flex justify-between pt-2 border-t border-slate-200"
                      >
                        <span class="text-slate-600">ID:</span>
                        <span class="font-mono text-sm text-slate-500"
                          >#{{ submission.id }}</span
                        >
                      </div>
                    </div>
                  </div>
                </div>

                <!-- Code Preview (Expanded) -->
                <div class="mt-4 text-right">
                  <button
                    (click)="toggleCodeVisibility(submission.id)"
                    class="btn-secondary"
                  >
                    {{ codeVisible[submission.id] ? 'Hide Code' : 'View Code' }}
                  </button>
                </div>
                <div
                  *ngIf="codeVisible[submission.id]"
                  class="mt-4 bg-slate-900 rounded-lg p-4 max-h-60 overflow-y-auto"
                >
                  <pre
                    class="text-slate-300 font-mono text-xs"
                    [textContent]="submission.code"
                  ></pre>
                </div>
              </div>
            }
          }
        </div>

        <!-- Pagination -->
        <div class="flex items-center justify-between mt-8">
          <p class="text-slate-600">
            Showing {{ filteredSubmissions().length }} of
            {{ allSubmissions().length }} submissions
          </p>
          <div class="flex gap-2">
            <button class="btn-secondary">Previous</button>
            <button class="btn-primary">Next</button>
          </div>
        </div>
      </div>
    </div>
  `,
})
export class SubmissionsComponent implements OnInit {
  private submissionApi = inject(SubmissionApiService);
  private router = inject(Router);

  allSubmissions = signal<SubmissionResponse[]>([]);
  loading = signal(false);
  error = signal<string>('');
  searchQuery = signal<string>('');
  selectedStatus = signal<string>('');
  selectedLanguage = signal<string>('');
  sortBy = signal<string>('recent');

  codeVisible: { [key: number]: boolean } = {};

  filteredSubmissions = computed(() => {
    let filtered = this.allSubmissions().filter((submission) => {
      const matchesSearch = (submission.problemTitle || `Problem #${submission.problemId}`)
        .toLowerCase()
        .includes(this.searchQuery().toLowerCase());
      const matchesStatus =
        !this.selectedStatus() || submission.status === this.selectedStatus();
      const matchesLanguage =
        !this.selectedLanguage() || submission.language === this.selectedLanguage();

      return matchesSearch && matchesStatus && matchesLanguage;
    });

    // Apply sorting
    if (this.sortBy() === 'oldest') {
      filtered = [...filtered].sort(
        (a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime()
      );
    } else {
      filtered = [...filtered].sort(
        (a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
      );
    }

    return filtered;
  });

  acceptedCount = computed(() =>
    this.allSubmissions().filter((s) => s.status === 'ACCEPTED').length
  );

  wrongAnswerCount = computed(() =>
    this.allSubmissions().filter((s) => s.status === 'WRONG_ANSWER').length
  );

  tleCount = computed(() =>
    this.allSubmissions().filter((s) => s.status === 'TIME_LIMIT_EXCEEDED').length
  );

  ngOnInit() {
    this.loadSubmissions();
  }

  loadSubmissions() {
    this.loading.set(true);
    this.submissionApi.getMySubmissions().subscribe({
      next: (data) => {
        this.allSubmissions.set(data || []);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(
          `Failed to load submissions: ${err.error?.message || err.statusText || 'Unknown error'}`
        );
        this.loading.set(false);
      },
    });
  }

  toggleCodeVisibility(submissionId: number): void {
    this.codeVisible[submissionId] = !this.codeVisible[submissionId];
  }

  getStatusBadgeClass(status: string): string {
    const classes: Record<string, string> = {
      ACCEPTED: 'badge-success',
      WRONG_ANSWER: 'badge-danger',
      TIME_LIMIT_EXCEEDED: 'badge-warning',
      RUNTIME_ERROR: 'badge-danger',
      COMPILATION_ERROR: 'badge-danger',
      MEMORY_LIMIT_EXCEEDED: 'badge-warning',
    };
    return classes[status] || 'badge-primary';
  }

  getStatusIconClass(status: string): string {
    const classes: Record<string, string> = {
      ACCEPTED: 'bg-success-600',
      WRONG_ANSWER: 'bg-danger-600',
      TIME_LIMIT_EXCEEDED: 'bg-warning-600',
      RUNTIME_ERROR: 'bg-danger-600',
      COMPILATION_ERROR: 'bg-danger-600',
      MEMORY_LIMIT_EXCEEDED: 'bg-warning-600',
    };
    return classes[status] || 'bg-primary-600';
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);

    if (diffMins < 1) return 'just now';
    if (diffMins < 60) return `${diffMins}m ago`;
    if (diffHours < 24) return `${diffHours}h ago`;
    if (diffDays < 7) return `${diffDays}d ago`;

    return date.toLocaleDateString();
  }

  formatProblemTitle(submission: SubmissionResponse): string {
    return submission.problemTitle || `Problem #${submission.problemId}`;
  }

  goToProblem(problemId: number) {
    this.router.navigate(['/problems', problemId]);
  }
}
