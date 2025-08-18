import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MessageService } from 'primeng/api';
import { PrescriptionService } from '../../services/prescription.service';
import { MedicineOrder } from '../../models/prescription.model';

@Component({
  selector: 'app-order-tracking',
  templateUrl: './order-tracking.component.html',
  styleUrls: ['./order-tracking.component.css'],
  providers: [MessageService]
})
export class OrderTrackingComponent implements OnInit {
  order?: MedicineOrder;
  orderNumber!: string;
  loading = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private prescriptionService: PrescriptionService,
    private messageService: MessageService
  ) {}

  ngOnInit(): void {
    this.orderNumber = this.route.snapshot.paramMap.get('orderNumber') || '';
    if (!this.orderNumber) {
      this.messageService.add({
        severity: 'error',
        summary: 'Error',
        detail: 'Invalid order number'
      });
      this.router.navigate(['/prescription/list']);
      return;
    }
    
    this.loadOrder();
  }

  loadOrder(): void {
    this.loading = true;
    
    this.prescriptionService.getOrderByNumber(this.orderNumber).subscribe({
      next: (order) => {
        this.order = order;
        this.loading = false;
      },
      error: (error) => {
        this.loading = false;
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: error.error?.message || 'Failed to load order'
        });
        this.router.navigate(['/prescription/list']);
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/prescription/list']);
  }

  formatDate(date: Date | string | undefined): string {
    if (!date) return 'N/A';
    return new Date(date).toLocaleDateString('en-IN', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  getStatusSeverity(status: string): string {
    switch (status) {
      case 'DELIVERED': return 'success';
      case 'CANCELLED': case 'REJECTED': return 'danger';
      case 'PENDING': return 'warning';
      case 'OUT_FOR_DELIVERY': case 'PREPARING': return 'info';
      default: return 'secondary';
    }
  }
}
