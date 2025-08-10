import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MessageService } from 'primeng/api';
import { Router } from '@angular/router';
import { AuthService } from '../auth.service';
import { catchError, switchMap } from 'rxjs/operators';
import { of } from 'rxjs';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
  loginForm!: FormGroup;
  loading = false;

  constructor(
    private fb: FormBuilder, 
    private messageService: MessageService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loginForm = this.fb.group({
      username: ['', [Validators.required]],
      password: Validators.required
    });
  }

  onSubmit() {
    if (this.loginForm.valid) {
      this.loading = true;
      const credentials = this.loginForm.value;
      
      this.authService.login(credentials).pipe(
        switchMap((response) => {
          this.messageService.add({
            severity: 'success',
            summary: 'Login Successful',
            detail: `Welcome back, ${response.username}! You are logged in as ${response.role}`
          });
          
          this.loginForm.reset();
          
          if (response.role === 'PHARMACIST') {
            return this.authService.getPharmacyProfile(response.userId).pipe(
              catchError(() => {
                setTimeout(() => {
                  this.router.navigate(['/pharmacist/profile']);
                }, 1500);
                return of(null);
              })
            );
          } else if (response.role === 'DOCTOR') {
            return this.authService.getDoctorProfile(response.userId).pipe(
              catchError(() => {
                setTimeout(() => {
                  this.router.navigate(['/doctor/profile']);
                }, 1500);
                return of(null);
              })
            );
          }
          return of(response);
        })
      ).subscribe({
        next: (response) => {
          this.loading = false;
          
          if (response && response.role === 'PHARMACIST') {
            setTimeout(() => {
              this.router.navigate(['/pharmacist/dashboard']);
            }, 1500);
          } else if (response && response.role === 'DOCTOR') {
            setTimeout(() => {
              this.router.navigate(['/doctor/dashboard']);
            }, 1500);
          } else if (response) {
            setTimeout(() => {
              if (response.role === 'PATIENT') {
                this.router.navigate(['/patient/dashboard']);
              } else {
                this.router.navigate(['/home']);
              }
            }, 1500);
          }
        },
        error: (error) => {
          this.loading = false;
          this.messageService.add({
            severity: 'error',
            summary: 'Login Failed',
            detail: error.error || 'Invalid username or password'
          });
        }
      });
    }
  }
}