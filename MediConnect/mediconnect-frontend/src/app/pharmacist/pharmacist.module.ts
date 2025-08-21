import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { PharmacistDashboardComponent } from './dashboard/pharmacist-dashboard.component';
import { PharmacyProfileComponent } from './profile/pharmacy-profile.component';
import { OrderManagementComponent } from './order-management/order-management.component';

// PrimeNG Modules
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { DropdownModule } from 'primeng/dropdown';
import { TableModule } from 'primeng/table';
import { BadgeModule } from 'primeng/badge';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { ToastModule } from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { InputTextareaModule } from 'primeng/inputtextarea';

const routes: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  { path: 'dashboard', component: PharmacistDashboardComponent },
  { path: 'profile', component: PharmacyProfileComponent },
  { path: 'orders', component: OrderManagementComponent },
  { path: 'orders/:id', component: OrderManagementComponent }
];

@NgModule({
  declarations: [
    PharmacistDashboardComponent,
    PharmacyProfileComponent,
    OrderManagementComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    RouterModule.forChild(routes),
    ButtonModule,
    CardModule,
    DialogModule,
    InputTextModule,
    DropdownModule,
    TableModule,
    BadgeModule,
    ProgressSpinnerModule,
    ToastModule,
    ConfirmDialogModule,
    InputTextareaModule
  ]
})
export class PharmacistModule { }