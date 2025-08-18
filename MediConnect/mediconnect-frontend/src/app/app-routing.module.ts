import { NgModule } from '@angular/core';
import { RouterModule,Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: 'home', pathMatch: 'full' },
  { path: 'home', loadChildren: () => import('./home/home.module').then(m => m.HomeModule) },
  { path: 'auth', loadChildren: () => import('./auth/auth.module').then(m => m.AuthModule) },
  { path: 'doctor', loadChildren: () => import('./doctor/doctor.module').then(m => m.DoctorModule) },
  { path: 'patient', loadChildren: () => import('./patient/patient.module').then(m => m.PatientModule) },
  { path: 'chat', loadChildren: () => import('./ai-chat/ai-chat.module').then(m => m.AiChatModule) },
  { path: 'pharmacist', loadChildren: () => import('./pharmacist/pharmacist.module').then(m => m.PharmacistModule) },
  { path: 'prescription', loadChildren: () => import('./prescription/prescription.module').then(m => m.PrescriptionModule) },
  { path: '**', redirectTo: 'home' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }