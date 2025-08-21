import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MessageService, ConfirmationService } from 'primeng/api';
import { PrescriptionService } from '../../services/prescription.service';
import { 
  Prescription, 
  PharmacyStore, 
  CreateOrderRequest,
  DeliveryEstimate,
  PrescriptionStatus 
} from '../../models/prescription.model';

@Component({
  selector: 'app-medicine-order',
  templateUrl: './medicine-order.component.html',
  styleUrls: ['./medicine-order.component.css'],
  providers: [MessageService, ConfirmationService]
})
export class MedicineOrderComponent implements OnInit {
  prescriptionId!: number;
  prescription?: Prescription;
  orderForm!: FormGroup;
  
  loading = false;
  loadingPharmacies = false;
  creatingOrder = false;
  
  nearbyPharmacies: PharmacyStore[] = [];
  selectedPharmacy?: PharmacyStore;
  deliveryEstimate?: DeliveryEstimate;
  
  currentStep = 1;
  totalSteps = 4;
  
  patientId!: number;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private fb: FormBuilder,
    private prescriptionService: PrescriptionService,
    private messageService: MessageService,
    private confirmationService: ConfirmationService
  ) {
    this.initializeForm();
  }

  ngOnInit(): void {
    this.loadPatientInfo();
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

  private loadPatientInfo(): void {
    // Try to get patient ID from userInfo first, but we'll also get it from prescription
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
    this.patientId = userInfo.id || 0;
  }

  private initializeForm(): void {
    this.orderForm = this.fb.group({
      deliveryAddress: ['', [Validators.required, Validators.minLength(10)]],
      deliveryPincode: ['', [Validators.required, Validators.pattern(/^\d{6}$/)]],
      specialInstructions: [''],
      confirmTerms: [false, Validators.requiredTrue]
    });
  }

  loadPrescription(): void {
    this.loading = true;
    
    this.prescriptionService.getPrescriptionById(this.prescriptionId).subscribe({
      next: (prescription) => {
        this.prescription = prescription;
        this.loading = false;
        
        // Set patient ID from prescription data
        console.log('Prescription data:', prescription);
        console.log('Prescription patient:', prescription.patient);
        console.log('Prescription appointment:', prescription.appointment);
        
        if (prescription.patient?.id) {
          this.patientId = prescription.patient.id;
          console.log('Set patientId from prescription.patient.id:', this.patientId);
        } else if (prescription.appointment?.patient?.id) {
          this.patientId = prescription.appointment.patient.id;
          console.log('Set patientId from prescription.appointment.patient.id:', this.patientId);
        } else {
          console.log('Could not find patient ID in prescription data');
          // Fallback: try to get from userInfo
          const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
          if (userInfo.id && userInfo.role === 'PATIENT') {
            this.patientId = userInfo.id;
            console.log('Fallback: Set patientId from userInfo:', this.patientId);
          }
        }
        
        // Validate prescription can be used for ordering
        if (prescription.status !== PrescriptionStatus.ACTIVE) {
          this.messageService.add({
            severity: 'warn',
            summary: 'Invalid Prescription',
            detail: 'Only active prescriptions can be used to order medicines'
          });
          this.router.navigate(['/prescription/list']);
          return;
        }
        
        // Check if prescription is expired
        if (prescription.validUntil && new Date(prescription.validUntil) < new Date()) {
          this.messageService.add({
            severity: 'warn',
            summary: 'Expired Prescription',
            detail: 'This prescription has expired and cannot be used to order medicines'
          });
          this.router.navigate(['/prescription/list']);
          return;
        }
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

  onPincodeChange(): void {
    const pincode = this.orderForm.get('deliveryPincode')?.value;
    if (pincode && pincode.length === 6) {
      this.loadNearbyPharmacies(pincode);
    } else {
      this.nearbyPharmacies = [];
      this.selectedPharmacy = undefined;
      this.deliveryEstimate = undefined;
    }
  }

  loadNearbyPharmacies(pincode: string): void {
    this.loadingPharmacies = true;
    this.nearbyPharmacies = [];
    this.selectedPharmacy = undefined;
    
    this.prescriptionService.getNearbyPharmacies(pincode, 20).subscribe({
      next: (pharmacies) => {
        this.nearbyPharmacies = pharmacies;
        this.loadingPharmacies = false;
        
        if (pharmacies.length === 0) {
          this.messageService.add({
            severity: 'warn',
            summary: 'No Pharmacies',
            detail: 'No pharmacies found in your area. Please try a different pincode.'
          });
        }
      },
      error: (error) => {
        this.loadingPharmacies = false;
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: error.error?.message || 'Failed to load nearby pharmacies'
        });
      }
    });
  }

  selectPharmacy(pharmacy: PharmacyStore): void {
    this.selectedPharmacy = pharmacy;
    
    const pincode = this.orderForm.get('deliveryPincode')?.value;
    if (pincode && pharmacy.id) {
      this.getDeliveryEstimate(pincode, pharmacy.id);
    }
  }

  getDeliveryEstimate(pincode: string, pharmacyId: number): void {
    this.prescriptionService.getDeliveryEstimate(pincode, pharmacyId).subscribe({
      next: (estimate) => {
        this.deliveryEstimate = estimate;
      },
      error: (error) => {
        this.messageService.add({
          severity: 'warn',
          summary: 'Estimate Unavailable',
          detail: 'Could not calculate delivery estimate'
        });
      }
    });
  }

  nextStep(): void {
    if (this.currentStep === 1 && this.orderForm.invalid) {
      this.markFormGroupTouched(this.orderForm);
      this.messageService.add({
        severity: 'warn',
        summary: 'Validation Error',
        detail: 'Please fill in all required fields correctly'
      });
      return;
    }
    
    if (this.currentStep === 2 && !this.selectedPharmacy) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Selection Required',
        detail: 'Please select a pharmacy to continue'
      });
      return;
    }
    
    if (this.currentStep < this.totalSteps) {
      this.currentStep++;
    }
  }

  previousStep(): void {
    if (this.currentStep > 1) {
      this.currentStep--;
    }
  }

  createOrder(): void {
    if (!this.prescription || !this.selectedPharmacy || this.orderForm.invalid) {
      this.messageService.add({
        severity: 'error',
        summary: 'Invalid Order',
        detail: 'Please complete all required fields'
      });
      return;
    }

    this.confirmationService.confirm({
      message: 'Are you sure you want to place this medicine order?',
      header: 'Confirm Order',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.submitOrder();
      }
    });
  }

  private submitOrder(): void {
    this.creatingOrder = true;
    
    const formValue = this.orderForm.value;
    
    // Debug: Log the values being sent
    console.log('Creating order with:', {
      prescriptionId: this.prescriptionId,
      patientId: this.patientId,
      prescription: this.prescription,
      userInfo: JSON.parse(localStorage.getItem('userInfo') || '{}')
    });
    
    // Validate required data
    if (!this.prescriptionId || this.prescriptionId <= 0) {
      this.messageService.add({
        severity: 'error',
        summary: 'Invalid Prescription',
        detail: 'Prescription ID is invalid'
      });
      this.creatingOrder = false;
      return;
    }
    
    if (!this.patientId || this.patientId <= 0) {
      this.messageService.add({
        severity: 'error',
        summary: 'Invalid Patient',
        detail: 'Patient ID is invalid. Please refresh the page and try again.'
      });
      this.creatingOrder = false;
      return;
    }
    
    const request: CreateOrderRequest = {
      prescriptionId: this.prescriptionId,
      patientId: this.patientId,
      deliveryAddress: formValue.deliveryAddress,
      deliveryPincode: formValue.deliveryPincode,
      specialInstructions: formValue.specialInstructions
    };

    this.prescriptionService.createMedicineOrder(request).subscribe({
      next: (response) => {
        this.creatingOrder = false;
        this.currentStep = 4; // Go to success step
        
        this.messageService.add({
          severity: 'success',
          summary: 'Order Created',
          detail: `Your order ${response.orderNumber} has been created successfully!`
        });
      },
      error: (error) => {
        this.creatingOrder = false;
        this.messageService.add({
          severity: 'error',
          summary: 'Order Failed',
          detail: error.error?.message || 'Failed to create medicine order'
        });
      }
    });
  }

  goToOrderTracking(): void {
    // Navigate to order tracking - would need the order number from response
    this.router.navigate(['/prescription/list']);
  }

  goToPrescriptionList(): void {
    this.router.navigate(['/prescription/list']);
  }

  private markFormGroupTouched(formGroup: FormGroup): void {
    Object.keys(formGroup.controls).forEach(key => {
      const control = formGroup.get(key);
      if (control instanceof FormGroup) {
        this.markFormGroupTouched(control);
      } else {
        control?.markAsTouched();
      }
    });
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.orderForm.get(fieldName);
    return !!(field?.invalid && field?.touched);
  }

  getFieldError(fieldName: string): string {
    const field = this.orderForm.get(fieldName);
    
    if (field?.errors) {
      if (field.errors['required']) return `${fieldName} is required`;
      if (field.errors['minlength']) return `${fieldName} is too short`;
      if (field.errors['pattern']) return `Invalid ${fieldName} format`;
      if (field.errors['requiredTrue']) return 'You must agree to the terms and conditions';
    }
    return '';
  }

  getTotalMedicines(): number {
    return this.prescription?.medicines?.length || 0;
  }

  getEstimatedTotal(): number {
    // This is a simple calculation - in reality, would be calculated by pharmacy
    const baseAmount = (this.prescription?.medicines?.length || 0) * 200; // ₹200 per medicine average
    const deliveryFee = this.deliveryEstimate?.deliveryFee || 20;
    return baseAmount + deliveryFee;
  }

  formatDistance(km: number): string {
    return this.prescriptionService.formatDistance(km);
  }

  formatDuration(minutes: number): string {
    return this.prescriptionService.formatDuration(minutes);
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

  getMedicineInstructions(medicine: any): string {
    return `${medicine.frequency} for ${medicine.duration}`;
  }
}
