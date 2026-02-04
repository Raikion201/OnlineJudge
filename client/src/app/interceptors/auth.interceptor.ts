import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.getToken();

  console.log('[AuthInterceptor] Request URL:', req.url);
  console.log('[AuthInterceptor] Token available:', !!token);
  
  // Add token to requests going to API Gateway
  if (token && req.url.includes('localhost:8087')) {
    console.log('[AuthInterceptor] Adding token to request');
    const cloned = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
    return next(cloned);
  }

  console.log('[AuthInterceptor] No token or not API request, passing through');
  return next(req);
};
