import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Problem {
  id: number;
  title: string;
  description: string;
  inputFormat: string;
  outputFormat: string;
  constraints: string;
  difficulty: 'EASY' | 'MEDIUM' | 'HARD';
  timeLimit: number;
  memoryLimit: number;
  isPublic: boolean;
  acceptanceRate: number;
  totalSubmissions: number;
  totalAccepted: number;
  createdAt: string;
  updatedAt: string;
  examples: ProblemExample[];
  tags: Tag[];
}

export interface ProblemExample {
  id: number;
  input: string;
  output: string;
  explanation: string;
  ordering: number;
}

export interface Tag {
  id: number;
  name: string;
  slug: string;
  description: string;
}

export interface Editorial {
  id: number;
  problemId: number;
  problemTitle: string;
  content: string;
  approach: string;
  timeComplexity: string;
  spaceComplexity: string;
  solutionCode: string;
  solutionLanguage: string;
  videoUrl: string;
  authorId: string;
  authorName: string;
  isPremium: boolean;
  viewCount: number;
  helpfulCount: number;
  createdAt: string;
  updatedAt: string;
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
export class ProblemApiService {
  private readonly API_URL = 'http://localhost:8087/api/v1';

  constructor(private http: HttpClient) {}

  // Problems
  getProblems(page: number = 0, size: number = 20): Observable<PageResponse<Problem>> {
    return this.http.get<PageResponse<Problem>>(`${this.API_URL}/problems`, {
      params: { page: page.toString(), size: size.toString() }
    });
  }

  getProblem(id: number): Observable<Problem> {
    return this.http.get<Problem>(`${this.API_URL}/problems/${id}`);
  }

  searchProblems(keyword?: string, difficulty?: string, tags?: string[]): Observable<PageResponse<Problem>> {
    let params: any = {};
    if (keyword) params.keyword = keyword;
    if (difficulty) params.difficulty = difficulty;
    if (tags && tags.length > 0) params.tags = tags.join(',');
    return this.http.get<PageResponse<Problem>>(`${this.API_URL}/problems/search`, { params });
  }

  createProblem(problem: Partial<Problem>): Observable<Problem> {
    return this.http.post<Problem>(`${this.API_URL}/problems`, problem);
  }

  updateProblem(id: number, problem: Partial<Problem>): Observable<Problem> {
    return this.http.put<Problem>(`${this.API_URL}/problems/${id}`, problem);
  }

  deleteProblem(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/problems/${id}`);
  }

  // Tags
  getTags(): Observable<PageResponse<Tag>> {
    return this.http.get<PageResponse<Tag>>(`${this.API_URL}/tags`);
  }

  // Editorials
  getEditorial(problemId: number): Observable<Editorial> {
    return this.http.get<Editorial>(`${this.API_URL}/problems/${problemId}/editorial`);
  }

  hasEditorial(problemId: number): Observable<{ exists: boolean }> {
    return this.http.get<{ exists: boolean }>(`${this.API_URL}/problems/${problemId}/editorial/exists`);
  }

  createEditorial(problemId: number, editorial: Partial<Editorial>): Observable<Editorial> {
    return this.http.post<Editorial>(`${this.API_URL}/problems/${problemId}/editorial`, editorial);
  }

  updateEditorial(problemId: number, editorial: Partial<Editorial>): Observable<Editorial> {
    return this.http.put<Editorial>(`${this.API_URL}/problems/${problemId}/editorial`, editorial);
  }

  deleteEditorial(problemId: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/problems/${problemId}/editorial`);
  }

  markEditorialHelpful(problemId: number): Observable<void> {
    return this.http.post<void>(`${this.API_URL}/problems/${problemId}/editorial/helpful`, {});
  }
}
