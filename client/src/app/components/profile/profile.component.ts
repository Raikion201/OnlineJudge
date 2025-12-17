import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute } from '@angular/router';
import { UserApiService, UserProfile, ActivityCalendar, UserActivity, LanguageStats } from '../../services/user-api.service';
import { AuthService } from '../../services/auth.service';
import { finalize, catchError } from 'rxjs/operators';
import { of } from 'rxjs';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    @if (profile()) {
      <div class="profile-container">
        <!-- Header Section -->
        <div class="profile-header">
          <div class="avatar-section">
            <div class="avatar">{{ profile()!.username?.charAt(0)?.toUpperCase() }}</div>
            <div class="user-info">
              <h1>{{ profile()!.username }}</h1>
              @if (profile()!.fullName) {
                <p class="full-name">{{ profile()!.fullName }}</p>
              }
              @if (profile()!.bio) {
                <p class="bio">{{ profile()!.bio }}</p>
              }
              <p class="member-since">Member since {{ profile()!.createdAt | date:'MMMM yyyy' }}</p>
            </div>
          </div>
          <div class="quick-stats">
            <div class="stat-item">
              <span class="stat-value">{{ profile()!.statistics?.ranking || 'N/A' }}</span>
              <span class="stat-label">Ranking</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">{{ profile()!.statistics?.totalProblemsSolved || 0 }}</span>
              <span class="stat-label">Problems Solved</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">{{ profile()!.streak?.currentStreak || 0 }}</span>
              <span class="stat-label">Day Streak</span>
            </div>
          </div>
        </div>

        <!-- Stats Grid -->
        <div class="stats-grid">
          <!-- Problem Solving Stats -->
          <div class="stats-card">
            <h3>Problem Solving</h3>
            <div class="difficulty-stats">
              <div class="diff-item easy">
                <span class="diff-count">{{ profile()!.statistics?.easySolved || 0 }}</span>
                <span class="diff-label">Easy</span>
              </div>
              <div class="diff-item medium">
                <span class="diff-count">{{ profile()!.statistics?.mediumSolved || 0 }}</span>
                <span class="diff-label">Medium</span>
              </div>
              <div class="diff-item hard">
                <span class="diff-count">{{ profile()!.statistics?.hardSolved || 0 }}</span>
                <span class="diff-label">Hard</span>
              </div>
            </div>
            <div class="total-solved">
              <span>Total: {{ profile()!.statistics?.totalProblemsSolved || 0 }} problems</span>
            </div>
          </div>

          <!-- Submission Stats -->
          <div class="stats-card">
            <h3>Submissions</h3>
            <div class="submission-stats">
              <div class="stat-row">
                <span>Total Submissions</span>
                <span class="value">{{ profile()!.statistics?.totalSubmissions || 0 }}</span>
              </div>
              <div class="stat-row accepted">
                <span>Accepted</span>
                <span class="value">{{ profile()!.statistics?.acceptedSubmissions || 0 }}</span>
              </div>
              <div class="stat-row">
                <span>Wrong Answers</span>
                <span class="value">{{ profile()!.statistics?.wrongAnswers || 0 }}</span>
              </div>
              <div class="stat-row">
                <span>Runtime Errors</span>
                <span class="value">{{ profile()!.statistics?.runtimeErrors || 0 }}</span>
              </div>
              <div class="stat-row">
                <span>Acceptance Rate</span>
                <span class="value">{{ (profile()!.statistics?.acceptanceRate || 0) | number:'1.1-1' }}%</span>
              </div>
            </div>
          </div>

          <!-- Streak Card -->
          <div class="stats-card streak-card">
            <h3>Streak</h3>
            <div class="streak-display">
              <div class="current-streak">
                <span class="streak-number">{{ profile()!.streak?.currentStreak || 0 }}</span>
                <span class="streak-label">Current Streak</span>
              </div>
              <div class="max-streak">
                <span class="streak-number">{{ profile()!.streak?.maxStreak || 0 }}</span>
                <span class="streak-label">Max Streak</span>
              </div>
            </div>
            <p class="streak-status" [class.active]="profile()!.streak?.isActiveToday">
              {{ profile()!.streak?.isActiveToday ? 'Active today!' : 'Not active today' }}
            </p>
          </div>

          <!-- Language Stats -->
          <div class="stats-card">
            <h3>Languages Used</h3>
            @if (profile()!.languageStats?.length) {
              <div class="language-stats">
                @for (lang of profile()!.languageStats; track lang.language) {
                  <div class="lang-item">
                    <div class="lang-header">
                      <span class="lang-name">{{ lang.language }}</span>
                      <span class="lang-count">{{ lang.submissionCount }} submissions</span>
                    </div>
                    <div class="lang-bar">
                      <div class="lang-fill" [style.width.%]="lang.percentage"></div>
                    </div>
                    <div class="lang-details">
                      <span>{{ lang.acceptedCount }} accepted</span>
                      <span>{{ lang.problemsSolved }} problems</span>
                    </div>
                  </div>
                }
              </div>
            } @else {
              <p class="no-data">No submissions yet</p>
            }
          </div>
        </div>

        <!-- Activity Calendar -->
        @if (activityCalendar()) {
          <div class="activity-section">
            <h3>{{ activityCalendar()!.totalSubmissions }} submissions in the last year</h3>
            <div class="activity-calendar">
              <div class="calendar-grid">
                @for (activity of activityCalendar()!.activities; track activity.date) {
                  <div
                    class="calendar-day"
                    [class.level-0]="activity.level === 0"
                    [class.level-1]="activity.level === 1"
                    [class.level-2]="activity.level === 2"
                    [class.level-3]="activity.level === 3"
                    [class.level-4]="activity.level === 4"
                    [title]="getActivityTooltip(activity)">
                  </div>
                }
              </div>
              <div class="calendar-legend">
                <span>Less</span>
                <div class="legend-item level-0"></div>
                <div class="legend-item level-1"></div>
                <div class="legend-item level-2"></div>
                <div class="legend-item level-3"></div>
                <div class="legend-item level-4"></div>
                <span>More</span>
              </div>
            </div>
            <div class="activity-summary">
              <span>{{ activityCalendar()!.totalActiveDays }} active days</span>
              <span>{{ activityCalendar()!.totalAccepted }} accepted submissions</span>
            </div>
          </div>
        }
      </div>
    }

    @if (loading() && !profile()) {
      <div class="loading">Loading profile...</div>
    }
    @if (error() && !profile()) {
      <div class="error">{{ error() }}</div>
    }
  `,
  styles: [`
    .profile-container {
      max-width: 1200px;
      margin: 0 auto;
      padding: 2rem;
    }

    .profile-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
      border-radius: 12px;
      padding: 2rem;
      margin-bottom: 2rem;
    }

    .avatar-section {
      display: flex;
      gap: 1.5rem;
      align-items: center;
    }

    .avatar {
      width: 100px;
      height: 100px;
      border-radius: 50%;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 2.5rem;
      font-weight: bold;
      color: white;
    }

    .user-info h1 {
      margin: 0;
      font-size: 1.8rem;
      color: #fff;
    }

    .user-info .full-name {
      color: #a0a0a0;
      margin: 0.3rem 0;
    }

    .user-info .bio {
      color: #ccc;
      margin: 0.5rem 0;
      max-width: 400px;
    }

    .user-info .member-since {
      color: #888;
      font-size: 0.9rem;
    }

    .quick-stats {
      display: flex;
      gap: 2rem;
    }

    .stat-item {
      text-align: center;
    }

    .stat-value {
      display: block;
      font-size: 1.8rem;
      font-weight: bold;
      color: #667eea;
    }

    .stat-label {
      color: #888;
      font-size: 0.9rem;
    }

    .stats-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
      gap: 1.5rem;
      margin-bottom: 2rem;
    }

    .stats-card {
      background: #1e1e2e;
      border-radius: 12px;
      padding: 1.5rem;
    }

    .stats-card h3 {
      margin: 0 0 1rem 0;
      color: #fff;
      font-size: 1.1rem;
    }

    .difficulty-stats {
      display: flex;
      justify-content: space-around;
      margin-bottom: 1rem;
    }

    .diff-item {
      text-align: center;
    }

    .diff-count {
      display: block;
      font-size: 1.5rem;
      font-weight: bold;
    }

    .diff-item.easy .diff-count { color: #00b8a9; }
    .diff-item.medium .diff-count { color: #ffc107; }
    .diff-item.hard .diff-count { color: #ff6b6b; }

    .diff-label {
      color: #888;
      font-size: 0.85rem;
    }

    .total-solved {
      text-align: center;
      color: #a0a0a0;
      padding-top: 1rem;
      border-top: 1px solid #333;
    }

    .submission-stats .stat-row {
      display: flex;
      justify-content: space-between;
      padding: 0.5rem 0;
      color: #ccc;
    }

    .submission-stats .stat-row.accepted .value {
      color: #00b8a9;
    }

    .submission-stats .value {
      font-weight: 600;
    }

    .streak-display {
      display: flex;
      justify-content: space-around;
      margin-bottom: 1rem;
    }

    .current-streak, .max-streak {
      text-align: center;
    }

    .streak-number {
      display: block;
      font-size: 2rem;
      font-weight: bold;
      color: #ff9f43;
    }

    .streak-label {
      color: #888;
      font-size: 0.85rem;
    }

    .streak-status {
      text-align: center;
      color: #888;
    }

    .streak-status.active {
      color: #00b8a9;
    }

    .language-stats {
      display: flex;
      flex-direction: column;
      gap: 1rem;
    }

    .lang-item {
      padding: 0.5rem 0;
    }

    .lang-header {
      display: flex;
      justify-content: space-between;
      margin-bottom: 0.3rem;
    }

    .lang-name {
      color: #fff;
      font-weight: 500;
    }

    .lang-count {
      color: #888;
      font-size: 0.85rem;
    }

    .lang-bar {
      height: 6px;
      background: #333;
      border-radius: 3px;
      overflow: hidden;
    }

    .lang-fill {
      height: 100%;
      background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
      border-radius: 3px;
    }

    .lang-details {
      display: flex;
      justify-content: space-between;
      margin-top: 0.3rem;
      color: #666;
      font-size: 0.8rem;
    }

    .activity-section {
      background: #1e1e2e;
      border-radius: 12px;
      padding: 1.5rem;
    }

    .activity-section h3 {
      margin: 0 0 1rem 0;
      color: #a0a0a0;
      font-size: 0.95rem;
    }

    .calendar-grid {
      display: grid;
      grid-template-columns: repeat(53, 1fr);
      gap: 3px;
      margin-bottom: 1rem;
    }

    .calendar-day {
      aspect-ratio: 1;
      border-radius: 2px;
      min-width: 10px;
    }

    .calendar-day.level-0 { background: #161b22; }
    .calendar-day.level-1 { background: #0e4429; }
    .calendar-day.level-2 { background: #006d32; }
    .calendar-day.level-3 { background: #26a641; }
    .calendar-day.level-4 { background: #39d353; }

    .calendar-legend {
      display: flex;
      align-items: center;
      justify-content: flex-end;
      gap: 4px;
      color: #666;
      font-size: 0.8rem;
    }

    .legend-item {
      width: 12px;
      height: 12px;
      border-radius: 2px;
    }

    .legend-item.level-0 { background: #161b22; }
    .legend-item.level-1 { background: #0e4429; }
    .legend-item.level-2 { background: #006d32; }
    .legend-item.level-3 { background: #26a641; }
    .legend-item.level-4 { background: #39d353; }

    .activity-summary {
      display: flex;
      gap: 2rem;
      margin-top: 1rem;
      color: #888;
      font-size: 0.9rem;
    }

    .no-data {
      color: #666;
      text-align: center;
      padding: 1rem;
    }

    .loading, .error {
      text-align: center;
      padding: 3rem;
      color: #888;
    }

    .error {
      color: #ff6b6b;
    }
  `]
})
export class ProfileComponent implements OnInit {
  private readonly userApi = inject(UserApiService);
  private readonly authService = inject(AuthService);
  private readonly route = inject(ActivatedRoute);

  profile = signal<UserProfile | null>(null);
  activityCalendar = signal<ActivityCalendar | null>(null);
  loading = signal(true);
  error = signal<string | null>(null);

  ngOnInit() {
    const userId = this.route.snapshot.paramMap.get('userId');

    // Wait a bit for Keycloak authentication to complete
    // This handles the race condition where the profile page loads before auth is ready
    const checkAuthAndLoad = () => {
      if (this.authService.isAuthenticated()) {
        if (userId) {
          this.loadUserProfile(userId);
        } else {
          this.loadMyProfile();
        }
      } else {
        // If not authenticated after waiting, show error
        this.loading.set(false);
        this.error.set('Please log in to view your profile');
      }
    };

    // Give Keycloak a moment to complete silent SSO
    if (this.authService.isAuthenticated()) {
      checkAuthAndLoad();
    } else {
      // Wait up to 2 seconds for auth to complete
      setTimeout(() => {
        checkAuthAndLoad();
      }, 1500);
    }
  }

  private loadMyProfile() {
    this.loading.set(true);
    this.error.set(null);
    this.userApi.getMyProfile()
      .pipe(
        catchError((err) => {
          console.error('[Profile] Failed to load profile:', err);
          this.error.set('Failed to load profile');
          return of(null);
        }),
        finalize(() => {
          this.loading.set(false);
        })
      )
      .subscribe({
        next: (profile) => {
          if (profile) {
            this.profile.set(profile);
            // Load activity calendar in background
            if (profile.keycloakId) {
              this.loadActivityCalendar(profile.keycloakId);
            }
          }
        }
      });
  }

  private loadUserProfile(userId: string) {
    this.loading.set(true);
    this.error.set(null);
    this.userApi.getUserProfile(userId)
      .pipe(
        catchError((err) => {
          console.error('[Profile] Failed to load user profile:', err);
          this.error.set('Failed to load profile');
          return of(null);
        }),
        finalize(() => {
          this.loading.set(false);
        })
      )
      .subscribe({
        next: (profile) => {
          if (profile) {
            this.profile.set(profile);
            // Load activity calendar in background
            this.loadActivityCalendar(userId);
          }
        }
      });
  }

  private loadActivityCalendar(userId: string) {
    this.userApi.getUserActivityCalendar(userId, 365).subscribe({
      next: (calendar) => {
        this.activityCalendar.set(calendar);
      },
      error: (err) => {
        console.error('Failed to load activity calendar:', err);
        // Don't set error - activity calendar is optional
      }
    });
  }

  getActivityTooltip(activity: UserActivity): string {
    const date = new Date(activity.date).toLocaleDateString();
    if (activity.submissionCount === 0) {
      return `${date}: No submissions`;
    }
    return `${date}: ${activity.submissionCount} submissions, ${activity.acceptedCount} accepted`;
  }
}
