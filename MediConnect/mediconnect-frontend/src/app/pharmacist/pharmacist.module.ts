import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { ReactiveFormsModule } from '@angular/forms';
import { PharmacistDashboardComponent } from './dashboard/pharmacist-dashboard.component';
import { PharmacyProfileComponent } from './profile/pharmacy-profile.component';

const routes: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  { path: 'dashboard', component: PharmacistDashboardComponent },
  { path: 'profile', component: PharmacyProfileComponent }
];

@NgModule({
  declarations: [
    PharmacistDashboardComponent,
    PharmacyProfileComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule.forChild(routes)
  ]
})
export class PharmacistModule { }