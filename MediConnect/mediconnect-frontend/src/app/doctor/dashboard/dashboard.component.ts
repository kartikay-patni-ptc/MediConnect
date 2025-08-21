import { Component, OnInit } from '@angular/core';
import { MenuItem, MessageService } from 'primeng/api';
import { ViewChild } from '@angular/core';
import { Table } from 'primeng/table';
import { Router } from '@angular/router';
import { AuthService } from '../../auth/auth.service';
import { AppointmentService } from '../../appointment/appointment.service';

interface Appointment {
  id: string;
  patientName: string;
  time: string;
  type: 'Consultation' | 'Follow-up' | 'Surgery' | 'Diagnostic';
  status: 'Upcoming' | 'Confirmed' | 'Completed' | 'Cancelled';
  notes?: string;
  aiSummary?: { // This will now hold patientAdvice
    answer: string;
    homeRemedies?: string;
    riskLevel: 'Low' | 'Medium' | 'High' | 'Critical';
    redFlags?: string;
    specializationHint?: string;
    specialists?: string[];
  };
  doctorSummary?: any; // Can be string or structured object
  // New AI consultation fields
  patientAdvice?: string;
  prescribedMedicines?: { name: string; dose: string; frequency: string; duration: string; otcOrPrescription: string; }[];
  riskLevel?: string;
  redFlags?: string[];
  homeRemedies?: string[];
  specializationHint?: string;
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
  showAppointmentDialog = false;
  selectedAppointment: Appointment | null = null;
  completedAppointments = 0;

  greeting = 'Good afternoon, Dr. Onichaan 👋';

  breadcrumbHome: MenuItem = { icon: 'pi pi-home', routerLink: ['/doctor'] };
  breadcrumbItems: MenuItem[] = [{ label: 'Dashboard' }];

  @ViewChild('dataTable') dataTable!: Table;

  actionItems: MenuItem[] = [];
  menuItems: MenuItem[] = [];

  showUploadDialog = false;
  uploadedFiles: File[] = [];

  // Patient selection dialog properties
  showPatientSelectionDialog = false;
  selectedPatientForPrescription: any = null;

  navItems: NavItem[] = [
    { id: 'dashboard', label: 'Dashboard', icon: 'pi pi-home' },
    { id: 'patients', label: 'Patients', icon: 'pi pi-users' },
    { id: 'appointments', label: 'Appointments', icon: 'pi pi-calendar' },
    { id: 'prescriptions', label: 'My Prescriptions', icon: 'pi pi-list' },
    { id: 'write-prescription', label: 'Write Prescription', icon: 'pi pi-pencil' },
    { id: 'manage-slots', label: 'Manage Slots', icon: 'pi pi-clock' },
    { id: 'ai-chat', label: 'AI Consult', icon: 'pi pi-comments' },
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
    { 
      id: 'APT-1012', 
      patientName: 'Sophia Patel', 
      time: 'Today, 03:00 PM', 
      type: 'Consultation', 
      status: 'Upcoming',
      notes: 'Patient experiencing chest pain and shortness of breath',
      aiSummary: {
        answer: 'Based on the symptoms described, this could be related to cardiovascular issues. The patient reports chest pain and shortness of breath which are concerning symptoms that require immediate medical attention.',
        homeRemedies: 'Rest, avoid strenuous activity, and monitor symptoms closely. If symptoms worsen, seek emergency care immediately.',
        riskLevel: 'High',
        redFlags: 'Chest pain, shortness of breath, dizziness, nausea - these are emergency symptoms that require immediate medical evaluation.',
        specializationHint: 'Cardiology consultation recommended',
        specialists: ['Cardiologist', 'Emergency Medicine']
      },
      doctorSummary: {
        chiefComplaint: 'Chest pain and shortness of breath',
        historyOfPresentIllness: 'Patient experiencing chest pain and shortness of breath for 2 days',
        medicalHistory: 'No known cardiac history',
        assessment: 'Possible cardiovascular issue requiring immediate evaluation',
        plan: 'ECG and blood work pending, cardiology consultation recommended',
        prescribedMedicines: [],
        redFlags: ['Chest pain', 'Shortness of breath'],
        specialistRecommendation: 'Cardiology'
      }
    },
    { 
      id: 'APT-1013', 
      patientName: 'Liam Johnson', 
      time: 'Tomorrow, 10:00 AM', 
      type: 'Follow-up', 
      status: 'Upcoming',
      notes: 'Follow-up for hypertension management',
      aiSummary: {
        answer: 'Regular follow-up for hypertension management is important. Monitor blood pressure readings and medication compliance.',
        homeRemedies: 'Maintain low-sodium diet, regular exercise, stress management techniques, and consistent medication schedule.',
        riskLevel: 'Medium',
        specializationHint: 'Cardiology follow-up',
        specialists: ['Cardiologist', 'Primary Care']
      },
      doctorSummary: {
        chiefComplaint: 'Hypertension management follow-up',
        historyOfPresentIllness: 'Patient reports stable blood pressure on current medication',
        medicalHistory: 'Known hypertension, no other chronic conditions',
        assessment: 'Well-controlled hypertension, medication effective',
        plan: 'Continue current medication, follow-up scheduled for next month',
        prescribedMedicines: [],
        redFlags: [],
        specialistRecommendation: 'Primary Care'
      }
    },
    { 
      id: 'APT-1014', 
      patientName: 'Ava Martinez', 
      time: 'Aug 12, 09:30 AM', 
      type: 'Diagnostic', 
      status: 'Upcoming',
      notes: 'Routine ECG and blood work',
      aiSummary: {
        answer: 'Routine diagnostic tests for cardiovascular health assessment. Standard procedure for monitoring heart function.',
        riskLevel: 'Low',
        specializationHint: 'Cardiology diagnostics',
        specialists: ['Cardiologist']
      },
      doctorSummary: {
        chiefComplaint: 'Routine cardiovascular health assessment',
        historyOfPresentIllness: 'Patient reports normal ECG and blood work results',
        medicalHistory: 'No known cardiac conditions',
        assessment: 'Normal cardiovascular function, no immediate concerns',
        plan: 'Continue routine monitoring, annual follow-up recommended',
        prescribedMedicines: [],
        redFlags: [],
        specialistRecommendation: 'Cardiology'
      }
    },
    { 
      id: 'APT-1015', 
      patientName: 'Noah Williams', 
      time: 'Aug 13, 02:00 PM', 
      type: 'Consultation', 
      status: 'Upcoming',
      notes: 'New patient consultation for heart palpitations',
      aiSummary: {
        answer: 'Heart palpitations can have various causes including stress, caffeine, or underlying heart conditions. Requires thorough evaluation.',
        homeRemedies: 'Reduce caffeine intake, practice stress reduction techniques, maintain regular sleep schedule.',
        riskLevel: 'Medium',
        redFlags: 'If accompanied by chest pain, shortness of breath, or fainting, seek immediate medical attention.',
        specializationHint: 'Cardiology consultation',
        specialists: ['Cardiologist', 'Electrophysiologist']
      },
      doctorSummary: {
        chiefComplaint: 'Heart palpitations',
        historyOfPresentIllness: 'Patient reports new onset heart palpitations',
        medicalHistory: 'No known cardiac conditions',
        assessment: 'Heart palpitations requiring evaluation to determine cause',
        plan: 'ECG and EKG scheduled for tomorrow, cardiology consultation recommended',
        prescribedMedicines: [],
        redFlags: ['Heart palpitations'],
        specialistRecommendation: 'Cardiology'
      }
    },
  ];

  constructor(
    private messageService: MessageService, 
    private router: Router,
    private authService: AuthService,
    private appointmentService: AppointmentService
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
    this.isSidebarOpen = localStorage.getItem('mc_sidebarOpen') !== 'false';
    this.loadDoctorData();

    this.actionItems = [
      { label: 'New Appointment', icon: 'pi pi-plus', command: () => this.onNew() },
      { label: 'Export CSV', icon: 'pi pi-download', command: () => this.exportCsv() },
      { label: 'Manage Slots', icon: 'pi pi-clock', command: () => this.manageSlots() },
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
              this.upcomingAppointments = response.appointments.map((apt: any) => {
                // Debug logging
                console.log('Raw appointment data:', apt);
                console.log('AI Summary type:', typeof apt.aiSummary);
                console.log('AI Summary value:', apt.aiSummary);
                
                // Parse aiSummary if it's a string
                let parsedAiSummary = apt.aiSummary;
                if (typeof apt.aiSummary === 'string' && apt.aiSummary) {
                  try {
                    parsedAiSummary = JSON.parse(apt.aiSummary);
                  } catch (e) {
                    console.warn('Failed to parse AI summary:', e);
                    // Create a basic structure if parsing fails
                    parsedAiSummary = {
                      answer: apt.aiSummary,
                      riskLevel: 'Medium'
                    };
                  }
                }
                
                // Parse JSON string fields if they exist
                let parsedPrescribedMedicines = apt.prescribedMedicines;
                if (typeof apt.prescribedMedicines === 'string' && apt.prescribedMedicines) {
                  try {
                    parsedPrescribedMedicines = JSON.parse(apt.prescribedMedicines);
                  } catch (e) {
                    console.warn('Failed to parse prescribed medicines:', e);
                  }
                }
                
                let parsedRedFlags = apt.redFlags;
                if (typeof apt.redFlags === 'string' && apt.redFlags) {
                  try {
                    parsedRedFlags = JSON.parse(apt.redFlags);
                  } catch (e) {
                    console.warn('Failed to parse red flags:', e);
                    parsedRedFlags = [apt.redFlags]; // Treat as single item array
                  }
                }
                
                let parsedHomeRemedies = apt.homeRemedies;
                if (typeof apt.homeRemedies === 'string' && apt.homeRemedies) {
                  try {
                    parsedHomeRemedies = JSON.parse(apt.homeRemedies);
                  } catch (e) {
                    console.warn('Failed to parse home remedies:', e);
                    parsedHomeRemedies = [apt.homeRemedies]; // Treat as single item array
                  }
                }
                
                let parsedDoctorSummary = apt.doctorSummary;
                if (typeof apt.doctorSummary === 'string' && apt.doctorSummary) {
                  try {
                    // Try to parse as JSON first, if it fails, keep as string
                    const tempParsed = JSON.parse(apt.doctorSummary);
                    if (typeof tempParsed === 'object') {
                      parsedDoctorSummary = tempParsed;
                    }
                  } catch (e) {
                    // Keep as string if not valid JSON
                    parsedDoctorSummary = apt.doctorSummary;
                  }
                }

                return {
                  id: apt.id,
                  patientName: apt.patientName,
                  time: apt.time,
                  type: apt.type,
                  status: apt.status,
                  notes: apt.notes,
                  aiSummary: parsedAiSummary,
                  doctorSummary: parsedDoctorSummary,
                  // New fields from AI consultation
                  patientAdvice: apt.patientAdvice,
                  prescribedMedicines: parsedPrescribedMedicines,
                  riskLevel: apt.riskLevel,
                  redFlags: parsedRedFlags,
                  homeRemedies: parsedHomeRemedies,
                  specializationHint: apt.specializationHint
                };
              });
            }

            // Calculate completed appointments
            this.completedAppointments = this.upcomingAppointments.filter(apt => apt.status === 'Completed').length;
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

  viewAppointmentDetails(appointment: Appointment): void {
    this.selectedAppointment = appointment;
    this.showAppointmentDialog = true;
  }

  completeAppointment(appointmentId: string): void {
    // Find the appointment in the list
    const appointment = this.upcomingAppointments.find(apt => apt.id === appointmentId);
    if (appointment) {
      // Update local state immediately for better UX
      appointment.status = 'Completed';
      this.completedAppointments++;
      
      // Save to backend
      this.appointmentService.updateAppointmentStatus(Number(appointmentId), 'Completed').subscribe({
        next: (updatedAppointment) => {
          this.messageService.add({
            severity: 'success',
            summary: 'Appointment Completed',
            detail: `Appointment ${appointmentId} has been marked as completed and saved`
          });
          // Update the appointment with the response from backend
          Object.assign(appointment, updatedAppointment);
        },
        error: (error) => {
          // Revert local state if backend update fails
          appointment.status = 'Upcoming';
          this.completedAppointments--;
          this.messageService.add({
            severity: 'error',
            summary: 'Update Failed',
            detail: 'Failed to save appointment status. Please try again.'
          });
          console.error('Error updating appointment status:', error);
        }
      });
    }
  }

  cancelAppointment(appointmentId: string): void {
    // Find the appointment in the list
    const appointment = this.upcomingAppointments.find(apt => apt.id === appointmentId);
    if (appointment) {
      // Update local state immediately for better UX
      appointment.status = 'Cancelled';
      
      // Save to backend
      this.appointmentService.updateAppointmentStatus(Number(appointmentId), 'Cancelled').subscribe({
        next: (updatedAppointment) => {
          this.messageService.add({
            severity: 'warn',
            summary: 'Appointment Cancelled',
            detail: `Appointment ${appointmentId} has been cancelled and saved`
          });
          // Update the appointment with the response from backend
          Object.assign(appointment, updatedAppointment);
        },
        error: (error) => {
          // Revert local state if backend update fails
          appointment.status = 'Upcoming';
          this.messageService.add({
            severity: 'error',
            summary: 'Update Failed',
            detail: 'Failed to save appointment cancellation. Please try again.'
          });
          console.error('Error cancelling appointment:', error);
        }
      });
    }
  }

  startConsultation(appointmentId: string): void {
    const appointment = this.upcomingAppointments.find(apt => apt.id === appointmentId);
    if (appointment) {
      // Update local state immediately for better UX
      appointment.status = 'Confirmed';
      
      // Save to backend
      this.appointmentService.updateAppointmentStatus(Number(appointmentId), 'Confirmed').subscribe({
        next: (updatedAppointment) => {
          this.messageService.add({
            severity: 'info',
            summary: 'Consultation Started',
            detail: `Consultation for ${appointment.patientName} has begun and saved`
          });
          // Update the appointment with the response from backend
          Object.assign(appointment, updatedAppointment);
        },
        error: (error) => {
          // Revert local state if backend update fails
          appointment.status = 'Upcoming';
          this.messageService.add({
            severity: 'error',
            summary: 'Update Failed',
            detail: 'Failed to start consultation. Please try again.'
          });
          console.error('Error starting consultation:', error);
        }
      });
    }
  }

  getRiskLevelSeverity(riskLevel: string): 'success' | 'warning' | 'danger' | 'info' {
    switch (riskLevel) {
      case 'Low':
        return 'success';
      case 'Medium':
        return 'warning';
      case 'High':
      case 'Critical':
        return 'danger';
      default:
        return 'info';
    }
  }

  viewAllAppointments(): void {
    this.messageService.add({
      severity: 'info',
      summary: 'View All Appointments',
      detail: 'Navigate to appointments page'
    });
    // Navigate to appointments page
    // this.router.navigate(['/doctor/appointments']);
  }

  manageSlots(): void {
    
    // Navigate to slot management page
     this.router.navigate(['/doctor/slots']);
  }

  viewPatientRecords(): void {
    this.messageService.add({
      severity: 'info',
      summary: 'Patient Records',
      detail: 'Access patient medical records'
    });
    // Navigate to patient records page
    // this.router.navigate(['/doctor/patients']);
  }

  writePrescription(appointmentId: string): void {
    // Close the appointment dialog first
    this.showAppointmentDialog = false;
    
    // Navigate to prescription writer with appointment ID
    this.router.navigate(['/prescription/write', appointmentId]);
    
    this.messageService.add({
      severity: 'success',
      summary: 'Redirecting',
      detail: 'Opening prescription writer for this appointment'
    });
  }

  onNavClick(itemId: string): void {
    switch (itemId) {
      case 'dashboard':
        // Already on dashboard - refresh
        this.loadDoctorData();
        break;
      case 'patients':
        // Navigate to patient records or show patient list
        this.messageService.add({ 
          severity: 'info', 
          summary: 'Patient Records', 
          detail: 'Patient records functionality coming soon' 
        });
        break;
      case 'appointments':
        // Navigate to appointments view
        this.router.navigate(['/doctor/appointments']);
        break;
      case 'prescriptions':
        this.router.navigate(['/prescription/list']);
        break;
      case 'write-prescription':
        // Show patient selection dialog first
        this.showPatientSelectionDialog = true;
        break;
      case 'manage-slots':
        // Navigate to slot management
        this.router.navigate(['/doctor/slots']);
        break;
      case 'ai-chat':
        // Navigate to AI chat consultation
        this.router.navigate(['/chat']);
        break;
      case 'messages':
        // Navigate to messages or show info
        this.messageService.add({ 
          severity: 'info', 
          summary: 'Messages', 
          detail: 'Messaging system coming soon' 
        });
        break;
      case 'settings':
        // Navigate to doctor profile settings
        this.router.navigate(['/doctor/profile']);
        break;
      default:
        this.messageService.add({ severity: 'info', summary: 'Navigation', detail: `${itemId} clicked` });
    }
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
      case 'Confirmed':
        return 'warning';
      case 'Cancelled':
        return 'danger';
      default:
        return 'info';
    }
  }

  onPatientDialogClosed(): void {
    this.showPatientSelectionDialog = false;
  }

  onPatientSelectedForPrescription(appointment: Appointment): void {
    this.selectedPatientForPrescription = appointment;
    this.showPatientSelectionDialog = false;
    
    // Navigate to prescription writer with the selected appointment
    this.router.navigate(['/prescription/write', appointment.id]);
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