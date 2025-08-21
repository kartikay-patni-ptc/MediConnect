import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormControl } from '@angular/forms';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';

export interface Appointment {
  id: string;
  patientName: string;
  time: string;
  type: 'Consultation' | 'Follow-up' | 'Surgery' | 'Diagnostic';
  status: 'Upcoming' | 'Confirmed' | 'Completed' | 'Cancelled';
  notes?: string;
}

@Component({
  selector: 'app-patient-selection-dialog',
  templateUrl: './patient-selection-dialog.component.html',
  styleUrls: ['./patient-selection-dialog.component.css']
})
export class PatientSelectionDialogComponent implements OnInit {
  @Input() visible: boolean = false;
  @Input() appointments: Appointment[] = [];
  @Input() loading: boolean = false;
  
  @Output() visibleChange = new EventEmitter<boolean>();
  @Output() patientSelected = new EventEmitter<Appointment>();

  searchControl = new FormControl('');
  filteredAppointments: Appointment[] = [];

  ngOnInit() {
    this.searchControl.valueChanges
      .pipe(
        debounceTime(300),
        distinctUntilChanged()
      )
      .subscribe(searchTerm => {
        this.filterAppointments(searchTerm || '');
      });
    
    this.filteredAppointments = [...this.appointments];
  }

  ngOnChanges() {
    if (this.appointments) {
      this.filteredAppointments = [...this.appointments];
    }
  }

  filterAppointments(searchTerm: string) {
    if (!searchTerm.trim()) {
      this.filteredAppointments = [...this.appointments];
      return;
    }

    const term = searchTerm.toLowerCase();
    this.filteredAppointments = this.appointments.filter(appointment =>
      appointment.patientName.toLowerCase().includes(term) ||
      appointment.type.toLowerCase().includes(term) ||
      appointment.status.toLowerCase().includes(term) ||
      (appointment.notes && appointment.notes.toLowerCase().includes(term))
    );
  }

  selectPatient(appointment: Appointment) {
    this.patientSelected.emit(appointment);
    this.closeDialog();
  }

  closeDialog() {
    this.visible = false;
    this.visibleChange.emit(false);
    this.searchControl.setValue('');
  }

  getStatusSeverityClass(status: string): string {
    switch (status.toLowerCase()) {
      case 'completed':
        return 'bg-green-100 text-green-800';
      case 'cancelled':
        return 'bg-red-100 text-red-800';
      case 'pending':
        return 'bg-yellow-100 text-yellow-800';
      case 'confirmed':
        return 'bg-blue-100 text-blue-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }
}
