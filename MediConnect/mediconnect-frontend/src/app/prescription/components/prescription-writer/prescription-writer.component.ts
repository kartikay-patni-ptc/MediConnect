import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, FormArray, Validators } from '@angular/forms';
import { MessageService, ConfirmationService } from 'primeng/api';
import { PrescriptionService } from '../../services/prescription.service';
import { 
  PrescriptionMedicine, 
  MedicineType, 
  CreatePrescriptionRequest 
} from '../../models/prescription.model';

@Component({
  selector: 'app-prescription-writer',
  templateUrl: './prescription-writer.component.html',
  styleUrls: ['./prescription-writer.component.css'],
  providers: [MessageService, ConfirmationService]
})
export class PrescriptionWriterComponent implements OnInit {
  prescriptionForm!: FormGroup;
  appointmentId!: number;
  loading = false;
  uploadingFile = false;
  showFileUpload = false;
  createdPrescriptionId?: number;

  medicineTypes = [
    { label: 'Prescription Medicine', value: MedicineType.PRESCRIPTION },
    { label: 'Over-the-Counter (OTC)', value: MedicineType.OTC },
    { label: 'Controlled Substance', value: MedicineType.CONTROLLED }
  ];

  frequencyOptions = [
    'Once daily',
    'Twice daily', 
    'Three times daily',
    'Four times daily',
    'Every 4 hours',
    'Every 6 hours',
    'Every 8 hours',
    'Every 12 hours',
    'As needed',
    'Before meals',
    'After meals',
    'At bedtime'
  ];

  durationOptions = [
    '3 days',
    '5 days',
    '7 days',
    '10 days',
    '14 days',
    '21 days',
    '1 month',
    '2 months',
    '3 months',
    'Until finished',
    'As directed'
  ];

  instructionOptions = [
    'Take with food',
    'Take on empty stomach',
    'Take with plenty of water',
    'Do not crush or chew',
    'Take before bedtime',
    'Take in the morning',
    'Avoid alcohol',
    'Complete the full course',
    'Store in refrigerator',
    'Keep away from sunlight'
  ];

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
    this.appointmentId = Number(this.route.snapshot.paramMap.get('appointmentId'));
    // Appointment ID is optional now - can write prescriptions without appointments
    if (this.appointmentId) {
      // Load appointment details if available
      this.loadAppointmentDetails();
    }
  }

  private loadAppointmentDetails(): void {
    // This would load appointment details if an appointment ID is provided
    // For now, we'll just set a flag
    console.log('Loading appointment details for ID:', this.appointmentId);
  }

  private initializeForm(): void {
    this.prescriptionForm = this.fb.group({
      diagnosis: ['', [Validators.required, Validators.minLength(10)]],
      symptoms: ['', [Validators.required, Validators.minLength(5)]],
      doctorNotes: ['', Validators.minLength(10)],
      medicines: this.fb.array([])
    });

    // Add initial medicine entry
    this.addMedicine();
  }

  get medicines(): FormArray {
    return this.prescriptionForm.get('medicines') as FormArray;
  }

  createMedicineFormGroup(): FormGroup {
    return this.fb.group({
      medicineName: ['', [Validators.required, Validators.minLength(2)]],
      genericName: ['', Validators.required],
      dosage: ['', [Validators.required, Validators.pattern(/^\d+\s*(mg|ml|g|mcg|units?)$/i)]],
      frequency: ['', Validators.required],
      duration: ['', Validators.required],
      quantity: [1, [Validators.required, Validators.min(1), Validators.max(1000)]],
      instructions: ['', Validators.required],
      medicineType: [MedicineType.PRESCRIPTION, Validators.required],
      specialInstructions: [''],
      sideEffects: [''],
      contraindications: ['']
    });
  }

  addMedicine(): void {
    this.medicines.push(this.createMedicineFormGroup());
  }

  removeMedicine(index: number): void {
    if (this.medicines.length > 1) {
      this.confirmationService.confirm({
        message: 'Are you sure you want to remove this medicine?',
        header: 'Confirm',
        icon: 'pi pi-exclamation-triangle',
        accept: () => {
          this.medicines.removeAt(index);
          this.messageService.add({
            severity: 'info',
            summary: 'Removed',
            detail: 'Medicine removed from prescription'
          });
        }
      });
    } else {
      this.messageService.add({
        severity: 'warn',
        summary: 'Warning',
        detail: 'At least one medicine is required'
      });
    }
  }

  duplicateMedicine(index: number): void {
    const medicine = this.medicines.at(index);
    const duplicated = this.createMedicineFormGroup();
    duplicated.patchValue(medicine.value);
    duplicated.get('medicineName')?.setValue(medicine.value.medicineName + ' (Copy)');
    this.medicines.push(duplicated);
    
    this.messageService.add({
      severity: 'success',
      summary: 'Duplicated',
      detail: 'Medicine duplicated successfully'
    });
  }

  onSubmit(): void {
    if (this.prescriptionForm.valid) {
      this.loading = true;
      
      const formValue = this.prescriptionForm.value;
      const request: CreatePrescriptionRequest = {
        appointmentId: this.appointmentId || undefined,
        diagnosis: formValue.diagnosis,
        symptoms: formValue.symptoms,
        doctorNotes: formValue.doctorNotes,
        medicines: formValue.medicines
      };

      this.prescriptionService.createPrescription(request).subscribe({
        next: (response) => {
          this.loading = false;
          this.createdPrescriptionId = response.prescriptionId;
          this.showFileUpload = true;
          
          this.messageService.add({
            severity: 'success',
            summary: 'Success',
            detail: `Prescription created successfully! Number: ${response.prescriptionNumber}`
          });
        },
        error: (error) => {
          this.loading = false;
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: error.error?.message || 'Failed to create prescription'
          });
        }
      });
    } else {
      this.markFormGroupTouched(this.prescriptionForm);
      this.messageService.add({
        severity: 'warn',
        summary: 'Validation Error',
        detail: 'Please fill in all required fields correctly'
      });
    }
  }

  onFileUpload(event: any): void {
    const file = event.files[0];
    if (file && this.createdPrescriptionId) {
      this.uploadingFile = true;
      
      this.prescriptionService.uploadPrescriptionFile(this.createdPrescriptionId, file).subscribe({
        next: (response) => {
          this.uploadingFile = false;
          this.messageService.add({
            severity: 'success',
            summary: 'Success',
            detail: 'Prescription file uploaded successfully'
          });
          
          // Navigate to prescription list or dashboard
          setTimeout(() => {
            this.router.navigate(['/doctor/dashboard']);
          }, 2000);
        },
        error: (error) => {
          this.uploadingFile = false;
          this.messageService.add({
            severity: 'error',
            summary: 'Upload Error',
            detail: error.error?.message || 'Failed to upload prescription file'
          });
        }
      });
    }
  }

  skipFileUpload(): void {
    this.messageService.add({
      severity: 'info',
      summary: 'Prescription Created',
      detail: 'Prescription created without file upload'
    });
    
    setTimeout(() => {
      this.router.navigate(['/doctor/dashboard']);
    }, 1500);
  }

  private markFormGroupTouched(formGroup: FormGroup): void {
    Object.keys(formGroup.controls).forEach(key => {
      const control = formGroup.get(key);
      if (control instanceof FormGroup) {
        this.markFormGroupTouched(control);
      } else if (control instanceof FormArray) {
        control.controls.forEach(arrayControl => {
          if (arrayControl instanceof FormGroup) {
            this.markFormGroupTouched(arrayControl);
          } else {
            arrayControl.markAsTouched();
          }
        });
      } else {
        control?.markAsTouched();
      }
    });
  }

  isFieldInvalid(fieldName: string, arrayIndex?: number): boolean {
    if (arrayIndex !== undefined) {
      const medicine = this.medicines.at(arrayIndex);
      const field = medicine.get(fieldName);
      return !!(field?.invalid && field?.touched);
    }
    
    const field = this.prescriptionForm.get(fieldName);
    return !!(field?.invalid && field?.touched);
  }

  getFieldError(fieldName: string, arrayIndex?: number): string {
    let field;
    if (arrayIndex !== undefined) {
      field = this.medicines.at(arrayIndex).get(fieldName);
    } else {
      field = this.prescriptionForm.get(fieldName);
    }

    if (field?.errors) {
      if (field.errors['required']) return `${fieldName} is required`;
      if (field.errors['minlength']) return `${fieldName} is too short`;
      if (field.errors['pattern']) return `Invalid ${fieldName} format`;
      if (field.errors['min']) return `${fieldName} must be at least ${field.errors['min'].min}`;
      if (field.errors['max']) return `${fieldName} must be at most ${field.errors['max'].max}`;
    }
    return '';
  }

  // Quick medicine templates
  addCommonMedicine(type: string): void {
    let template: Partial<PrescriptionMedicine>;
    
    switch (type) {
      case 'paracetamol':
        template = {
          medicineName: 'Paracetamol',
          genericName: 'Paracetamol',
          dosage: '500mg',
          frequency: 'Three times daily',
          duration: '5 days',
          quantity: 15,
          instructions: 'Take after meals',
          medicineType: MedicineType.OTC
        };
        break;
      case 'amoxicillin':
        template = {
          medicineName: 'Amoxicillin',
          genericName: 'Amoxicillin',
          dosage: '500mg',
          frequency: 'Three times daily',
          duration: '7 days',
          quantity: 21,
          instructions: 'Complete the full course',
          medicineType: MedicineType.PRESCRIPTION
        };
        break;
      case 'omeprazole':
        template = {
          medicineName: 'Omeprazole',
          genericName: 'Omeprazole',
          dosage: '20mg',
          frequency: 'Once daily',
          duration: '14 days',
          quantity: 14,
          instructions: 'Take before breakfast',
          medicineType: MedicineType.PRESCRIPTION
        };
        break;
      default:
        return;
    }

    const medicineForm = this.createMedicineFormGroup();
    medicineForm.patchValue(template);
    this.medicines.push(medicineForm);
    
    this.messageService.add({
      severity: 'info',
      summary: 'Template Added',
      detail: `${template.medicineName} template added`
    });
  }
}
