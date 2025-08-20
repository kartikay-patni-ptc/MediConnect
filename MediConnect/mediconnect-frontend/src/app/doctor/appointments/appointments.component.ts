import { Component, OnInit, ViewChild } from '@angular/core';
import { Table } from 'primeng/table';
import { MessageService, ConfirmationService } from 'primeng/api';
import { AppointmentService } from '../../appointment/appointment.service';
import { AuthService } from '../../auth/auth.service';
import { Appointment } from '../../appointment/appointment.model';
import { Router } from '@angular/router';

@Component({
  selector: 'app-appointments',
  templateUrl: './appointments.component.html',
  styleUrls: ['./appointments.component.css'],
  providers: [MessageService, ConfirmationService]
})
export class AppointmentsComponent implements OnInit {
  @ViewChild('dataTable') dataTable!: Table;
  
  appointments: Appointment[] = [];
  loading = true;
  searchQuery = '';
  selectedAppointment: Appointment | null = null;
  showPrescriptionDialog = false;
  currentUser: any;
  doctorId: number = 0;

  statusOptions = [
    { label: 'All', value: '' },
    { label: 'Pending', value: 'Pending' },
    { label: 'Confirmed', value: 'Confirmed' },
    { label: 'Completed', value: 'Completed' },
    { label: 'Cancelled', value: 'Cancelled' }
  ];

  selectedStatus = '';

  constructor(
    private appointmentService: AppointmentService,
    private authService: AuthService,
    private messageService: MessageService,
    private confirmationService: ConfirmationService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
    this.loadDoctorAppointments();
  }

  loadDoctorAppointments(): void {
    this.loading = true;
    const userId = this.authService.getUserId();
    
    this.appointmentService.getDoctorByUserId(userId).subscribe({
      next: (doctor) => {
        this.doctorId = doctor.id;
        this.loadAppointments();
      },
      error: (error) => {
        console.error('Error getting doctor info:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to load doctor information'
        });
        this.loading = false;
      }
    });
  }

  loadAppointments(): void {
    this.appointmentService.getDoctorAppointments(this.doctorId).subscribe({
      next: (appointments) => {
        this.appointments = appointments;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading appointments:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to load appointments'
        });
        this.loading = false;
      }
    });
  }

  onGlobalFilter(): void {
    if (this.dataTable) {
      this.dataTable.filterGlobal(this.searchQuery, 'contains');
    }
  }

  onStatusFilter(): void {
    if (this.dataTable) {
      this.dataTable.filter(this.selectedStatus, 'status', 'equals');
    }
  }

  writePrescription(appointment: Appointment): void {
    if (appointment.status === 'Completed') {
      this.messageService.add({
        severity: 'warn',
        summary: 'Warning',
        detail: 'Cannot write prescription for completed appointment'
      });
      return;
    }
    
    this.router.navigate(['/prescription/write', appointment.id]);
  }

  updateAppointmentStatus(appointment: Appointment, newStatus: 'Pending' | 'Confirmed' | 'Cancelled' | 'Completed'): void {
    // This would typically call an API to update the status
    appointment.status = newStatus;
    this.messageService.add({
      severity: 'success',
      summary: 'Status Updated',
      detail: `Appointment status changed to ${newStatus}`
    });
  }

  cancelAppointment(appointment: Appointment): void {
    this.confirmationService.confirm({
      message: `Are you sure you want to cancel this appointment?`,
      header: 'Cancel Appointment',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        appointment.status = 'Cancelled';
        this.messageService.add({
          severity: 'success',
          summary: 'Appointment Cancelled',
          detail: 'Appointment has been cancelled'
        });
      }
    });
  }

  getStatusSeverity(status: string): 'success' | 'warning' | 'danger' | 'info' {
    switch (status) {
      case 'Completed':
        return 'success';
      case 'Confirmed':
        return 'warning';
      case 'Cancelled':
        return 'danger';
      default:
        return 'info';
    }
  }

  getStatusMenuItems(appointment: Appointment): any[] {
    const statuses: ('Pending' | 'Confirmed' | 'Cancelled' | 'Completed')[] = ['Pending', 'Confirmed', 'Completed'];
    return statuses
      .filter(status => status !== appointment.status)
      .map(status => ({
        label: status,
        icon: 'pi pi-check',
        command: () => this.updateAppointmentStatus(appointment, status)
      }));
  }

  goBack(): void {
    this.router.navigate(['/doctor/dashboard']);
  }
}
