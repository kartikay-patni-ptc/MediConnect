import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { MessageService } from 'primeng/api';
import { PrescriptionService } from '../../services/prescription.service';
import { Prescription, PrescriptionStatus } from '../../models/prescription.model';
import { AuthService } from '../../../auth/auth.service';

@Component({
  selector: 'app-prescription-list',
  templateUrl: './prescription-list.component.html',
  styleUrls: ['./prescription-list.component.css'],
  providers: [MessageService]
})
export class PrescriptionListComponent implements OnInit {
  prescriptions: Prescription[] = [];
  loading = false;
  userRole!: string;
  userId!: number;
  
  // Filter options
  statusFilter: PrescriptionStatus | null = null;
  searchTerm = '';
  
  // Table configuration
  first = 0;
  rows = 10;
  totalRecords = 0;
  
  statusOptions = [
    { label: 'All Prescriptions', value: null },
    { label: 'Active', value: PrescriptionStatus.ACTIVE },
    { label: 'Expired', value: PrescriptionStatus.EXPIRED },
    { label: 'Cancelled', value: PrescriptionStatus.CANCELLED },
    { label: 'Completed', value: PrescriptionStatus.COMPLETED }
  ];

  constructor(
    private router: Router,
    private prescriptionService: PrescriptionService,
    private messageService: MessageService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadUserInfo();
    this.loadPrescriptions();
  }

  private loadUserInfo(): void {
    // Get user info from auth service instead of localStorage
    const currentUser = this.authService.getCurrentUser();
    this.userRole = currentUser.role || 'PATIENT';
    this.userId = currentUser.userId || 0;
    
    // Validate user ID
    if (this.userId === 0) {
      console.error('Invalid user ID:', this.userId);
      this.messageService.add({
        severity: 'error',
        summary: 'Authentication Error',
        detail: 'User not properly authenticated. Please login again.'
      });
      this.router.navigate(['/login']);
      return;
    }
  }

  loadPrescriptions(): void {
    this.loading = true;
    
    let prescriptionObservable;
    if (this.userRole === 'DOCTOR') {
      prescriptionObservable = this.prescriptionService.getDoctorPrescriptions(this.userId);
    } else {
      prescriptionObservable = this.prescriptionService.getPatientPrescriptions(this.userId);
    }

    prescriptionObservable.subscribe({
      next: (prescriptions) => {
        this.prescriptions = prescriptions;
        this.totalRecords = prescriptions.length;
        this.loading = false;
      },
      error: (error) => {
        this.loading = false;
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: error.error?.message || 'Failed to load prescriptions'
        });
      }
    });
  }

  get filteredPrescriptions(): Prescription[] {
    let filtered = this.prescriptions;

    // Apply status filter
    if (this.statusFilter) {
      filtered = filtered.filter(p => p.status === this.statusFilter);
    }

    // Apply search filter
    if (this.searchTerm) {
      const term = this.searchTerm.toLowerCase();
      filtered = filtered.filter(p => 
        p.prescriptionNumber?.toLowerCase().includes(term) ||
        p.diagnosis.toLowerCase().includes(term) ||
        p.symptoms.toLowerCase().includes(term) ||
        p.doctor?.firstName?.toLowerCase().includes(term) ||
        p.doctor?.lastName?.toLowerCase().includes(term) ||
        p.patient?.firstName?.toLowerCase().includes(term) ||
        p.patient?.lastName?.toLowerCase().includes(term)
      );
    }

    return filtered;
  }

  viewPrescription(prescription: Prescription): void {
    this.router.navigate(['/prescription/view', prescription.id]);
  }

  createPrescription(): void {
    // This would typically be called from an appointment context
    this.messageService.add({
      severity: 'info',
      summary: 'Info',
      detail: 'Please create prescriptions from the appointment details'
    });
  }

  orderMedicines(prescription: Prescription): void {
    if (prescription.status !== PrescriptionStatus.ACTIVE) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Warning',
        detail: 'Only active prescriptions can be used to order medicines'
      });
      return;
    }
    
    this.router.navigate(['/prescription/order', prescription.id]);
  }

  downloadPrescription(prescription: Prescription): void {
    if (prescription.prescriptionImageUrl) {
      // Open the prescription file in a new tab
      window.open(`http://localhost:8080${prescription.prescriptionImageUrl}`, '_blank');
    } else {
      this.messageService.add({
        severity: 'warn',
        summary: 'Warning',
        detail: 'No prescription file available for download'
      });
    }
  }

getStatusSeverity(status: string): 'success' | 'info' | 'warning' | 'danger' {
  switch (status.toLowerCase()) {
    case 'delivered':
    case 'completed':
      return 'success';
    case 'pending':
      return 'warning';
    case 'cancelled':
    case 'failed':
      return 'danger';
    default:
      return 'info';
  }
}

  getStatusIcon(status: PrescriptionStatus): string {
    return this.prescriptionService.getStatusIcon(status);
  }

  onPageChange(event: any): void {
    this.first = event.first;
    this.rows = event.rows;
  }

  onFilterChange(): void {
    this.first = 0; // Reset to first page when filtering
  }

  refreshList(): void {
    this.loadPrescriptions();
    this.messageService.add({
      severity: 'success',
      summary: 'Refreshed',
      detail: 'Prescription list refreshed successfully'
    });
  }

  formatDate(date: Date | string | undefined): string {
    if (!date) return 'N/A';
    return new Date(date).toLocaleDateString('en-IN', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  }

  formatDateTime(date: Date | string | undefined): string {
    if (!date) return 'N/A';
    return new Date(date).toLocaleString('en-IN', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  getDoctorName(prescription: Prescription): string {
    if (prescription.doctor) {
      return `Dr. ${prescription.doctor.firstName} ${prescription.doctor.lastName}`;
    }
    return 'Unknown Doctor';
  }

  getPatientName(prescription: Prescription): string {
    if (prescription.patient) {
      return `${prescription.patient.firstName} ${prescription.patient.lastName}`;
    }
    return 'Unknown Patient';
  }

  getMedicineCount(prescription: Prescription): number {
    return prescription.medicines?.length || 0;
  }

  isExpiringSoon(prescription: Prescription): boolean {
    if (!prescription.validUntil || prescription.status !== PrescriptionStatus.ACTIVE) {
      return false;
    }
    
    const validUntil = new Date(prescription.validUntil);
    const now = new Date();
    const daysUntilExpiry = Math.ceil((validUntil.getTime() - now.getTime()) / (1000 * 60 * 60 * 24));
    
    return daysUntilExpiry <= 30 && daysUntilExpiry > 0;
  }

  isExpired(prescription: Prescription): boolean {
    if (!prescription.validUntil) return false;
    
    const validUntil = new Date(prescription.validUntil);
    const now = new Date();
    
    return validUntil < now;
  }

  getDaysUntilExpiry(prescription: Prescription): number {
    if (!prescription.validUntil) return 0;
    
    const validUntil = new Date(prescription.validUntil);
    const now = new Date();
    
    return Math.ceil((validUntil.getTime() - now.getTime()) / (1000 * 60 * 60 * 24));
  }

  getActiveCount(): number {
    return this.prescriptions.filter(p => p.status === PrescriptionStatus.ACTIVE).length;
  }

  getExpiringSoonCount(): number {
    return this.prescriptions.filter(p => this.isExpiringSoon(p)).length;
  }

  getExpiredCount(): number {
    return this.prescriptions.filter(p => p.status === PrescriptionStatus.EXPIRED).length;
  }
}