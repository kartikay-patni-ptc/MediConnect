import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { map, tap } from 'rxjs/operators';
import { AuthService } from '../../auth/auth.service';
import { 
  Prescription, 
  MedicineOrder, 
  CreatePrescriptionRequest, 
  CreateOrderRequest,
  PharmacyStore,
  DeliveryEstimate,
  PrescriptionStatus,
  OrderStatus
} from '../models/prescription.model';

@Injectable({
  providedIn: 'root'
})
export class PrescriptionService {
  private baseUrl = 'http://localhost:8080/api';
  
  // State management
  private prescriptionsSubject = new BehaviorSubject<Prescription[]>([]);
  private ordersSubject = new BehaviorSubject<MedicineOrder[]>([]);
  
  public prescriptions$ = this.prescriptionsSubject.asObservable();
  public orders$ = this.ordersSubject.asObservable();

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  private getAuthHeaders(): HttpHeaders {
    const token = this.authService.getToken();
    console.log('PrescriptionService - JWT Token:', token ? 'Present' : 'Missing');
    console.log('PrescriptionService - Token value:', token);
    
    if (!token) {
      console.error('No JWT token found! User might not be authenticated.');
    }
    
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    });
  }

  // Prescription Management
  createPrescription(request: CreatePrescriptionRequest): Observable<any> {
    return this.http.post(`${this.baseUrl}/prescriptions/create`, request, { headers: this.getAuthHeaders() })
      .pipe(tap(() => this.refreshPrescriptions()));
  }

  uploadPrescriptionFile(prescriptionId: number, file: File): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    
    // For file uploads, we need to set the Authorization header manually
    const token = this.authService.getToken();
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
      // Note: Don't set Content-Type for FormData, let the browser set it
    });
    
    return this.http.post(`${this.baseUrl}/prescriptions/${prescriptionId}/upload`, formData, { headers })
      .pipe(tap(() => this.refreshPrescriptions()));
  }

  getPatientPrescriptions(patientId: number): Observable<Prescription[]> {
    return this.http.get<any>(`${this.baseUrl}/prescriptions/patient/${patientId}`, { headers: this.getAuthHeaders() })
      .pipe(
        map(response => response.prescriptions || []),
        tap(prescriptions => this.prescriptionsSubject.next(prescriptions))
      );
  }

  getDoctorPrescriptions(doctorId: number): Observable<Prescription[]> {
    return this.http.get<any>(`${this.baseUrl}/prescriptions/doctor/${doctorId}`, { headers: this.getAuthHeaders() })
      .pipe(
        map(response => response.prescriptions || []),
        tap(prescriptions => this.prescriptionsSubject.next(prescriptions))
      );
  }

  getActivePrescriptions(patientId: number): Observable<Prescription[]> {
    return this.http.get<any>(`${this.baseUrl}/prescriptions/patient/${patientId}/active`, { headers: this.getAuthHeaders() })
      .pipe(map(response => response.prescriptions || []));
  }

  getPrescriptionById(prescriptionId: number): Observable<Prescription> {
    return this.http.get<any>(`${this.baseUrl}/prescriptions/${prescriptionId}`, { headers: this.getAuthHeaders() })
      .pipe(map(response => response.prescription));
  }

  getPrescriptionByNumber(prescriptionNumber: string): Observable<Prescription> {
    return this.http.get<any>(`${this.baseUrl}/prescriptions/number/${prescriptionNumber}`, { headers: this.getAuthHeaders() })
      .pipe(map(response => response.prescription));
  }

  updatePrescriptionStatus(prescriptionId: number, status: PrescriptionStatus): Observable<any> {
    return this.http.put(`${this.baseUrl}/prescriptions/${prescriptionId}/status`, { status }, { headers: this.getAuthHeaders() })
      .pipe(tap(() => this.refreshPrescriptions()));
  }

  // Medicine Order Management
  createMedicineOrder(request: CreateOrderRequest): Observable<any> {
    return this.http.post(`${this.baseUrl}/medicine-orders/create`, request, { headers: this.getAuthHeaders() })
      .pipe(tap(() => this.refreshOrders()));
  }

  getPatientOrders(patientId: number): Observable<MedicineOrder[]> {
    return this.http.get<any>(`${this.baseUrl}/medicine-orders/patient/${patientId}`, { headers: this.getAuthHeaders() })
      .pipe(
        map(response => response.orders || []),
        tap(orders => this.ordersSubject.next(orders))
      );
  }

  getPharmacyOrders(pharmacyId: number): Observable<MedicineOrder[]> {
    return this.http.get<any>(`${this.baseUrl}/medicine-orders/pharmacy/${pharmacyId}`, { headers: this.getAuthHeaders() })
      .pipe(map(response => response.orders || []));
  }

  getPendingOrders(): Observable<MedicineOrder[]> {
    return this.http.get<any>(`${this.baseUrl}/medicine-orders/pending`, { headers: this.getAuthHeaders() })
      .pipe(map(response => response.orders || []));
  }

  getOrderById(orderId: number): Observable<MedicineOrder> {
    return this.http.get<any>(`${this.baseUrl}/medicine-orders/${orderId}`, { headers: this.getAuthHeaders() })
      .pipe(map(response => response.order));
  }

  getOrderByNumber(orderNumber: string): Observable<MedicineOrder> {
    return this.http.get<any>(`${this.baseUrl}/medicine-orders/number/${orderNumber}`, { headers: this.getAuthHeaders() })
      .pipe(map(response => response.order));
  }

  acceptOrder(orderId: number, pharmacyId: number): Observable<any> {
    return this.http.post(`${this.baseUrl}/medicine-orders/${orderId}/accept`, { pharmacyId }, { headers: this.getAuthHeaders() })
      .pipe(tap(() => this.refreshOrders()));
  }

  rejectOrder(orderId: number, pharmacyId: number, rejectionReason: string): Observable<any> {
    return this.http.post(`${this.baseUrl}/medicine-orders/${orderId}/reject`, { 
      pharmacyId, 
      rejectionReason 
    }, { headers: this.getAuthHeaders() }).pipe(tap(() => this.refreshOrders()));
  }

  updateOrderStatus(orderId: number, status: OrderStatus): Observable<any> {
    return this.http.put(`${this.baseUrl}/medicine-orders/${orderId}/status`, { status }, { headers: this.getAuthHeaders() })
      .pipe(tap(() => this.refreshOrders()));
  }

  // Pharmacy and Delivery
  getNearbyPharmacies(pincode: string, radiusKm: number = 10): Observable<PharmacyStore[]> {
    return this.http.get<any>(`${this.baseUrl}/medicine-orders/nearby-pharmacies`, {
      params: { pincode, radiusKm: radiusKm.toString() },
      headers: this.getAuthHeaders()
    }).pipe(map(response => response.pharmacies || []));
  }

  getDeliveryEstimate(pincode: string, pharmacyId: number): Observable<DeliveryEstimate> {
    return this.http.get<any>(`${this.baseUrl}/medicine-orders/delivery-estimate`, {
      params: { pincode, pharmacyId: pharmacyId.toString() },
      headers: this.getAuthHeaders()
    }).pipe(map(response => ({
      distance: response.distance,
      estimatedTimeMinutes: response.estimatedTimeMinutes,
      deliveryFee: response.deliveryFee
    })));
  }

  // Utility methods
  private refreshPrescriptions(): void {
    // This would typically refetch based on current user context
    // For now, we'll just emit the current value to trigger updates
    const current = this.prescriptionsSubject.value;
    this.prescriptionsSubject.next([...current]);
  }

  private refreshOrders(): void {
    // This would typically refetch based on current user context
    const current = this.ordersSubject.value;
    this.ordersSubject.next([...current]);
  }

  // Helper methods for status management
  getStatusSeverity(status: PrescriptionStatus | OrderStatus): string {
    switch (status) {
      case PrescriptionStatus.ACTIVE:
      case OrderStatus.DELIVERED:
        return 'success';
      case PrescriptionStatus.EXPIRED:
      case OrderStatus.CANCELLED:
      case OrderStatus.REJECTED:
        return 'danger';
      case PrescriptionStatus.CANCELLED:
      case OrderStatus.PENDING:
        return 'warning';
      case OrderStatus.ACCEPTED:
      case OrderStatus.PREPARING:
      case OrderStatus.OUT_FOR_DELIVERY:
        return 'info';
      default:
        return 'secondary';
    }
  }

  getStatusIcon(status: PrescriptionStatus | OrderStatus): string {
    switch (status) {
      case PrescriptionStatus.ACTIVE:
        return 'pi pi-check-circle';
      case PrescriptionStatus.EXPIRED:
        return 'pi pi-clock';
      case PrescriptionStatus.CANCELLED:
        return 'pi pi-times-circle';
      case OrderStatus.PENDING:
        return 'pi pi-hourglass';
      case OrderStatus.ACCEPTED:
        return 'pi pi-thumbs-up';
      case OrderStatus.PREPARING:
        return 'pi pi-cog';
      case OrderStatus.OUT_FOR_DELIVERY:
        return 'pi pi-truck';
      case OrderStatus.DELIVERED:
        return 'pi pi-check';
      case OrderStatus.CANCELLED:
      case OrderStatus.REJECTED:
        return 'pi pi-times';
      default:
        return 'pi pi-info-circle';
    }
  }

  formatDuration(minutes: number): string {
    if (minutes < 60) {
      return `${minutes} min`;
    } else if (minutes < 1440) {
      const hours = Math.floor(minutes / 60);
      const remainingMinutes = minutes % 60;
      return remainingMinutes > 0 ? `${hours}h ${remainingMinutes}m` : `${hours}h`;
    } else {
      const days = Math.floor(minutes / 1440);
      const remainingHours = Math.floor((minutes % 1440) / 60);
      return remainingHours > 0 ? `${days}d ${remainingHours}h` : `${days}d`;
    }
  }

  formatDistance(km: number): string {
    if (km < 1) {
      return `${Math.round(km * 1000)} m`;
    }
    return `${km.toFixed(1)} km`;
  }
}
