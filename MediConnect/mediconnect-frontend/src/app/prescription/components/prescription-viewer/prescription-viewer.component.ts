import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MessageService } from 'primeng/api';
import { PrescriptionService } from '../../services/prescription.service';
import { Prescription } from '../../models/prescription.model';

@Component({
  selector: 'app-prescription-viewer',
  templateUrl: './prescription-viewer.component.html',
  styleUrls: ['./prescription-viewer.component.css'],
  providers: [MessageService]
})
export class PrescriptionViewerComponent implements OnInit {
  prescription?: Prescription;
  prescriptionId!: number;
  loading = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    public prescriptionService: PrescriptionService,
    private messageService: MessageService
  ) {}

  ngOnInit(): void {
    this.prescriptionId = Number(this.route.snapshot.paramMap.get('prescriptionId'));
    if (!this.prescriptionId) {
      this.messageService.add({
        severity: 'error',
        summary: 'Error',
        detail: 'Invalid prescription ID'
      });
      this.router.navigate(['/prescription/list']);
      return;
    }
    
    this.loadPrescription();
  }

  loadPrescription(): void {
    this.loading = true;
    
    this.prescriptionService.getPrescriptionById(this.prescriptionId).subscribe({
      next: (prescription) => {
        this.prescription = prescription;
        this.loading = false;
      },
      error: (error) => {
        this.loading = false;
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: error.error?.message || 'Failed to load prescription'
        });
        this.router.navigate(['/prescription/list']);
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/prescription/list']);
  }

  orderMedicines(): void {
    if (this.prescription?.id) {
      this.router.navigate(['/prescription/order', this.prescription.id]);
    }
  }

  formatDate(date: Date | string | undefined): string {
    if (!date) return 'N/A';
    return new Date(date).toLocaleDateString('en-IN', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  }

  getDoctorName(): string {
    if (this.prescription?.doctor) {
      return `Dr. ${this.prescription.doctor.firstName} ${this.prescription.doctor.lastName}`;
    }
    return 'Unknown Doctor';
  }

  openDocument(): void {
    if (this.prescription?.prescriptionImageUrl) {
      window.open('http://localhost:8080' + this.prescription.prescriptionImageUrl, '_blank');
    }
  }
}
