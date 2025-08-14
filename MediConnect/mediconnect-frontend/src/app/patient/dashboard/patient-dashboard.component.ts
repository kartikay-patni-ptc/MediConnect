import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { MessageService } from 'primeng/api';
import { AuthService } from '../../auth/auth.service';

interface Doctor {
  id: number;
  firstName: string;
  lastName: string;
  specialization: string;
  experience: number;
  hospital: string;
  phoneNumber: string;
  email: string;
}

interface Prescription {
  id: number;
  doctorName: string;
  date: string;
  medications: string[];
  diagnosis: string;
  notes: string;
}

@Component({
  selector: 'app-patient-dashboard',
  templateUrl: './patient-dashboard.component.html',
  styleUrls: ['./patient-dashboard.component.css']
})
export class PatientDashboardComponent implements OnInit {
  patient: any = null;
  doctors: Doctor[] = [];
  filteredDoctors: Doctor[] = [];
  prescriptions: Prescription[] = [];
  searchTerm: string = '';
  isLoading = false;

  constructor(
    private authService: AuthService,
    private messageService: MessageService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadDashboard();
    // Don't load all doctors initially - we'll search from database when needed
  }

  loadDashboard(): void {
    const userId = this.authService.getUserId();
    if (userId) {
      this.isLoading = true;
      this.authService.getPatientDashboard(userId).subscribe({
        next: (response: any) => {
          this.patient = response.patient;
          
          // Load doctors from dashboard data
          if (response.doctors) {
            this.doctors = response.doctors;
            this.filteredDoctors = [...this.doctors];
          }
          
          // Load prescriptions from dashboard data
          if (response.prescriptions) {
            this.prescriptions = response.prescriptions.map((prescription: any) => ({
              id: prescription.id,
              doctorName: prescription.doctorName,
              date: prescription.date,
              medications: prescription.medications,
              diagnosis: prescription.diagnosis,
              notes: prescription.notes
            }));
          }
        },
        error: (error) => {
          console.error('Error loading dashboard:', error);
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: 'Failed to load dashboard'
          });
        },
        complete: () => {
          this.isLoading = false;
        }
      });
    }
  }



  loadPrescriptions(): void {
    // This will be loaded from the dashboard data
    // Keeping empty for now as it's loaded in loadDashboard()
  }

  searchDoctors(): void {
    console.log('Searching for:', this.searchTerm);
    
    if (!this.searchTerm.trim()) {
      // If no search term, clear results
      this.filteredDoctors = [];
      return;
    }
    
    const searchTerm = this.searchTerm.trim();
    
    // Always search from backend database
    this.searchDoctorsFromDatabase(searchTerm);
  }
  
  private searchDoctorsFromDatabase(searchTerm: string): void {
    this.isLoading = true;
    console.log('Searching doctors from database for:', searchTerm);
    
    // Call backend API to search doctors
    this.authService.searchDoctors(searchTerm).subscribe({
      next: (response: any) => {
        this.filteredDoctors = response;
        console.log(`Found ${this.filteredDoctors.length} doctors from database for: ${searchTerm}`);
        
        // Show success message if doctors found
        if (this.filteredDoctors.length > 0) {
          this.messageService.add({
            severity: 'success',
            summary: 'Search Results',
            detail: `Found ${this.filteredDoctors.length} doctor(s) matching "${searchTerm}"`
          });
        } else {
          this.messageService.add({
            severity: 'info',
            summary: 'No Results',
            detail: `No doctors found matching "${searchTerm}"`
          });
        }
      },
      error: (error) => {
        console.error('Error searching doctors from database:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Search Error',
          detail: 'Failed to search doctors. Please try again.'
        });
        this.filteredDoctors = [];
      },
      complete: () => {
        this.isLoading = false;
      }
    });
  }

  bookAppointment(doctor: Doctor): void {
    this.messageService.add({
      severity: 'info',
      summary: 'Info',
      detail: `Appointment booking feature coming soon for Dr. ${doctor.firstName} ${doctor.lastName}`
    });
  }

  downloadPrescription(prescription: Prescription): void {
    // Mock PDF download functionality
    this.messageService.add({
      severity: 'success',
      summary: 'Success',
      detail: `Downloading prescription from ${prescription.date}`
    });
  }

  editProfile(): void {
    this.router.navigate(['/patient/profile']);
  }

  logout(): void {
    this.authService.logout();
  }
  
  clearSearch(): void {
    this.searchTerm = '';
    this.filteredDoctors = [];
  }
}