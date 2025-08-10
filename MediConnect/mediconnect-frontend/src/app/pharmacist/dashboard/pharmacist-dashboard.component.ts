import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { MessageService } from 'primeng/api';
import { AuthService } from '../../auth/auth.service';

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

  constructor(
    private authService: AuthService,
    private router: Router,
    private messageService: MessageService
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
    this.loadDashboardData();
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
}