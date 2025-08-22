import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface MedicineOrder {
  id: number;
  orderNumber: string;
  patientName: string;
  patientPhone: string;
  deliveryAddress: string;
  deliveryPincode: string;
  specialInstructions?: string;
  status: string;
  totalAmount: number;
  deliveryFee: number;
  finalAmount: number;
  createdAt: Date;
  acceptedAt?: Date;
  expectedDeliveryTime?: Date;
  orderItems: OrderItem[];
  prescription?: any;
}

export interface OrderItem {
  id: number;
  medicineName: string;
  dosage: string;
  quantity: number;
  status: string;
}

export interface OrderStatusUpdate {
  status: string;
  notes?: string;
}

@Injectable({
  providedIn: 'root'
})
export class PharmacyOrderService {
  private readonly API_URL = 'http://localhost:8080/api/medicine-orders';

  constructor(private http: HttpClient) {}

  /**
   * Get all orders for a specific pharmacy
   */
  getPharmacyOrders(pharmacyUserId: number): Observable<any> {
    return this.http.get(`${this.API_URL}/pharmacy/${pharmacyUserId}`);
  }

  /**
   * Get a specific order by ID
   */
  getOrderById(orderId: number): Observable<MedicineOrder> {
    return this.http.get<MedicineOrder>(`${this.API_URL}/${orderId}`);
  }

  /**
   * Accept an order
   */
  acceptOrder(orderId: number): Observable<any> {
    return this.http.put(`${this.API_URL}/${orderId}/accept`, {});
  }

  /**
   * Reject an order with reason
   */
  rejectOrder(orderId: number, rejectionReason: string): Observable<any> {
    return this.http.put(`${this.API_URL}/${orderId}/reject`, { rejectionReason });
  }

  /**
   * Update order status
   */
  updateOrderStatus(orderId: number, status: string): Observable<any> {
    return this.http.put(`${this.API_URL}/${orderId}/status`, { status });
  }

  /**
   * Update order item status
   */
  updateOrderItemStatus(orderId: number, itemId: number, status: string): Observable<any> {
    return this.http.put(`${this.API_URL}/${orderId}/items/${itemId}/status`, { status });
  }

  /**
   * Add pharmacy notes to an order
   */
  addPharmacyNotes(orderId: number, notes: string): Observable<any> {
    return this.http.put(`${this.API_URL}/${orderId}/notes`, { notes });
  }

  /**
   * Get order statistics for dashboard
   */
  getOrderStatistics(pharmacyUserId: number): Observable<any> {
    return this.http.get(`${this.API_URL}/pharmacy/${pharmacyUserId}/statistics`);
  }

  /**
   * Get orders by status
   */
  getOrdersByStatus(pharmacyUserId: number, status: string): Observable<MedicineOrder[]> {
    return this.http.get<MedicineOrder[]>(`${this.API_URL}/pharmacy/${pharmacyUserId}/status/${status}`);
  }

  /**
   * Search orders
   */
  searchOrders(pharmacyUserId: number, query: string): Observable<MedicineOrder[]> {
    return this.http.get<MedicineOrder[]>(`${this.API_URL}/pharmacy/${pharmacyUserId}/search`, {
      params: { q: query }
    });
  }

  /**
   * Get recent orders
   */
  getRecentOrders(pharmacyUserId: number, limit: number = 10): Observable<MedicineOrder[]> {
    return this.http.get<MedicineOrder[]>(`${this.API_URL}/pharmacy/${pharmacyUserId}/recent`, {
      params: { limit: limit.toString() }
    });
  }

  /**
   * Get orders for today
   */
  getTodayOrders(pharmacyUserId: number): Observable<MedicineOrder[]> {
    return this.http.get<MedicineOrder[]>(`${this.API_URL}/pharmacy/${pharmacyUserId}/today`);
  }

  /**
   * Get orders by date range
   */
  getOrdersByDateRange(pharmacyUserId: number, startDate: string, endDate: string): Observable<MedicineOrder[]> {
    return this.http.get<MedicineOrder[]>(`${this.API_URL}/pharmacy/${pharmacyUserId}/date-range`, {
      params: { startDate, endDate }
    });
  }

  /**
   * Export orders to CSV
   */
  exportOrdersToCSV(pharmacyUserId: number, filters?: any): Observable<Blob> {
    return this.http.post(`${this.API_URL}/pharmacy/${pharmacyUserId}/export`, filters, {
      responseType: 'blob'
    });
  }

  /**
   * Get delivery tracking information
   */
  getDeliveryTracking(orderId: number): Observable<any> {
    return this.http.get(`${this.API_URL}/${orderId}/tracking`);
  }

  /**
   * Update delivery tracking
   */
  updateDeliveryTracking(orderId: number, trackingData: any): Observable<any> {
    return this.http.put(`${this.API_URL}/${orderId}/tracking`, trackingData);
  }

  /**
   * Get pharmacy performance metrics
   */
  getPerformanceMetrics(pharmacyUserId: number): Observable<any> {
    return this.http.get(`${this.API_URL}/pharmacy/${pharmacyUserId}/performance`);
  }
}





