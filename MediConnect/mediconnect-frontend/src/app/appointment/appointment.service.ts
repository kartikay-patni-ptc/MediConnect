import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Doctor, DoctorSlot, Appointment } from './appointment.model';

@Injectable({ providedIn: 'root' })
export class AppointmentService {
  private readonly API_URL = 'http://localhost:8080/api/appointments';
  private readonly DOCTOR_API_URL = 'http://localhost:8080/api/doctors';

  constructor(private http: HttpClient) {}

  getAvailableDoctors(specialization?: string): Observable<Doctor[]> {
    return this.http.get<Doctor[]>(`${this.DOCTOR_API_URL}/available`, { params: specialization ? { specialization } : {} });
  }

  getDoctorSlots(doctorId: number): Observable<DoctorSlot[]> {
    return this.http.get<DoctorSlot[]>(`${this.DOCTOR_API_URL}/${doctorId}/slots`);
  }

  bookAppointment(appointment: Appointment): Observable<any> {
    return this.http.post(`${this.API_URL}`, appointment);
  }

  getPatientAppointments(patientId: number): Observable<Appointment[]> {
    return this.http.get<Appointment[]>(`${this.API_URL}/patient/${patientId}`);
  }

  getDoctorAppointments(doctorId: number): Observable<Appointment[]> {
    return this.http.get<Appointment[]>(`${this.API_URL}/doctor/${doctorId}`);
  }
}
