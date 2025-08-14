import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
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

// Components
import { PatientDashboardComponent } from './dashboard/patient-dashboard.component';
import { PatientProfileComponent } from './profile/patient-profile.component';

@NgModule({
  declarations: [
    PatientDashboardComponent,
    PatientProfileComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    PatientRoutingModule,
    CardModule,
    ButtonModule,
    InputTextModule,
    TableModule,
    ToastModule,
    DropdownModule,
    CalendarModule,
    InputTextareaModule
  ],
  providers: [MessageService]
})
export class PatientModule { }