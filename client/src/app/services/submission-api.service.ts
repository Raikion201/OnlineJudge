import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, interval, switchMap, takeWhile, map, startWith, of, catchError } from 'rxjs';

export interface SubmissionPayload {
  problemId: number;
  language: string;
  code: string;
}

export interface RunCodePayload {
  problemId: number;
  language: string;
  code: string;
  customInput?: string;
}

export interface RunCodeResult {
  testCaseNumber?: number;
  input: string;
  expectedOutput: string;
  actualOutput: string;
  passed: boolean;
  executionTime?: number;
  memoryUsed?: number;
  error?: string;
  errorMessage?: string;
  status: string;
}

export interface RunCodeResponse {
  status: string;
  testCaseResults: RunCodeResult[];
  totalExecutionTime?: number;
  maxMemoryUsed?: number;
  errorMessage?: string;
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
  memoryUsed?: number;
  testCasesPassed?: number;
  totalTestCases?: number;
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

export interface TestCaseResult {
  testCaseId: number;
  status: 'pending' | 'running' | 'passed' | 'failed' | 'error';
  input?: string;
  expectedOutput?: string;
  actualOutput?: string;
  executionTime?: number;
  memoryUsed?: number;
  errorMessage?: string;
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

  /**
   * Run code against sample test cases (like LeetCode "Run" button)
   */
  runCode(payload: RunCodePayload): Observable<RunCodeResult[]> {
    return this.http.post<RunCodeResponse>(`${this.API_URL}/run`, payload).pipe(
      map(response => {
        // Handle error response
        if (response.errorMessage && (!response.testCaseResults || response.testCaseResults.length === 0)) {
          return [{
            input: '',
            expectedOutput: '',
            actualOutput: '',
            passed: false,
            status: response.status || 'ERROR',
            errorMessage: response.errorMessage
          }] as RunCodeResult[];
        }
        // Return the test case results array
        return response.testCaseResults || [];
      })
    );
  }

  /**
   * Get submission by ID
   */
  getSubmission(id: number): Observable<SubmissionResponse> {
    return this.http.get<SubmissionResponse>(`${this.API_URL}/${id}`);
  }

  /**
   * Poll submission status until it's complete
   * Returns an observable that emits status updates
   */
  pollSubmissionStatus(submissionId: number, intervalMs: number = 1000, maxAttempts: number = 60): Observable<SubmissionResponse> {
    let attempts = 0;
    return interval(intervalMs).pipe(
      startWith(0),
      switchMap(() => this.getSubmission(submissionId)),
      takeWhile((response) => {
        attempts++;
        const isTerminal = this.isTerminalStatus(response.status);
        return !isTerminal && attempts < maxAttempts;
      }, true),
      catchError(err => {
        console.error('Error polling submission status', err);
        return of({
          id: submissionId,
          status: 'ERROR',
          resultMessage: 'Failed to get submission status'
        } as SubmissionResponse);
      })
    );
  }

  /**
   * Check if a submission status is terminal (final)
   */
  isTerminalStatus(status: string): boolean {
    const terminalStatuses = [
      'ACCEPTED',
      'WRONG_ANSWER',
      'TIME_LIMIT_EXCEEDED',
      'MEMORY_LIMIT_EXCEEDED',
      'RUNTIME_ERROR',
      'COMPILATION_ERROR',
      'SYSTEM_ERROR',
      'ERROR'
    ];
    return terminalStatuses.includes(status?.toUpperCase());
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


