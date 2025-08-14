import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MessageService } from 'primeng/api';
import { AuthService } from '../auth.service';

@Component({
  selector: 'app-verify-doctor',
  templateUrl: './verify-doctor.component.html',
  styleUrls: ['./verify-doctor.component.css']
})
export class VerifyDoctorComponent implements OnInit {
  verificationForm: FormGroup;
  isLoading = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private messageService: MessageService,
    private router: Router
  ) {
    this.verificationForm = this.fb.group({
      fullName: ['', Validators.required],
      registrationNumber: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    // Check if user is coming from signup flow
    const tempUserRole = localStorage.getItem('tempUserRole');
    if (tempUserRole !== 'DOCTOR') {
      this.router.navigate(['/auth/login']);
    }
  }

  onSubmit(): void {
    if (this.verificationForm.valid) {
      this.isLoading = true;
      const formData = this.verificationForm.value;
      
      // For now, we'll simulate verification since we don't have the actual user ID yet
      // In a real scenario, you'd need to get the user ID from the signup response
      const verificationData = {
        fullName: formData.fullName,
        registrationNumber: formData.registrationNumber,
        userId: 0 // This will be updated after login
      };

      this.authService.verifyDoctor(verificationData).subscribe({
        next: (response: any) => {
          if (response.verified) {
            this.messageService.add({
              severity: 'success',
              summary: 'Verification Successful',
              detail: 'Your doctor credentials have been verified successfully! Please login to continue.'
            });
            
            // Store verification data for later use during profile creation
            localStorage.setItem('verifiedDoctorName', formData.fullName);
            localStorage.setItem('verifiedRegistrationNumber', formData.registrationNumber);
            localStorage.setItem('doctorVerificationStatus', 'true');
            
            // Clear temp data and redirect to login
            localStorage.removeItem('tempUserRole');
            localStorage.removeItem('tempUsername');
            localStorage.removeItem('tempEmail');
            
            setTimeout(() => {
              this.router.navigate(['/auth/login']);
            }, 2000);
          } else {
            this.messageService.add({
              severity: 'error',
              summary: 'Verification Failed',
              detail: 'Unable to verify your credentials. Please check your information and try again.'
            });
          }
        },
        error: (error) => {
          console.error('Verification error:', error);
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: 'An error occurred during verification. Please try again.'
          });
        },
        complete: () => {
          this.isLoading = false;
        }
      });
    } else {
      this.messageService.add({
        severity: 'warn',
        summary: 'Warning',
        detail: 'Please fill in all required fields'
      });
    }
  }

  goBack(): void {
    this.router.navigate(['/auth/login']);
  }
}