import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { UserApiService, Bookmark } from '../../services/user-api.service';
import { catchError, finalize } from 'rxjs/operators';
import { of } from 'rxjs';

@Component({
  selector: 'app-bookmarks',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="bookmarks-container">
      <div class="bookmarks-header">
        <h1>Saved Problems</h1>
        @if (!loading()) {
          <p class="subtitle">{{ bookmarks().length }} problem{{ bookmarks().length !== 1 ? 's' : '' }} saved for later</p>
        } @else {
          <p class="subtitle">Loading...</p>
        }
      </div>

      @if (loading()) {
        <div class="loading-state">
          <div class="spinner"></div>
          <p>Loading your saved problems...</p>
        </div>
      } @else if (bookmarks().length > 0) {
        <div class="bookmarks-list">
          @for (bookmark of bookmarks(); track bookmark.id) {
            <div class="bookmark-card">
              <div class="bookmark-content">
                <div class="problem-info">
                  <a [routerLink]="['/problems', bookmark.problemId]" class="problem-title">
                    {{ bookmark.problemTitle || 'Problem #' + bookmark.problemId }}
                  </a>
                  <span class="difficulty" [class]="bookmark.difficulty?.toLowerCase()">
                    {{ bookmark.difficulty || 'Unknown' }}
                  </span>
                </div>
                @if (bookmark.note) {
                  <p class="note">{{ bookmark.note }}</p>
                }
                <p class="bookmark-date">Saved on {{ bookmark.createdAt | date:'medium' }}</p>
              </div>
              <div class="bookmark-actions">
                <a [routerLink]="['/problems', bookmark.problemId]" class="btn-solve">
                  Solve
                </a>
                <button class="btn-remove" (click)="removeBookmark(bookmark.problemId)">
                  Remove
                </button>
              </div>
            </div>
          }
        </div>
      } @else {
        <div class="empty-state">
          <div class="empty-icon">&#128278;</div>
          <h2>No saved problems yet</h2>
          <p>When you find a problem you want to solve later, click the bookmark icon to save it here.</p>
          <a routerLink="/problems" class="btn-browse">Browse Problems</a>
        </div>
      }
    </div>
  `,
  styles: [`
    .bookmarks-container {
      max-width: 900px;
      margin: 0 auto;
      padding: 2rem;
    }

    .bookmarks-header {
      margin-bottom: 2rem;
    }

    .bookmarks-header h1 {
      margin: 0;
      color: #fff;
      font-size: 1.8rem;
    }

    .subtitle {
      color: #888;
      margin: 0.5rem 0 0 0;
    }

    .bookmarks-list {
      display: flex;
      flex-direction: column;
      gap: 1rem;
    }

    .bookmark-card {
      background: #1e1e2e;
      border-radius: 10px;
      padding: 1.25rem;
      display: flex;
      justify-content: space-between;
      align-items: center;
      transition: transform 0.2s, box-shadow 0.2s;
    }

    .bookmark-card:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
    }

    .bookmark-content {
      flex: 1;
    }

    .problem-info {
      display: flex;
      align-items: center;
      gap: 1rem;
      margin-bottom: 0.5rem;
    }

    .problem-title {
      color: #fff;
      text-decoration: none;
      font-size: 1.1rem;
      font-weight: 500;
    }

    .problem-title:hover {
      color: #667eea;
    }

    .difficulty {
      padding: 0.2rem 0.6rem;
      border-radius: 4px;
      font-size: 0.75rem;
      font-weight: 600;
      text-transform: uppercase;
    }

    .difficulty.easy {
      background: rgba(0, 184, 169, 0.2);
      color: #00b8a9;
    }

    .difficulty.medium {
      background: rgba(255, 193, 7, 0.2);
      color: #ffc107;
    }

    .difficulty.hard {
      background: rgba(255, 107, 107, 0.2);
      color: #ff6b6b;
    }

    .note {
      color: #a0a0a0;
      font-size: 0.9rem;
      margin: 0.5rem 0;
      font-style: italic;
    }

    .bookmark-date {
      color: #666;
      font-size: 0.8rem;
      margin: 0;
    }

    .bookmark-actions {
      display: flex;
      gap: 0.75rem;
    }

    .btn-solve {
      padding: 0.5rem 1.25rem;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      border: none;
      border-radius: 6px;
      text-decoration: none;
      font-weight: 500;
      cursor: pointer;
      transition: opacity 0.2s;
    }

    .btn-solve:hover {
      opacity: 0.9;
    }

    .btn-remove {
      padding: 0.5rem 1rem;
      background: transparent;
      color: #ff6b6b;
      border: 1px solid #ff6b6b;
      border-radius: 6px;
      cursor: pointer;
      transition: all 0.2s;
    }

    .btn-remove:hover {
      background: rgba(255, 107, 107, 0.1);
    }

    .empty-state {
      text-align: center;
      padding: 4rem 2rem;
      background: #1e1e2e;
      border-radius: 12px;
    }

    .empty-icon {
      font-size: 4rem;
      margin-bottom: 1rem;
    }

    .empty-state h2 {
      color: #fff;
      margin: 0 0 0.5rem 0;
    }

    .empty-state p {
      color: #888;
      max-width: 400px;
      margin: 0 auto 1.5rem;
    }

    .btn-browse {
      display: inline-block;
      padding: 0.75rem 1.5rem;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      text-decoration: none;
      border-radius: 8px;
      font-weight: 500;
    }

    .loading-state {
      text-align: center;
      padding: 4rem 2rem;
      background: #1e1e2e;
      border-radius: 12px;
    }

    .loading-state p {
      color: #888;
      margin-top: 1rem;
    }

    .spinner {
      width: 40px;
      height: 40px;
      margin: 0 auto;
      border: 3px solid #333;
      border-top-color: #667eea;
      border-radius: 50%;
      animation: spin 0.8s linear infinite;
    }

    @keyframes spin {
      to { transform: rotate(360deg); }
    }
  `]
})
export class BookmarksComponent implements OnInit {
  private readonly userApi = inject(UserApiService);

  bookmarks = signal<Bookmark[]>([]);
  loading = signal(true);

  ngOnInit() {
    this.loadBookmarks();
  }

  loadBookmarks() {
    this.loading.set(true);
    this.userApi.getMyBookmarks()
      .pipe(
        catchError((error) => {
          console.error('Failed to load bookmarks:', error);
          // Return empty array on error
          return of([]);
        }),
        finalize(() => {
          // Always set loading to false when the observable completes
          this.loading.set(false);
        })
      )
      .subscribe({
        next: (response: any) => {
          console.log('Bookmarks API response:', response);
          // Handle both array and paginated response
          let parsed: Bookmark[] = [];
          if (Array.isArray(response)) {
            parsed = response;
          } else if (response && Array.isArray(response.content)) {
            parsed = response.content;
          } else if (response && Array.isArray(response.data)) {
            parsed = response.data;
          } else if (response && typeof response === 'object') {
            // Handle single object or other response formats
            parsed = [];
            console.warn('Unexpected bookmarks response format:', response);
          }
          console.log('Parsed bookmarks:', parsed);
          this.bookmarks.set(parsed);
        }
      });
  }

  removeBookmark(problemId: number) {
    this.userApi.removeBookmark(problemId).subscribe({
      next: () => {
        this.bookmarks.update(current => current.filter(b => b.problemId !== problemId));
      }
    });
  }
}
