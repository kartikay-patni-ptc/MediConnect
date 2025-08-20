import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MessageService, ConfirmationService } from 'primeng/api';
import { AuthService } from '../../auth/auth.service';
import { AppointmentService } from '../../appointment/appointment.service';
import { DoctorSlot } from '../../appointment/appointment.model';

@Component({
  selector: 'app-slot-management',
  templateUrl: './slot-management.component.html',
  styleUrls: ['./slot-management.component.css'],
  providers: [MessageService, ConfirmationService]
})
export class SlotManagementComponent implements OnInit {
  slots: DoctorSlot[] = [];
  loading = false;
  showAddSlotDialog = false;
  slotForm: FormGroup;
  selectedSlot: DoctorSlot | null = null;
  doctorId: number = 0;

  constructor(
    private fb: FormBuilder,
    private messageService: MessageService,
    private confirmationService: ConfirmationService,
    private authService: AuthService,
    private appointmentService: AppointmentService
  ) {
    this.slotForm = this.fb.group({
      startTime: ['', Validators.required],
      endTime: ['', Validators.required],
      date: ['', Validators.required]
    });
  }

  
ngOnInit(): void {
  const userId = this.authService.getUserId();
  if (userId > 0) {
    this.appointmentService.getDoctorByUserId(userId).subscribe({
      next: (doctor) => {
        this.doctorId = doctor.id;
        this.loadSlots();
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Doctor profile not found. Please login again.'
        });
      }
    });
  }
}


  loadSlots(): void {
    this.loading = true;
    this.appointmentService.getDoctorSlots(this.doctorId).subscribe({
      next: (slots) => {
        this.slots = slots;
        this.loading = false;
      },
      error: (error) => {
        console.error('Failed to load slots:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to load time slots'
        });
        this.loading = false;
      }
    });
  }

  openAddSlotDialog(): void {
    this.slotForm.reset();
    this.showAddSlotDialog = true;
  }

  addSlot(): void {
    if (this.slotForm.valid) {
      const formValue = this.slotForm.value;
      const date = new Date(formValue.date);
      const startTime = new Date(formValue.startTime);
      const endTime = new Date(formValue.endTime);

      // Combine date with time
      const startDateTime = new Date(
        date.getFullYear(),
        date.getMonth(),
        date.getDate(),
        startTime.getHours(),
        startTime.getMinutes()
      );
      const endDateTime = new Date(
        date.getFullYear(),
        date.getMonth(),
        date.getDate(),
        endTime.getHours(),
        endTime.getMinutes()
      );

      const slotData = {
        
        startTime: startDateTime.toISOString().slice(0, 19), // removes 'Z'
        endTime: endDateTime.toISOString().slice(0, 19)

      };

      this.appointmentService.createDoctorSlot(this.doctorId, slotData).subscribe({
        next: (response: any) => {
          this.messageService.add({
            severity: 'success',
            summary: 'Success',
            detail: 'Time slot added successfully'
          });
          this.showAddSlotDialog = false;
          this.loadSlots();
        },
        error: (error: any) => {
          console.error('Failed to add slot:', error);
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: 'Failed to add time slot'
          });
        }
      });
    }
  }

  toggleSlotAvailability(slot: DoctorSlot): void {
    const newStatus = !slot.available;
    const action = newStatus ? 'make available' : 'make unavailable';
    
    this.confirmationService.confirm({
      message: `Are you sure you want to ${action} this time slot?`,
      header: 'Confirm Action',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.appointmentService.updateSlotAvailability(slot.id, newStatus).subscribe({
          next: (response: any) => {
            slot.available = newStatus;
            this.messageService.add({
              severity: 'success',
              summary: 'Success',
              detail: `Time slot ${action} successfully`
            });
          },
          error: (error: any) => {
            console.error('Failed to update slot:', error);
            this.messageService.add({
              severity: 'error',
              summary: 'Error',
              detail: 'Failed to update time slot'
            });
          }
        });
      }
    });
  }

  deleteSlot(slot: DoctorSlot): void {
    this.confirmationService.confirm({
      message: 'Are you sure you want to delete this time slot?',
      header: 'Confirm Deletion',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.appointmentService.deleteDoctorSlot(slot.id).subscribe({
          next: (response: any) => {
            this.slots = this.slots.filter(s => s.id !== slot.id);
            this.messageService.add({
              severity: 'success',
              summary: 'Success',
              detail: 'Time slot deleted successfully'
            });
          },
          error: (error: any) => {
            console.error('Failed to delete slot:', error);
            this.messageService.add({
              severity: 'error',
              summary: 'Error',
              detail: 'Failed to delete time slot'
            });
          }
        });
      }
    });
  }

  formatDateTime(dateTimeString: string): string {
    const date = new Date(dateTimeString);
    return date.toLocaleString('en-US', {
      weekday: 'short',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  getSlotStatusClass(slot: DoctorSlot): string {
    return slot.available ? 'available' : 'unavailable';
  }

  getSlotDuration(startTime: string, endTime: string): string {
    const start = new Date(startTime);
    const end = new Date(endTime);
    const diffMs = end.getTime() - start.getTime();
    const diffHours = Math.floor(diffMs / (1000 * 60 * 60));
    const diffMinutes = Math.floor((diffMs % (1000 * 60 * 60)) / (1000 * 60));
    
    if (diffHours > 0) {
      return `${diffHours}h ${diffMinutes}m`;
    } else {
      return `${diffMinutes}m`;
    }
  }
  getAvailableSlotsCount(): number {
    return this.slots.filter(s => s.available).length;
  }

  getBookedSlotsCount(): number {
    return this.slots.filter(s => !s.available).length;
  }
}