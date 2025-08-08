import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MessageService } from 'primeng/api';
import { Router } from '@angular/router';
import { AuthService, ApiResponse } from '../auth.service';

@Component({
  selector: 'app-signup',
  templateUrl: './signup.component.html',
  styleUrls: ['./signup.component.css']
})
export class SignupComponent implements OnInit {
  signupForm!: FormGroup;
  loading = false;
  roles = [
    { label: 'Patient', value: 'PATIENT' },
    { label: 'Doctor', value: 'DOCTOR' },
    { label: 'Pharmacist', value: 'PHARMACIST' }
  ];

  constructor(
    private fb: FormBuilder, 
    private messageService: MessageService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.signupForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [
        Validators.required,
        Validators.minLength(8)
      ]],
      confirmPassword: ['', Validators.required],
      role: ['', Validators.required]
    }, {
      validators: this.passwordMatchValidator
    });
  }

  passwordMatchValidator(form: FormGroup) {
    return form.get('password')?.value === form.get('confirmPassword')?.value
      ? null : { mismatch: true };
  }

  testConnection() {
    // First test basic health
    this.authService.healthCheck().subscribe({
      next: (response: ApiResponse) => {
        console.log('Health check response:', response);
        this.messageService.add({
          severity: 'success',
          summary: 'Health Check',
          detail: response.message || 'Server is running!'
        });
        
        // Then test auth endpoint
        this.authService.testConnection().subscribe({
          next: (testResponse: ApiResponse) => {
            console.log('Auth test response:', testResponse);
            this.messageService.add({
              severity: 'success',
              summary: 'Auth Test',
              detail: testResponse.message || 'Auth endpoint is working!'
            });
          },
          error: (testError) => {
            console.error('Auth test error:', testError);
            this.messageService.add({
              severity: 'error',
              summary: 'Auth Test',
              detail: 'Auth endpoint error: ' + testError.message
            });
          }
        });
      },
      error: (error) => {
        console.error('Health check error:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Health Check',
          detail: 'Server is not accessible: ' + error.message
        });
      }
    });
  }

  onSubmit() {
    if (this.signupForm.valid) {
      this.loading = true;
      const userData = {
        username: this.signupForm.get('username')?.value,
        email: this.signupForm.get('email')?.value,
        password: this.signupForm.get('password')?.value,
        role: this.signupForm.get('role')?.value
      };

      console.log('Submitting registration with data:', userData);

      this.authService.register(userData).subscribe({
        next: (response: ApiResponse) => {
          this.loading = false;
          
          // Check if response has a message property (JSON response)
          const successMessage = response.message || 'Your account has been created! Please login.';
          
          this.messageService.add({
            severity: 'success',
            summary: 'Signup Successful',
            detail: successMessage
          });
          
          // Clear the form
          this.signupForm.reset();
          
          // Redirect to login for all users
          // Pharmacists will be redirected to profile creation after login
          setTimeout(() => {
            this.router.navigate(['/auth/login']);
          }, 2000);
        },
        error: (error: any) => {
          this.loading = false;
          console.error('Registration error:', error);
          let errorMessage = 'Registration failed. Please try again.';
          
          // Handle different types of error responses
          if (error.error) {
            if (typeof error.error === 'string') {
              errorMessage = error.error;
            } else if (error.error.error) {
              errorMessage = error.error.error;
            } else if (error.error.message) {
              errorMessage = error.error.message;
            }
          } else if (error.message) {
            errorMessage = error.message;
          }
          
          this.messageService.add({
            severity: 'error',
            summary: 'Signup Failed',
            detail: errorMessage
          });
        }
      });
    } else {
      this.signupForm.markAllAsTouched();
      const passwordControl = this.signupForm.get('password');
      if (passwordControl?.errors?.['insecurePassword']) {
        this.messageService.add({
          severity: 'error',
          summary: 'Weak Password',
          detail: 'Password must have:\n- 8+ characters\n- Uppercase & lowercase\n- Number\n- Special character'
        });
      }
    }
  }
}