import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule, PercentPipe } from '@angular/common';
import { Router } from '@angular/router';
import { SubmissionApiService, GlobalLeaderboardEntry } from '../../services/submission-api.service';

@Component({
  selector: 'app-leaderboard',
  standalone: true,
  imports: [CommonModule, PercentPipe],
  template: `
    <div class="bg-slate-50 min-h-screen py-8">
      <div class="container-max">
        <!-- Header -->
        <div class="mb-8 text-center">
          <h1 class="text-4xl font-bold text-slate-900 mb-2">Leaderboard</h1>
          <p class="text-lg text-slate-600">
            See who's on top of the game
          </p>
        </div>

        <!-- Leaderboard Table -->
        <div class="bg-white rounded-lg border border-slate-200">
          <div class="overflow-x-auto">
            <table class="min-w-full divide-y divide-slate-200">
              <thead class="bg-slate-100">
                <tr>
                  <th
                    scope="col"
                    class="px-6 py-3 text-left text-xs font-medium text-slate-500 uppercase tracking-wider"
                  >
                    Rank
                  </th>
                  <th
                    scope="col"
                    class="px-6 py-3 text-left text-xs font-medium text-slate-500 uppercase tracking-wider"
                  >
                    User
                  </th>
                  <th
                    scope="col"
                    class="px-6 py-3 text-left text-xs font-medium text-slate-500 uppercase tracking-wider"
                  >
                    Problems Solved
                  </th>
                  <th
                    scope="col"
                    class="px-6 py-3 text-left text-xs font-medium text-slate-500 uppercase tracking-wider"
                  >
                    Acceptance Rate
                  </th>
                  <th
                    scope="col"
                    class="px-6 py-3 text-left text-xs font-medium text-slate-500 uppercase tracking-wider"
                  >
                    Last Submission
                  </th>
                </tr>
              </thead>
              <tbody class="bg-white divide-y divide-slate-200">
                @if (loading()) {
                  <tr>
                    <td colspan="5" class="px-6 py-8 text-center text-slate-600">
                      Loading...
                    </td>
                  </tr>
                } @else {
                  @for (user of leaderboard(); track user.username) {
                    <tr
                      class="hover:bg-slate-50 transition-colors"
                    >
                      <td class="px-6 py-4 whitespace-nowrap">
                        <div class="text-lg font-bold text-slate-900">
                          #{{ user.rank || $index + 1 }}
                        </div>
                      </td>
                      <td class="px-6 py-4 whitespace-nowrap">
                        <div class="flex items-center">
                          <div class="flex-shrink-0 h-10 w-10">
                            <div
                              class="h-10 w-10 rounded-full bg-primary-600 flex items-center justify-center text-white font-bold"
                            >
                              {{ (user.username && user.username.charAt(0)) ? user.username.charAt(0).toUpperCase() : 'U' }}
                            </div>
                          </div>
                          <div class="ml-4">
                            <div class="text-sm font-medium text-slate-900">
                              {{ user.username || 'Unknown' }}
                            </div>
                          </div>
                        </div>
                      </td>
                      <td class="px-6 py-4 whitespace-nowrap">
                        <div class="text-sm text-slate-900">
                          {{ user.totalAccepted || 0 }}
                        </div>
                      </td>
                      <td class="px-6 py-4 whitespace-nowrap">
                        <div class="text-sm text-slate-900">
                          {{ (user.acceptanceRate || 0) | percent }}
                        </div>
                      </td>
                      <td class="px-6 py-4 whitespace-nowrap text-sm text-slate-500">
                        {{ formatDate(user.lastSubmission) }}
                      </td>
                    </tr>
                  } @empty {
                    <tr>
                      <td colspan="5" class="px-6 py-8 text-center text-slate-600">
                        No leaderboard data available.
                      </td>
                    </tr>
                  }
                }
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  `,
})
export class LeaderboardComponent implements OnInit {
  private submissionApi = inject(SubmissionApiService);
  private router = inject(Router);
  
  leaderboard = signal<GlobalLeaderboardEntry[]>([]);
  loading = signal<boolean>(true);

  ngOnInit() {
    this.loadLeaderboard();
  }

  loadLeaderboard() {
    this.loading.set(true);
    
    // Mock data for leaderboard
    const mockData: GlobalLeaderboardEntry[] = [
      {
        rank: 1,
        userId: '1',
        username: 'admin1',
        totalScore: 300,
        totalAccepted: 3,
        totalSubmissions: 5,
        lastSubmission: new Date(Date.now() - 2 * 60 * 60 * 1000).toISOString(), // 2 hours ago
        acceptanceRate: 0.60 // 60% (3 accepted out of 5 submissions)
      },
      {
        rank: 2,
        userId: '2',
        username: 'test1',
        totalScore: 200,
        totalAccepted: 2,
        totalSubmissions: 4,
        lastSubmission: new Date(Date.now() - 5 * 60 * 60 * 1000).toISOString(), // 5 hours ago
        acceptanceRate: 0.50 // 50% (2 accepted out of 4 submissions)
      }
    ];

    // Simulate API delay
    setTimeout(() => {
      this.leaderboard.set(mockData);
      this.loading.set(false);
    }, 500);

    // Uncomment below to use real API
    /*
    this.submissionApi.getGlobalLeaderboard().subscribe({
      next: (data) => {
        this.leaderboard.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Failed to load leaderboard', err);
        this.loading.set(false);
      }
    });
    */
  }

  formatDate(dateString?: string): string {
    if (!dateString) return 'N/A';
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
}
