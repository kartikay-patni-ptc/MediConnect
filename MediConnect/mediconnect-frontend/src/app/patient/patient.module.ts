import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { PatientRoutingModule } from './patient-routing.module';

// PrimeNG Components
import { CardModule } from 'primeng/card';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { TableModule } from 'primeng/table';
import { ToastModule } from 'primeng/toast';
import { DropdownModule } from 'primeng/dropdown';
import { CalendarModule } from 'primeng/calendar';
import { InputTextareaModule } from 'primeng/inputtextarea';
import { MessageService } from 'primeng/api';
import { PanelModule } from 'primeng/panel';
import { ToolbarModule } from 'primeng/toolbar';
import { BreadcrumbModule } from 'primeng/breadcrumb';
import { AvatarModule } from 'primeng/avatar';
import { TagModule } from 'primeng/tag';
import { SplitButtonModule } from 'primeng/splitbutton';
import { TooltipModule } from 'primeng/tooltip';

// Components
import { PatientDashboardComponent } from './dashboard/patient-dashboard.component';
import { PatientProfileComponent } from './profile/patient-profile.component';
import { HistoryModule } from './history/history.module';
import { AiChatModule } from '../ai-chat/ai-chat.module';
import { ChartsModule } from '../charts/charts.module';
import { AppointmentModule } from '../appointment/appointment.module';
import { PrescriptionService } from '../prescription/services/prescription.service';

@NgModule({
  declarations: [
    PatientDashboardComponent,
    PatientProfileComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    HttpClientModule,
    PatientRoutingModule,
    CardModule,
    ButtonModule,
    InputTextModule,
    TableModule,
    ToastModule,
    DropdownModule,
    CalendarModule,
    InputTextareaModule,
    PanelModule,
    ToolbarModule,
    BreadcrumbModule,
    AvatarModule,
    TagModule,
    SplitButtonModule,
    TooltipModule,
    HistoryModule,
    AiChatModule,
    ChartsModule,
    AppointmentModule
  ],
  providers: [MessageService, PrescriptionService]
})
export class PatientModule { }