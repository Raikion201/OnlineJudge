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
}


