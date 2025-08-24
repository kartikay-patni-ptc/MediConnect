import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { MessageService } from 'primeng/api';
import { AuthService } from '../../auth/auth.service';
import { BookAppointmentDialogComponent } from '../../appointment/book-appointment-dialog.component';
import { AppointmentService } from '../../appointment/appointment.service';
import { Doctor } from '../../appointment/appointment.model';
import { PrescriptionService } from '../../prescription/services/prescription.service';
import { Prescription as RealPrescription } from '../../prescription/models/prescription.model';



@Component({
  selector: 'app-patient-dashboard',
  templateUrl: './patient-dashboard.component.html',
  styleUrls: ['./patient-dashboard.component.css']
})
export class PatientDashboardComponent implements OnInit {
  patient: any = null;
  doctors: Doctor[] = [];
  filteredDoctors: Doctor[] = [];
  prescriptions: RealPrescription[] = [];
  searchTerm: string = '';
  isLoading = false;
  showBookingDialog = false;
  selectedDoctor: Doctor | null = null;
  patientId: number = 0;
  aiSummary: string | null = null;
  doctorSummary: string | null = null;
  patientAdvice: string | null = null;
  prescribedMedicines: string | null = null;
  riskLevel: string | null = null;
  redFlags: string | null = null;
  homeRemedies: string | null = null;
  specializationHint: string | null = null;

  constructor(
    private authService: AuthService,
    private messageService: MessageService,
    private router: Router,
    private appointmentService: AppointmentService,
    private prescriptionService: PrescriptionService
  ) {}

  ngOnInit(): void {
    this.patientId = this.authService.getUserId();
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
          
          // Load prescriptions using prescription service
          this.loadPrescriptions();
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

  bookAppointment(doctor: Doctor, aiData?: any): void {
    this.selectedDoctor = doctor;
    
    // Store AI response data if provided
    if (aiData) {
      this.aiSummary = aiData.answer || aiData.patientAdvice || null;
      this.doctorSummary = aiData.doctorSummary || null;
      this.patientAdvice = aiData.patientAdvice || null;
      this.prescribedMedicines = aiData.prescribedMedicines ? JSON.stringify(aiData.prescribedMedicines) : null;
      this.riskLevel = aiData.riskLevel || null;
      this.redFlags = aiData.redFlags ? JSON.stringify(aiData.redFlags) : null;
      this.homeRemedies = aiData.homeRemedies ? JSON.stringify(aiData.homeRemedies) : null;
      this.specializationHint = aiData.specializationHint || null;
    } else {
      // Clear AI data if not provided
      this.aiSummary = null;
      this.doctorSummary = null;
      this.patientAdvice = null;
      this.prescribedMedicines = null;
      this.riskLevel = null;
      this.redFlags = null;
      this.homeRemedies = null;
      this.specializationHint = null;
    }
    
    this.showBookingDialog = true;
  }

  onAppointmentBooked(appointment: any) {
    this.messageService.add({
      severity: 'success',
      summary: 'Appointment Booked',
      detail: 'Your appointment has been booked successfully.'
    });
    this.showBookingDialog = false;
    // Optionally refresh appointment list here
  }



  loadPrescriptions(): void {
    if (this.patientId && this.patientId > 0) {
      this.prescriptionService.getPatientPrescriptions(this.patientId).subscribe({
        next: (prescriptions) => {
          this.prescriptions = prescriptions;
        },
        error: (error) => {
          console.error('Failed to load prescriptions:', error);
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: 'Failed to load prescriptions'
          });
        }
      });
    } else {
      console.warn('Patient ID not available yet, skipping prescription load');
    }
  }

  downloadPrescription(prescription: RealPrescription): void {
    if (prescription.prescriptionImageUrl) {
      window.open('http://localhost:8080' + prescription.prescriptionImageUrl, '_blank');
    } else {
      this.messageService.add({
        severity: 'warn',
        summary: 'No File',
        detail: 'No prescription file available for download'
      });
    }
  }

  viewPrescription(prescription: RealPrescription): void {
    this.router.navigate(['/prescription/view', prescription.id]);
  }

  orderMedicines(prescription: RealPrescription): void {
    this.router.navigate(['/prescription/order', prescription.id]);
  }

  editProfile(): void {
    this.router.navigate(['/patient/profile']);
  }

    logout(): void {
    this.authService.logout();
    this.messageService.add({
      severity: 'success',
      summary: 'Logged Out',
      detail: 'You have been successfully logged out'
    });
    setTimeout(() => {
      this.router.navigate(['/home']);
    }, 1000);
  }
  
  
  clearSearch(): void {
    this.searchTerm = '';
    this.filteredDoctors = [];
  }
}