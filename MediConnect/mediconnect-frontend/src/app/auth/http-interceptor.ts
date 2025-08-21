import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { catchError } from 'rxjs/operators';
import { throwError } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  // Add auth token if available
  const token = localStorage.getItem('token');
  if (token) {
    request = request.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  // Add CORS headers
  request = request.clone({
    setHeaders: {
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    }
  });

  return next(request).pipe(
    catchError((error: HttpErrorResponse) => {
      console.error('HTTP Error:', error);
      
      if (error.status === 0) {
        console.error('Network error - server might be down');
      } else if (error.status === 401) {
        console.error('Unauthorized - token invalid or expired');
        // Clear local session and redirect
        localStorage.removeItem('token');
        localStorage.removeItem('username');
        localStorage.removeItem('role');
        localStorage.removeItem('userId');
        // Use hard redirect to avoid DI here
        if (typeof window !== 'undefined') {
          window.location.href = '/auth/login';
        }
      } else if (error.status === 403) {
        console.error('Forbidden - insufficient permissions');
        if (typeof window !== 'undefined') {
          window.location.href = '/home';
        }
      } else if (error.status === 404) {
        console.error('Not found - endpoint might not exist');
      } else if (error.status >= 500) {
        console.error('Server error - backend issue');
      }
      
      return throwError(() => error);
    })
  );
};