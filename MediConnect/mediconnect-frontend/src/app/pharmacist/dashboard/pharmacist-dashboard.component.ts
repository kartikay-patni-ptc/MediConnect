import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { MessageService } from 'primeng/api';
import { AuthService } from '../../auth/auth.service';
import { PharmacyOrderService } from '../services/pharmacy-order.service';

@Component({
  selector: 'app-pharmacist-dashboard',
  templateUrl: './pharmacist-dashboard.component.html',
  styleUrls: ['./pharmacist-dashboard.component.css']
})
export class PharmacistDashboardComponent implements OnInit {
  currentUser: any;
  pharmacyProfile: any;
  dashboardData: any;
  loading = true;
  hasProfile = false;

  // Order statistics
  orderStats = {
    totalOrders: 0,
    pendingOrders: 0,
    acceptedOrders: 0,
    preparingOrders: 0,
    readyOrders: 0,
    deliveredOrders: 0
  };

  // Recent orders
  recentOrders: any[] = [];
  loadingOrders = false;

  constructor(
    private authService: AuthService,
    private router: Router,
    private messageService: MessageService,
    private pharmacyOrderService: PharmacyOrderService
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
    this.loadDashboardData();
    this.loadOrderStatistics();
    this.loadRecentOrders();
  }

  loadDashboardData() {
    this.authService.getPharmacyDashboard(this.currentUser.userId).subscribe({
      next: (response: any) => {
        this.dashboardData = response;
        this.pharmacyProfile = response.store;
        this.hasProfile = true;
        this.loading = false;
      },
      error: (error: any) => {
        console.error('Error loading dashboard data:', error);
        this.loading = false;
        this.hasProfile = false;
      }
    });
  }

  loadOrderStatistics() {
    if (!this.currentUser?.userId) return;
    
    this.pharmacyOrderService.getOrderStatistics(this.currentUser.userId).subscribe({
      next: (response: any) => {
        this.orderStats = response;
      },
      error: (error) => {
        console.error('Error loading order statistics:', error);
      }
    });
  }

  loadRecentOrders() {
    if (!this.currentUser?.userId) return;
    
    this.loadingOrders = true;
    this.pharmacyOrderService.getRecentOrders(this.currentUser.userId, 5).subscribe({
      next: (orders: any[]) => {
        this.recentOrders = orders;
        this.loadingOrders = false;
      },
      error: (error) => {
        console.error('Error loading recent orders:', error);
        this.loadingOrders = false;
      }
    });
  }

  goToHome(): void {
    this.router.navigate(['/home']);
  }

  logout(): void {
    this.authService.logout();
    this.messageService.add({
      severity: 'success',
      summary: 'Logged Out',
      detail: 'You have been successfully logged out'
    });
    setTimeout(() => {
      this.router.navigate(['/home']);
    }, 1000);
  }

  navigateToProfile(): void {
    this.router.navigate(['/pharmacist/profile']);
  }

  navigateToOrders(): void {
    this.router.navigate(['/pharmacist/orders']);
  }

  viewOrderDetails(order: any): void {
    this.router.navigate(['/pharmacist/orders', order.id]);
  }

  getStatusSeverity(status: string): 'success' | 'warning' | 'danger' | 'info' {
    switch (status) {
      case 'DELIVERED':
        return 'success';
      case 'ACCEPTED':
      case 'PREPARING':
      case 'READY_FOR_PICKUP':
        return 'warning';
      case 'REJECTED':
      case 'CANCELLED':
        return 'danger';
      default:
        return 'info';
    }
  }

  getStatusLabel(status: string): string {
    return status.replace(/_/g, ' ').toLowerCase()
      .replace(/\b\w/g, l => l.toUpperCase());
  }

  getStatusSeverityClass(status: string): string {
    switch (status) {
      case 'DELIVERED':
        return 'bg-green-100 text-green-800';
      case 'ACCEPTED':
      case 'PREPARING':
      case 'READY_FOR_PICKUP':
        return 'bg-yellow-100 text-yellow-800';
      case 'REJECTED':
      case 'CANCELLED':
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-blue-100 text-blue-800';
    }
  }

  refreshData(): void {
    this.loadOrderStatistics();
    this.loadRecentOrders();
    this.messageService.add({
      severity: 'success',
      summary: 'Refreshed',
      detail: 'Dashboard data has been refreshed'
    });
  }
}