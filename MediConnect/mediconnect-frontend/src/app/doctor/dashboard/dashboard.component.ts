import { Component, OnInit } from '@angular/core';
import { MenuItem, MessageService } from 'primeng/api';
import { ViewChild } from '@angular/core';
import { Table } from 'primeng/table';
import { Router } from '@angular/router';
import { AuthService } from '../../auth/auth.service';

interface Appointment {
  id: string;
  patientName: string;
  time: string;
  type: 'Consultation' | 'Follow-up' | 'Surgery' | 'Diagnostic';
  status: 'Upcoming' | 'In Progress' | 'Completed' | 'Cancelled';
}

interface NavItem { id: string; label: string; icon: string; }

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  isSidebarOpen = true;
  mobileSidebar = false;
  selectedDate: Date = new Date();
  searchQuery = '';
  loading = true;
  currentUser: any;

  greeting = 'Good afternoon, Dr. Onichaan 👋';

  breadcrumbHome: MenuItem = { icon: 'pi pi-home', routerLink: ['/doctor'] };
  breadcrumbItems: MenuItem[] = [{ label: 'Dashboard' }];

  @ViewChild('dataTable') dataTable!: Table;

  actionItems: MenuItem[] = [];
  menuItems: MenuItem[] = [];

  showUploadDialog = false;
  uploadedFiles: File[] = [];

  navItems: NavItem[] = [
    { id: 'dashboard', label: 'Dashboard', icon: 'pi pi-home' },
    { id: 'patients', label: 'Patients', icon: 'pi pi-users' },
    { id: 'appointments', label: 'Appointments', icon: 'pi pi-calendar' },
    { id: 'upload', label: 'Upload Prescription', icon: 'pi pi-upload' },
    { id: 'messages', label: 'Messages', icon: 'pi pi-envelope' },
    { id: 'settings', label: 'Settings', icon: 'pi pi-cog' }
  ];

  // Dummy Data - Do not alter
  todaySchedule = [
    { time: '09:00 AM', patient: 'John Carter', type: 'Consultation' },
    { time: '10:15 AM', patient: 'Emily Davis', type: 'Follow-up' },
    { time: '11:30 AM', patient: 'Michael Chen', type: 'Diagnostic' },
  ];

  patientStats = {
    totalPatients: 248,
    newThisWeek: 12,
    awaiting: 5,
  };

  upcomingAppointments: Appointment[] = [
    { id: 'APT-1012', patientName: 'Sophia Patel', time: 'Today, 03:00 PM', type: 'Consultation', status: 'Upcoming' },
    { id: 'APT-1013', patientName: 'Liam Johnson', time: 'Tomorrow, 10:00 AM', type: 'Follow-up', status: 'Upcoming' },
    { id: 'APT-1014', patientName: 'Ava Martinez', time: 'Aug 12, 09:30 AM', type: 'Diagnostic', status: 'Upcoming' },
    { id: 'APT-1015', patientName: 'Noah Williams', time: 'Aug 13, 02:00 PM', type: 'Consultation', status: 'Upcoming' },
  ];

  constructor(
    private messageService: MessageService, 
    private router: Router,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
    this.isSidebarOpen = localStorage.getItem('mc_sidebarOpen') !== 'false';
    this.loadDoctorData();

    this.actionItems = [
      { label: 'New Appointment', icon: 'pi pi-plus', command: () => this.onNew() },
      { label: 'Export CSV', icon: 'pi pi-download', command: () => this.exportCsv() },
    ];

    this.menuItems = [
      { label: 'Dashboard', icon: 'pi pi-home' },
      { label: 'Patients', icon: 'pi pi-users' },
      { label: 'Appointments', icon: 'pi pi-calendar' },
      { label: 'Upload Prescription', icon: 'pi pi-upload', command: () => this.openUploadDialog() },
      { label: 'Messages', icon: 'pi pi-envelope' },
      { label: 'Settings', icon: 'pi pi-cog' }
    ];

    setTimeout(() => {
      this.loading = false;
    }, 500);
  }

  loadDoctorData(): void {
    const userId = this.authService.getUserId();
    if (userId) {
      this.authService.getDoctorDashboard(userId).subscribe({
        next: (response: any) => {
          if (response.doctor) {
            this.greeting = `Good ${this.getTimeOfDay()}, Dr. ${response.doctor.firstName} 👋`;
            
            // Update patient stats with real data
            this.patientStats = {
              totalPatients: response.totalPatients || 0,
              newThisWeek: response.newThisWeek || 0,
              awaiting: response.pendingAppointments || 0,
            };
            
            // Update appointments with real data
            if (response.appointments) {
              this.upcomingAppointments = response.appointments.map((apt: any) => ({
                id: apt.id,
                patientName: apt.patientName,
                time: apt.time,
                type: apt.type,
                status: apt.status
              }));
            }
          }
        },
        error: (error) => {
          console.error('Error loading doctor data:', error);
        }
      });
    }
  }

  getTimeOfDay(): string {
    const hour = new Date().getHours();
    if (hour < 12) return 'morning';
    if (hour < 17) return 'afternoon';
    return 'evening';
  }

  onNavClick(itemId: string): void {
    if (itemId === 'upload') {
      this.router.navigate(['/doctor/prescriptions']);
      return;
    }
    this.messageService.add({ severity: 'info', summary: 'Navigation', detail: `${itemId} clicked` });
  }

  toggleSidebar(): void {
    this.isSidebarOpen = !this.isSidebarOpen;
    localStorage.setItem('mc_sidebarOpen', String(this.isSidebarOpen));
  }

  openMobileSidebar(): void {
    this.mobileSidebar = true;
  }

  closeMobileSidebar(): void {
    this.mobileSidebar = false;
  }

  onGlobalFilter(): void {
    if (this.dataTable) {
      this.dataTable.filterGlobal(this.searchQuery, 'contains');
    }
  }

  onNew(): void {
    this.messageService.add({ severity: 'success', summary: 'New', detail: 'Create new appointment' });
  }

  exportCsv(): void {
    if (this.dataTable) {
      this.dataTable.exportCSV();
    }
  }

  openUploadDialog(): void {
    this.showUploadDialog = true;
  }

  onUpload(event: any): void {
    const files = event.files as File[];
    this.uploadedFiles.push(...files);
    this.messageService.add({ severity: 'success', summary: 'Uploaded', detail: `${files.length} file(s) uploaded` });
    this.showUploadDialog = false;
  }

  getStatusSeverity(status: Appointment['status']): 'success' | 'warning' | 'danger' | 'info' {
    switch (status) {
      case 'Completed':
        return 'success';
      case 'In Progress':
        return 'warning';
      case 'Cancelled':
        return 'danger';
      default:
        return 'info';
    }
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
}