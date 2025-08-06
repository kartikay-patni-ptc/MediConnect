import { Component, OnInit } from '@angular/core';
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

  constructor(private authService: AuthService) {}

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

  logout(): void {
    this.authService.logout();
  }

  navigateToProfile(): void {
    // Navigate to profile creation if no profile exists
    if (!this.hasProfile) {
      // This will be handled by routing
    }
  }
}