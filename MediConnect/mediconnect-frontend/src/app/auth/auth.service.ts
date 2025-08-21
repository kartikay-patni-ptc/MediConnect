import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap, switchMap } from 'rxjs/operators';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  username: string;
  role: string;
  userId: number;
}

export interface User {
  username: string;
  email: string;
  password: string;
  role?: string;
}

export interface ForgotPasswordRequest {
  username: string;
}

export interface ResetPasswordRequest {
  username: string;
  newPassword: string;
}

export interface PharmacyProfile {
  name: string;
  ownerName: string;
  licenseNumber: string;
  phoneNumber: string;
  email: string;
  address: string;
  description: string;
  latitude?: number;
  longitude?: number;
  userId: number;
}

export interface DoctorProfile {
  firstName: string;
  lastName: string;
  specialization: string;
  licenseNumber: string;
  phoneNumber: string;
  email: string;
  experience: number;
  education: string;
  hospital: string;
  address: string;
  description: string;
  userId: number;
}

export interface ApiResponse {
  message?: string;
  error?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly API_URL = 'http://localhost:8080/api/auth';
  private readonly PHARMACY_API_URL = 'http://localhost:8080/api/pharmacy';
  private readonly DOCTOR_API_URL = 'http://localhost:8080/api/doctor';
  private readonly AI_API_URL = 'http://localhost:8080/api/ai';
  private readonly MEDS_API_URL = 'http://localhost:8080/api/patient/history/medications';

  constructor(private http: HttpClient) {}

  healthCheck(): Observable<ApiResponse> {
    return this.http.get<ApiResponse>(`${this.API_URL}/health`);
  }

  testConnection(): Observable<ApiResponse> {
    return this.http.get<ApiResponse>(`${this.API_URL}/test`);
  }

  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.API_URL}/login`, credentials)
      .pipe(
        tap((response: LoginResponse) => {
          localStorage.setItem('token', response.token);
          localStorage.setItem('username', response.username);
          localStorage.setItem('role', response.role);
          localStorage.setItem('userId', response.userId.toString());
        })
      );
  }

  register(user: User): Observable<ApiResponse> {
    console.log('Sending registration request:', user);
    return this.http.post<ApiResponse>(`${this.API_URL}/register`, user);
  }

  forgotPassword(request: ForgotPasswordRequest): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(`${this.API_URL}/forgot-password`, request);
  }

  resetPassword(request: ResetPasswordRequest): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(`${this.API_URL}/reset-password`, request);
  }

  // Pharmacy Profile Methods
  createPharmacyProfile(profile: PharmacyProfile): Observable<any> {
    return this.http.post(`${this.PHARMACY_API_URL}/create-profile`, profile);
  }

  getPharmacyProfile(userId: number): Observable<any> {
    return this.http.get(`${this.PHARMACY_API_URL}/profile/${userId}`);
  }

  getPharmacyDashboard(userId: number): Observable<any> {
    return this.http.get(`${this.PHARMACY_API_URL}/dashboard/${userId}`);
  }

  updatePharmacyProfile(id: number, profile: PharmacyProfile): Observable<any> {
    return this.http.put(`${this.PHARMACY_API_URL}/profile/${id}`, profile);
  }

  // Doctor Profile Methods
  createDoctorProfile(profile: DoctorProfile): Observable<any> {
    return this.http.post(`${this.DOCTOR_API_URL}/create-profile`, profile);
  }

  getDoctorProfile(userId: number): Observable<any> {
    return this.http.get(`${this.DOCTOR_API_URL}/profile/${userId}`);
  }

  getDoctorDashboard(userId: number): Observable<any> {
    return this.http.get(`${this.DOCTOR_API_URL}/dashboard/${userId}`);
  }

  updateDoctorProfile(id: number, profile: DoctorProfile): Observable<any> {
    return this.http.put(`${this.DOCTOR_API_URL}/profile/${id}`, profile);
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    localStorage.removeItem('role');
    localStorage.removeItem('userId');
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  isLoggedIn(): boolean {
    const token = this.getToken();
    if (!token) { return false; }
    if (this.isTokenExpired(token)) {
      this.logout();
      return false;
    }
    return true;
  }

  private decodeJwt(token: string): any | null {
    try {
      const payload = token.split('.')[1];
      const decoded = atob(payload.replace(/-/g, '+').replace(/_/g, '/'));
      return JSON.parse(decodeURIComponent(Array.prototype.map.call(decoded, (c: string) => {
        return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
      }).join('')));
    } catch {
      return null;
    }
  }

  isTokenExpired(token?: string): boolean {
    const jwt = token || this.getToken();
    if (!jwt) { return true; }
    const payload = this.decodeJwt(jwt);
    if (!payload || !payload.exp) { return true; }
    const nowSeconds = Math.floor(Date.now() / 1000);
    return payload.exp < nowSeconds;
  }

  getCurrentUser(): any {
    return {
      username: localStorage.getItem('username'),
      role: localStorage.getItem('role'),
      userId: parseInt(localStorage.getItem('userId') || '0')
    };
  }
  getUserId(): number {
    return parseInt(localStorage.getItem('userId') || '0');
  }

  // Doctor Verification Methods
  verifyDoctor(verificationData: any): Observable<any> {
    return this.http.post(`${this.DOCTOR_API_URL}/verify`, verificationData);
  }

  // Patient Profile Methods
  createPatientProfile(profile: any): Observable<any> {
    return this.http.post('http://localhost:8080/api/patient/create-profile', profile);
  }

  getPatientProfile(userId: number): Observable<any> {
    return this.http.get(`http://localhost:8080/api/patient/profile/${userId}`);
  }

  getPatientDashboard(userId: number): Observable<any> {
    return this.http.get(`http://localhost:8080/api/patient/dashboard/${userId}`);
  }

  updatePatientProfile(id: number, profile: any): Observable<any> {
    return this.http.put(`http://localhost:8080/api/patient/profile/${id}`, profile);
  }
  getPatientHistory(userId: number): Observable<any> {
    return this.http.get(`http://localhost:8080/api/patient/history/${userId}`);
  }

  // Doctor Search Methods
  searchDoctors(specialization?: string): Observable<any> {
    const params = specialization ? { specialization } : {};
    return this.http.get(`${this.DOCTOR_API_URL}/search`, { params: params as any });
  }

  // AI Consult Methods
  aiConsult(question: string): Observable<any> {
    const body = { patientId: this.getUserId(), question };
    return this.http.post(`${this.AI_API_URL}/consult`, body);
  }

  aiHistory(): Observable<any> {
    const userId = this.getUserId();
    return this.http.get(`${this.AI_API_URL}/history/${userId}`);
  }

  // Medication History CRUD
  listMedications(patientId: number): Observable<any> {
    return this.http.get(`${this.MEDS_API_URL}/${patientId}`);
  }

  addMedication(patientId: number, payload: any): Observable<any> {
    return this.http.post(`${this.MEDS_API_URL}/${patientId}`, payload);
  }

  updateMedication(id: number, payload: any): Observable<any> {
    return this.http.put(`${this.MEDS_API_URL}/${id}`, payload);
  }

  deleteMedication(id: number): Observable<any> {
    return this.http.delete(`${this.MEDS_API_URL}/${id}`);
  }
}