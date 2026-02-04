import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface User {
  id?: number;
  keycloakId?: string;
  username: string;
  email: string;
  fullName?: string;
  bio?: string;
  isActive?: boolean;
  createdAt?: string;
  roles?: string[];
}

export interface UserStatistics {
  userId: string;
  totalSubmissions: number;
  acceptedSubmissions: number;
  wrongAnswers: number;
  timeLimitExceeded: number;
  runtimeErrors: number;
  compilationErrors: number;
  easySolved: number;
  mediumSolved: number;
  hardSolved: number;
  totalProblemsSolved: number;
  totalScore: number;
  acceptanceRate: number;
  ranking: number;
  contributionPoints: number;
}

export interface UserActivity {
  date: string;
  submissionCount: number;
  acceptedCount: number;
  problemsAttempted: number;
  level: number; // 0-4 for heatmap coloring
}

export interface UserStreak {
  userId: string;
  currentStreak: number;
  maxStreak: number;
  lastActivityDate: string;
  streakStartDate: string;
  isActiveToday: boolean;
}

export interface LanguageStats {
  language: string;
  submissionCount: number;
  acceptedCount: number;
  problemsSolved: number;
  percentage: number;
}

export interface ActivityCalendar {
  userId: string;
  totalActiveDays: number;
  totalSubmissions: number;
  totalAccepted: number;
  activities: UserActivity[];
  streak: UserStreak;
}

export interface UserProfile {
  id: number;
  keycloakId: string;
  username: string;
  email: string;
  fullName: string;
  bio: string;
  isActive: boolean;
  createdAt: string;
  statistics: UserStatistics;
  streak: UserStreak;
  languageStats: LanguageStats[];
  activeDaysLast30: number;
  submissionsLast30: number;
}

export interface Bookmark {
  id: number;
  problemId: number;
  problemTitle?: string;
  difficulty?: string;
  note: string;
  createdAt: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

@Injectable({
  providedIn: 'root'
})
export class UserApiService {
  private readonly API_URL = 'http://localhost:8087/api/v1';

  constructor(private http: HttpClient) {}

  // User CRUD
  getUsers(page: number = 0, size: number = 20): Observable<PageResponse<User>> {
    return this.http.get<PageResponse<User>>(`${this.API_URL}/users`, {
      params: { page: page.toString(), size: size.toString() }
    });
  }

  getUserById(id: number): Observable<User> {
    return this.http.get<User>(`${this.API_URL}/users/${id}`);
  }

  getCurrentUser(): Observable<User> {
    return this.http.get<User>(`${this.API_URL}/users/me`);
  }

  updateUser(id: number, user: Partial<User>): Observable<User> {
    return this.http.put<User>(`${this.API_URL}/users/${id}`, user);
  }

  deleteUser(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/users/${id}`);
  }

  // Statistics
  getMyStatistics(): Observable<UserStatistics> {
    return this.http.get<UserStatistics>(`${this.API_URL}/users/me/statistics`);
  }

  getUserStatistics(userId: string): Observable<UserStatistics> {
    return this.http.get<UserStatistics>(`${this.API_URL}/users/${userId}/statistics`);
  }

  // Activity Calendar
  getMyActivityCalendar(days: number = 365): Observable<ActivityCalendar> {
    return this.http.get<ActivityCalendar>(`${this.API_URL}/users/me/activity-calendar`, {
      params: { days: days.toString() }
    });
  }

  getUserActivityCalendar(userId: string, days: number = 365): Observable<ActivityCalendar> {
    return this.http.get<ActivityCalendar>(`${this.API_URL}/users/${userId}/activity-calendar`, {
      params: { days: days.toString() }
    });
  }

  // Streak
  getMyStreak(): Observable<UserStreak> {
    return this.http.get<UserStreak>(`${this.API_URL}/users/me/streak`);
  }

  getUserStreak(userId: string): Observable<UserStreak> {
    return this.http.get<UserStreak>(`${this.API_URL}/users/${userId}/streak`);
  }

  // Language Stats
  getMyLanguageStats(): Observable<LanguageStats[]> {
    return this.http.get<LanguageStats[]>(`${this.API_URL}/users/me/language-stats`);
  }

  getUserLanguageStats(userId: string): Observable<LanguageStats[]> {
    return this.http.get<LanguageStats[]>(`${this.API_URL}/users/${userId}/language-stats`);
  }

  // Profile
  getMyProfile(): Observable<UserProfile> {
    return this.http.get<UserProfile>(`${this.API_URL}/users/me/profile`);
  }

  getUserProfile(userId: string): Observable<UserProfile> {
    return this.http.get<UserProfile>(`${this.API_URL}/users/${userId}/profile`);
  }

  // Bookmarks
  getMyBookmarks(): Observable<Bookmark[]> {
    return this.http.get<Bookmark[]>(`${this.API_URL}/users/me/bookmarks`);
  }

  addBookmark(problemId: number, problemTitle?: string, difficulty?: string, note?: string): Observable<Bookmark> {
    return this.http.post<Bookmark>(`${this.API_URL}/users/me/bookmarks`, {
      problemId,
      problemTitle,
      difficulty,
      note
    });
  }

  removeBookmark(problemId: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/users/me/bookmarks/${problemId}`);
  }

  isBookmarked(problemId: number): Observable<{ bookmarked: boolean }> {
    return this.http.get<{ bookmarked: boolean }>(`${this.API_URL}/users/me/bookmarks/check/${problemId}`);
  }

  getBookmarkCount(): Observable<{ count: number }> {
    return this.http.get<{ count: number }>(`${this.API_URL}/users/me/bookmarks/count`);
  }
}
