import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface SubmissionPayload {
  problemId: number;
  language: string;
  code: string;
}

export interface SubmissionResponse {
  id: number;
  problemId: number;
  userId: string;
  language: string;
  code: string;
  status: string;
  resultMessage?: string;
  createdAt: string;
  updatedAt: string;
  problemTitle?: string;
  score?: number;
  executionTime?: number;
}

export interface LeaderboardEntry {
  userId: string;
  username: string;
  score: number;
  executionTime: number;
  language: string;
  submittedAt: string;
}

export interface GlobalLeaderboardEntry {
  rank: number;
  userId: string;
  username: string;
  totalScore: number;
  totalAccepted: number;
  totalSubmissions: number;
  lastSubmission: string;
  acceptanceRate: number;
}

@Injectable({
  providedIn: 'root'
})
export class SubmissionApiService {
  private readonly http = inject(HttpClient);
  private readonly API_URL = 'http://localhost:8087/api/v1/submissions';

  submit(payload: SubmissionPayload): Observable<SubmissionResponse> {
    return this.http.post<SubmissionResponse>(this.API_URL, payload);
  }

  getMySubmissions(): Observable<SubmissionResponse[]> {
    return this.http.get<SubmissionResponse[]>(`${this.API_URL}/user/me`);
  }

  getLeaderboard(problemId: number): Observable<LeaderboardEntry[]> {
    return this.http.get<LeaderboardEntry[]>(`${this.API_URL}/leaderboard/${problemId}`);
  }

  getGlobalLeaderboard(): Observable<GlobalLeaderboardEntry[]> {
    return this.http.get<GlobalLeaderboardEntry[]>(`${this.API_URL}/leaderboard/global`);
  }
}


