import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { InputTextareaModule } from 'primeng/inputtextarea';
import { MessageService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';

import { DashboardComponent } from './dashboard/dashboard.component';
import { DoctorProfileComponent } from './profile/doctor-profile.component';
import { SlotManagementComponent } from './slot-management/slot-management.component';
import { AppointmentsComponent } from './appointments/appointments.component';
import { DoctorRoutingModule } from './doctor-routing.module';
import { PrescriptionModule } from '../prescription/prescription.module';

import { FormsModule } from '@angular/forms';
import { TableModule } from 'primeng/table';
import { ToolbarModule } from 'primeng/toolbar';
import { BreadcrumbModule } from 'primeng/breadcrumb';
import { SidebarModule } from 'primeng/sidebar';
import { AvatarModule } from 'primeng/avatar';
import { CardModule } from 'primeng/card';
import { TagModule } from 'primeng/tag';
import { DialogModule } from 'primeng/dialog';
import { FileUploadModule } from 'primeng/fileupload';
import { SplitButtonModule } from 'primeng/splitbutton';
import { CalendarModule } from 'primeng/calendar';
import { TooltipModule } from 'primeng/tooltip';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { DropdownModule } from 'primeng/dropdown';
import { BadgeModule } from 'primeng/badge';
import { ProgressSpinnerModule } from 'primeng/progressspinner';

@NgModule({
  declarations: [
    DashboardComponent,
    DoctorProfileComponent,
    SlotManagementComponent,
    AppointmentsComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    ButtonModule,
    InputTextModule,
    InputTextareaModule,
    ToastModule,
    DoctorRoutingModule,
    FormsModule,
    TableModule,
    ToolbarModule,
    BreadcrumbModule,
    SidebarModule,
    AvatarModule,
    CardModule,
    TagModule,
    DialogModule,
    FileUploadModule,
    SplitButtonModule,
    CalendarModule,
    TooltipModule,
    ConfirmDialogModule,
    DropdownModule,
    BadgeModule,
    ProgressSpinnerModule,
    PrescriptionModule
  ],
  providers: [MessageService]
})
export class DoctorModule { }