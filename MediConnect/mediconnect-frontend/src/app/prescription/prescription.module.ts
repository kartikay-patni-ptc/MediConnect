import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { HttpClientModule } from '@angular/common/http';

// PrimeNG modules
import { CardModule } from 'primeng/card';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { InputTextareaModule } from 'primeng/inputtextarea';
import { DropdownModule } from 'primeng/dropdown';
import { TableModule } from 'primeng/table';
import { DialogModule } from 'primeng/dialog';
import { FileUploadModule } from 'primeng/fileupload';
import { ToastModule } from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { BadgeModule } from 'primeng/badge';
import { TagModule } from 'primeng/tag';
import { PanelModule } from 'primeng/panel';
import { DividerModule } from 'primeng/divider';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { InputNumberModule } from 'primeng/inputnumber';
import { ChipsModule } from 'primeng/chips';
import { CalendarModule } from 'primeng/calendar';

// Components
import { PrescriptionWriterComponent } from './components/prescription-writer/prescription-writer.component';
import { PrescriptionListComponent } from './components/prescription-list/prescription-list.component';
import { PrescriptionViewerComponent } from './components/prescription-viewer/prescription-viewer.component';
import { MedicineOrderComponent } from './components/medicine-order/medicine-order.component';
import { OrderTrackingComponent } from './components/order-tracking/order-tracking.component';


const routes = [
  { path: 'write/:appointmentId', component: PrescriptionWriterComponent },
  { path: 'write', component: PrescriptionWriterComponent },
  { path: 'list', component: PrescriptionListComponent },
  { path: 'view/:prescriptionId', component: PrescriptionViewerComponent },
  { path: 'order/:prescriptionId', component: MedicineOrderComponent },
  { path: 'track/:orderNumber', component: OrderTrackingComponent },
  { path: '', redirectTo: 'list', pathMatch: 'full' as const }
];

@NgModule({
  declarations: [
    PrescriptionWriterComponent,
    PrescriptionListComponent,
    PrescriptionViewerComponent,
    MedicineOrderComponent,
    OrderTrackingComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    HttpClientModule,
    RouterModule.forChild(routes),
    
    // PrimeNG
    CardModule,
    ButtonModule,
    InputTextModule,
    InputTextareaModule,
    DropdownModule,
    TableModule,
    DialogModule,
    FileUploadModule,
    ToastModule,
    ConfirmDialogModule,
    BadgeModule,
    TagModule,
    PanelModule,
    DividerModule,
    ProgressSpinnerModule,
    InputNumberModule,
    ChipsModule,
    CalendarModule
  ],
  exports: [
    PrescriptionWriterComponent,
    PrescriptionListComponent,
    PrescriptionViewerComponent,
    MedicineOrderComponent,
    OrderTrackingComponent
  ]
})
export class PrescriptionModule { }
