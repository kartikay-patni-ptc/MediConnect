import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MessageService } from 'primeng/api';
import { Router } from '@angular/router';
import { AuthService } from '../../auth/auth.service';

@Component({
  selector: 'app-patient-profile',
  templateUrl: './patient-profile.component.html',
  styleUrls: ['./patient-profile.component.css']
})
export class PatientProfileComponent implements OnInit {
  profileForm!: FormGroup;
  loading = false;
  currentUser: any;

  constructor(
    private fb: FormBuilder,
    private messageService: MessageService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
    this.initForm();
    
    // Test API connectivity
    this.testApiConnection();
  }

  testApiConnection() {
    console.log('Testing API connection...');
    this.authService.healthCheck().subscribe({
      next: (response) => {
        console.log('API health check successful:', response);
      },
      error: (error) => {
        console.error('API health check failed:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Connection Error',
          detail: 'Cannot connect to server. Please check if the backend is running.'
        });
      }
    });
  }

  initForm() {
    this.profileForm = this.fb.group({
      firstName: ['', [Validators.required, Validators.minLength(2)]],
      lastName: ['', [Validators.required, Validators.minLength(2)]],
      phoneNumber: ['', [Validators.required, Validators.pattern(/^[0-9]{10}$/)]],
      email: ['', [Validators.required, Validators.email]],
      dateOfBirth: ['', [Validators.required]],
      gender: ['', [Validators.required]],
      address: ['', [Validators.required, Validators.minLength(10)]],
      emergencyContact: [''],
      medicalHistory: ['']
    });
  }

  onSubmit() {
    console.log('Form submission started');
    console.log('Form valid:', this.profileForm.valid);
    console.log('Form values:', this.profileForm.value);
    console.log('Current user:', this.currentUser);
    
    if (this.profileForm.valid) {
      this.loading = true;
      
      const formData = this.profileForm.value;
      const profileData = {
        ...formData,
        userId: this.currentUser.userId,
        dateOfBirth: formData.dateOfBirth.toISOString().split('T')[0]
      };

      console.log('Submitting profile data:', profileData);

      this.authService.createPatientProfile(profileData).subscribe({
        next: (response: any) => {
          this.loading = false;
          console.log('Profile creation response:', response);
          
          this.messageService.add({
            severity: 'success',
            summary: 'Profile Created',
            detail: 'Patient profile created successfully!'
          });
          
          // Redirect to dashboard after 2 seconds
          setTimeout(() => {
            this.router.navigate(['/patient/dashboard']);
          }, 2000);
        },
        error: (error: any) => {
          this.loading = false;
          console.error('Profile creation error:', error);
          console.error('Error details:', {
            status: error.status,
            statusText: error.statusText,
            error: error.error,
            message: error.message
          });
          
          let errorMessage = 'Failed to create profile. Please try again.';
          
          if (error.error) {
            if (typeof error.error === 'string') {
              errorMessage = error.error;
            } else if (error.error.message) {
              errorMessage = error.error.message;
            }
          } else if (error.message) {
            errorMessage = error.message;
          }
          
          this.messageService.add({
            severity: 'error',
            summary: 'Profile Creation Failed',
            detail: errorMessage
          });
        }
      });
    } else {
      this.profileForm.markAllAsTouched();
      console.log('Form validation errors:', this.profileForm.errors);
      this.messageService.add({
        severity: 'warn',
        summary: 'Form Validation',
        detail: 'Please fill in all required fields correctly.'
      });
    }
  }
}