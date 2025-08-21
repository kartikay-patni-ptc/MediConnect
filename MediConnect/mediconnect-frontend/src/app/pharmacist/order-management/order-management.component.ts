import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MessageService, ConfirmationService } from 'primeng/api';
import { Router } from '@angular/router';
import { AuthService } from '../../auth/auth.service';
import { PharmacyOrderService } from '../services/pharmacy-order.service';

export interface MedicineOrder {
  id: number;
  orderNumber: string;
  patientName: string;
  patientPhone: string;
  deliveryAddress: string;
  deliveryPincode: string;
  specialInstructions?: string;
  status: OrderStatus;
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
  status: ItemStatus;
}

export enum OrderStatus {
  PENDING = 'PENDING',
  PHARMACY_ASSIGNED = 'PHARMACY_ASSIGNED',
  ACCEPTED = 'ACCEPTED',
  REJECTED = 'REJECTED',
  PREPARING = 'PREPARING',
  READY_FOR_PICKUP = 'READY_FOR_PICKUP',
  OUT_FOR_DELIVERY = 'OUT_FOR_DELIVERY',
  DELIVERED = 'DELIVERED',
  CANCELLED = 'CANCELLED',
  REFUNDED = 'REFUNDED'
}

export enum ItemStatus {
  PENDING = 'PENDING',
  PREPARING = 'PREPARING',
  READY = 'READY',
  OUT_OF_STOCK = 'OUT_OF_STOCK'
}

@Component({
  selector: 'app-order-management',
  templateUrl: './order-management.component.html',
  styleUrls: ['./order-management.component.css'],
  providers: [MessageService, ConfirmationService]
})
export class OrderManagementComponent implements OnInit {
  // Make enums available in template
  OrderStatus = OrderStatus;
  ItemStatus = ItemStatus;
  
  orders: MedicineOrder[] = [];
  filteredOrders: MedicineOrder[] = [];
  loading = false;
  selectedOrder: MedicineOrder | null = null;
  showOrderDialog = false;
  showRejectDialog = false;
  
  // Filters
  statusFilter: string = 'ALL';
  searchQuery = '';
  
  // Form for rejection
  rejectForm!: FormGroup;
  
  // Statistics
  totalOrders = 0;
  pendingOrders = 0;
  acceptedOrders = 0;
  preparingOrders = 0;
  readyOrders = 0;
  
  currentUser: any;

  constructor(
    private fb: FormBuilder,
    private messageService: MessageService,
    private confirmationService: ConfirmationService,
    private authService: AuthService,
    private pharmacyOrderService: PharmacyOrderService,
    private router: Router
  ) {
    this.initializeForm();
  }

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
    this.loadOrders();
  }

  private initializeForm(): void {
    this.rejectForm = this.fb.group({
      rejectionReason: ['', [Validators.required, Validators.minLength(10)]]
    });
  }

  loadOrders(): void {
    if (!this.currentUser?.userId) return;
    
    this.loading = true;
    this.pharmacyOrderService.getPharmacyOrders(this.currentUser.userId).subscribe({
      next: (response: any) => {
        this.orders = response.orders || [];
        this.filteredOrders = [...this.orders];
        this.calculateStatistics();
        this.loading = false;
      },
      error: (error: any) => {
        this.loading = false;
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to load orders'
        });
        console.error('Error loading orders:', error);
      }
    });
  }

  private calculateStatistics(): void {
    this.totalOrders = this.orders.length;
    this.pendingOrders = this.orders.filter(o => o.status === OrderStatus.PENDING || o.status === OrderStatus.PHARMACY_ASSIGNED).length;
    this.acceptedOrders = this.orders.filter(o => o.status === OrderStatus.ACCEPTED).length;
    this.preparingOrders = this.orders.filter(o => o.status === OrderStatus.PREPARING).length;
    this.readyOrders = this.orders.filter(o => o.status === OrderStatus.READY_FOR_PICKUP).length;
  }

  filterOrders(): void {
    let filtered = [...this.orders];
    
    // Apply status filter
    if (this.statusFilter !== 'ALL') {
      filtered = filtered.filter(order => order.status === this.statusFilter as OrderStatus);
    }
    
    // Apply search filter
    if (this.searchQuery.trim()) {
      const query = this.searchQuery.toLowerCase();
      filtered = filtered.filter(order => 
        order.orderNumber.toLowerCase().includes(query) ||
        order.patientName.toLowerCase().includes(query) ||
        order.deliveryAddress.toLowerCase().includes(query)
      );
    }
    
    this.filteredOrders = filtered;
  }

  viewOrderDetails(order: MedicineOrder): void {
    this.selectedOrder = order;
    this.showOrderDialog = true;
  }

  acceptOrder(order: MedicineOrder): void {
    this.confirmationService.confirm({
      message: `Are you sure you want to accept order ${order.orderNumber}?`,
      header: 'Accept Order',
      icon: 'pi pi-check-circle',
      accept: () => {
        this.pharmacyOrderService.acceptOrder(order.id).subscribe({
          next: (response: any) => {
            this.messageService.add({
              severity: 'success',
              summary: 'Order Accepted',
              detail: `Order ${order.orderNumber} has been accepted successfully`
            });
            this.loadOrders(); // Refresh the list
          },
          error: (error: any) => {
            this.messageService.add({
              severity: 'error',
              summary: 'Error',
              detail: 'Failed to accept order'
            });
          }
        });
      }
    });
  }

  rejectOrder(order: MedicineOrder): void {
    this.selectedOrder = order;
    this.showRejectDialog = true;
  }

  submitRejection(): void {
    if (this.rejectForm.valid && this.selectedOrder) {
      const reason = this.rejectForm.value.rejectionReason;
      
      this.pharmacyOrderService.rejectOrder(this.selectedOrder.id, reason).subscribe({
        next: (response: any) => {
          this.messageService.add({
            severity: 'success',
            summary: 'Order Rejected',
            detail: `Order ${this.selectedOrder?.orderNumber} has been rejected`
          });
          this.showRejectDialog = false;
          this.rejectForm.reset();
          this.loadOrders(); // Refresh the list
        },
        error: (error: any) => {
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: 'Failed to reject order'
          });
        }
      });
    }
  }

  updateOrderStatus(order: MedicineOrder, newStatus: OrderStatus): void {
    this.pharmacyOrderService.updateOrderStatus(order.id, newStatus).subscribe({
      next: (response: any) => {
        this.messageService.add({
          severity: 'success',
          summary: 'Status Updated',
          detail: `Order ${order.orderNumber} status updated to ${newStatus}`
        });
        this.loadOrders(); // Refresh the list
      },
      error: (error: any) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to update order status'
        });
      }
    });
  }

  getStatusSeverity(status: OrderStatus): 'success' | 'warning' | 'danger' | 'info' {
    switch (status) {
      case OrderStatus.DELIVERED:
        return 'success';
      case OrderStatus.ACCEPTED:
      case OrderStatus.PREPARING:
      case OrderStatus.READY_FOR_PICKUP:
        return 'warning';
      case OrderStatus.REJECTED:
      case OrderStatus.CANCELLED:
        return 'danger';
      default:
        return 'info';
    }
  }

  getStatusSeverityClass(status: OrderStatus): string {
    switch (status) {
      case OrderStatus.DELIVERED:
        return 'bg-green-100 text-green-800';
      case OrderStatus.ACCEPTED:
      case OrderStatus.PREPARING:
      case OrderStatus.READY_FOR_PICKUP:
        return 'bg-yellow-100 text-yellow-800';
      case OrderStatus.REJECTED:
      case OrderStatus.CANCELLED:
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-blue-100 text-blue-800';
    }
  }

  getItemStatusSeverityClass(status: string): string {
    switch (status) {
      case 'READY':
        return 'bg-green-100 text-green-800';
      case 'PREPARING':
        return 'bg-yellow-100 text-yellow-800';
      case 'OUT_OF_STOCK':
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-blue-100 text-blue-800';
    }
  }

  getStatusLabel(status: OrderStatus): string {
    return status.replace(/_/g, ' ').toLowerCase()
      .replace(/\b\w/g, l => l.toUpperCase());
  }

  closeOrderDialog(): void {
    this.showOrderDialog = false;
    this.selectedOrder = null;
  }

  closeRejectDialog(): void {
    this.showRejectDialog = false;
    this.rejectForm.reset();
    this.selectedOrder = null;
  }

  refreshOrders(): void {
    this.loadOrders();
  }
}
