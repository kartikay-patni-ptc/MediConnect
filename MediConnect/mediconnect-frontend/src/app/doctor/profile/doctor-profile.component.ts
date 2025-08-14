import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MessageService } from 'primeng/api';
import { Router } from '@angular/router';
import { AuthService } from '../../auth/auth.service';

@Component({
  selector: 'app-doctor-profile',
  templateUrl: './doctor-profile.component.html',
  styleUrls: ['./doctor-profile.component.css']
})
export class DoctorProfileComponent implements OnInit {
  profileForm!: FormGroup;
  loading = false;
  currentUser: any;

  constructor(
    private fb: FormBuilder,
    private messageService: MessageService,
    private authService: AuthService,
    public router: Router
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
    this.initForm();
    this.loadVerificationData();
  }

  initForm() {
    this.profileForm = this.fb.group({
      firstName: ['', [Validators.required, Validators.minLength(2)]],
      lastName: ['', [Validators.required, Validators.minLength(2)]],
      specialization: ['', [Validators.required]],
      licenseNumber: ['', [Validators.required]],
      phoneNumber: ['', [Validators.required, Validators.pattern(/^[0-9]{10}$/)]],
      email: ['', [Validators.required, Validators.email]],
      experience: ['', [Validators.required, Validators.min(0)]],
      education: ['', [Validators.required]],
      hospital: ['', [Validators.required]],
      address: ['', [Validators.required, Validators.minLength(10)]],
      description: ['', [Validators.required, Validators.minLength(20)]]
    });
  }

  loadVerificationData() {
    // Check if doctor was verified during signup
    const verifiedDoctorName = localStorage.getItem('verifiedDoctorName');
    const verifiedRegistrationNumber = localStorage.getItem('verifiedRegistrationNumber');
    
    if (verifiedDoctorName && verifiedRegistrationNumber) {
      console.log('Loading verification data:', { verifiedDoctorName, verifiedRegistrationNumber });
      
      // Split the full name into first and last name
      const nameParts = verifiedDoctorName.trim().split(' ');
      if (nameParts.length >= 2) {
        const firstName = nameParts[0];
        const lastName = nameParts.slice(1).join(' '); // Handle multiple last names
        
        this.profileForm.patchValue({
          firstName: firstName,
          lastName: lastName,
          licenseNumber: verifiedRegistrationNumber
        });
        
        console.log('Pre-filled form with verification data');
      }
    }
  }

  onSubmit() {
    if (this.profileForm.valid) {
      this.loading = true;
      
      const profileData = {
        ...this.profileForm.value,
        userId: this.currentUser.userId
      };

      this.authService.createDoctorProfile(profileData).subscribe({
        next: (response: any) => {
          this.loading = false;
          
          // Clear verification data after successful profile creation
          localStorage.removeItem('verifiedDoctorName');
          localStorage.removeItem('verifiedRegistrationNumber');
          localStorage.removeItem('doctorVerificationStatus');
          
          this.messageService.add({
            severity: 'success',
            summary: 'Profile Created',
            detail: 'Doctor profile created successfully!'
          });
          
          setTimeout(() => {
            this.router.navigate(['/doctor/dashboard']);
          }, 2000);
        },
        error: (error: any) => {
          this.loading = false;
          
          let errorMessage = 'Failed to create profile. Please try again.';
          if (error.error?.message) {
            errorMessage = error.error.message;
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
      this.messageService.add({
        severity: 'warn',
        summary: 'Form Validation',
        detail: 'Please fill in all required fields correctly.'
      });
    }
  }
}