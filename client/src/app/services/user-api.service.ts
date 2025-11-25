import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface User {
  id?: number;
  username: string;
  email: string;
  firstName?: string;
  lastName?: string;
  roles?: string[];
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

  /**
   * Get all users (paginated)
   */
  getUsers(page: number = 0, size: number = 20): Observable<PageResponse<User>> {
    return this.http.get<PageResponse<User>>(`${this.API_URL}/users`, {
      params: { page: page.toString(), size: size.toString() }
    });
  }

  /**
   * Get user by ID
   */
  getUserById(id: number): Observable<User> {
    return this.http.get<User>(`${this.API_URL}/users/${id}`);
  }

  /**
   * Get current user profile
   */
  getCurrentUser(): Observable<User> {
    return this.http.get<User>(`${this.API_URL}/users/me`);
  }

  /**
   * Update user
   */
  updateUser(id: number, user: Partial<User>): Observable<User> {
    return this.http.put<User>(`${this.API_URL}/users/${id}`, user);
  }

  /**
   * Delete user
   */
  deleteUser(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/users/${id}`);
  }
}
