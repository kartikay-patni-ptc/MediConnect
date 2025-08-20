import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Doctor, DoctorSlot, Appointment } from './appointment.model';
import { map } from 'rxjs/operators';

@Injectable({ providedIn: 'root' })
export class AppointmentService {
  private readonly API_URL = 'http://localhost:8080/api/appointments';
  private readonly DOCTOR_API_URL = 'http://localhost:8080/api/doctor';

  constructor(private http: HttpClient) {}

  getAvailableDoctors(specialization?: string): Observable<Doctor[]> {
    return this.http.get<Doctor[]>(`${this.DOCTOR_API_URL}/available`, { params: specialization ? { specialization } : {} });
  }

  getDoctorSlots(doctorId: number): Observable<DoctorSlot[]> {
    return this.http.get<any>(`${this.DOCTOR_API_URL}/${doctorId}/slots`)
      .pipe(
        map(response => response.slots || [])
      );
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

  // Doctor Slot Management Methods
  createDoctorSlot(doctorId: number, slotData: any): Observable<any> {
    return this.http.post(`${this.DOCTOR_API_URL}/${doctorId}/slots`, slotData);
  }

  updateSlotAvailability(slotId: number, available: boolean): Observable<any> {
    return this.http.put(`${this.DOCTOR_API_URL}/slots/${slotId}/availability`, { available });
  }

  deleteDoctorSlot(slotId: number): Observable<any> {
    return this.http.delete(`${this.DOCTOR_API_URL}/slots/${slotId}`);
  }
  
getDoctorByUserId(userId: number): Observable<Doctor> {
  return this.http.get<Doctor>(`http://localhost:8080/api/doctor/by-user/${userId}`);
}

}