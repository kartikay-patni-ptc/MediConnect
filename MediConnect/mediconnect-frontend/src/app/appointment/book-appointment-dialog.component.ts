import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AppointmentService } from './appointment.service';
import { Doctor, DoctorSlot, Appointment } from './appointment.model';

@Component({
  selector: 'app-book-appointment-dialog',
  templateUrl: './book-appointment-dialog.component.html',
  styleUrls: ['./book-appointment-dialog.component.css']
})
export class BookAppointmentDialogComponent implements OnInit {
  @Input() visible = false;
  @Input() doctor: Doctor | null = null;
  @Input() patientId!: number;
  @Input() aiSummary: string | null = null;
  @Input() doctorSummary: string | null = null;
  @Input() patientAdvice: string | null = null;
  @Input() prescribedMedicines: string | null = null;
  @Input() riskLevel: string | null = null;
  @Input() redFlags: string | null = null;
  @Input() homeRemedies: string | null = null;
  @Input() specializationHint: string | null = null;
  @Output() visibleChange = new EventEmitter<boolean>();
  @Output() appointmentBooked = new EventEmitter<Appointment>();

  slots: DoctorSlot[] = [];
  bookingForm!: FormGroup;
  isLoading = false;
  error: string | null = null;

  constructor(private fb: FormBuilder, private appointmentService: AppointmentService) {}

  ngOnInit(): void {
    this.bookingForm = this.fb.group({
      slotId: [null, Validators.required],
      notes: ['']
    });
    if (this.doctor) {
      this.fetchSlots();
    }
  }

  ngOnChanges(): void {
    if (this.doctor) {
      this.fetchSlots();
    }
  }

  fetchSlots() {
    if (!this.doctor) return;
    this.isLoading = true;
    this.appointmentService.getDoctorSlots(this.doctor.id).subscribe({
      next: slots => {
        this.slots = slots.filter(s => s.available);
        this.isLoading = false;
      },
      error: () => {
        this.error = 'Failed to load slots.';
        this.isLoading = false;
      }
    });
  }

  submit() {
    if (!this.bookingForm.valid || !this.doctor) return;
    this.isLoading = true;
    const appointment: Appointment = {
      patientId: this.patientId,
      doctorId: this.doctor.id,
      slotId: this.bookingForm.value.slotId,
      status: 'Pending',
      notes: this.bookingForm.value.notes,
      aiSummary: this.aiSummary || undefined,
      doctorSummary: this.doctorSummary || undefined,
      patientAdvice: this.patientAdvice || undefined,
      prescribedMedicines: this.prescribedMedicines || undefined,
      riskLevel: this.riskLevel || undefined,
      redFlags: this.redFlags || undefined,
      homeRemedies: this.homeRemedies || undefined,
      specializationHint: this.specializationHint || undefined
    };
    this.appointmentService.bookAppointment(appointment).subscribe({
      next: (result) => {
        this.isLoading = false;
        this.visibleChange.emit(false);
        this.appointmentBooked.emit(result);
      },
      error: () => {
        this.error = 'Failed to book appointment.';
        this.isLoading = false;
      }
    });
  }

  close() {
    this.visibleChange.emit(false);
  }
}

