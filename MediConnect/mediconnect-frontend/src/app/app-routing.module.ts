import { NgModule, inject } from '@angular/core';
import { RouterModule,Routes, CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth/auth.service';

// Generic auth guard
const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (!auth.isLoggedIn()) {
    router.navigate(['/auth/login']);
    return false;
  }
  return true;
};

// Role guard factory
const roleGuard = (allowed: string[]): CanActivateFn => () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (!auth.isLoggedIn()) {
    router.navigate(['/auth/login']);
    return false;
  }
  const role = (auth.getCurrentUser()?.role || '').toUpperCase();
  if (!allowed.map(r => r.toUpperCase()).includes(role)) {
    router.navigate(['/home']);
    return false;
  }
  return true;
};

export const routes: Routes = [
  { path: '', redirectTo: 'home', pathMatch: 'full' },
  { path: 'home', loadChildren: () => import('./home/home.module').then(m => m.HomeModule) },
  { path: 'auth', loadChildren: () => import('./auth/auth.module').then(m => m.AuthModule) },
  { path: 'doctor', canActivate: [roleGuard(['DOCTOR'])], loadChildren: () => import('./doctor/doctor.module').then(m => m.DoctorModule) },
  { path: 'patient', canActivate: [roleGuard(['PATIENT'])], loadChildren: () => import('./patient/patient.module').then(m => m.PatientModule) },
  { path: 'chat', loadChildren: () => import('./ai-chat/ai-chat.module').then(m => m.AiChatModule) },
  { path: 'pharmacist', canActivate: [roleGuard(['PHARMACIST'])], loadChildren: () => import('./pharmacist/pharmacist.module').then(m => m.PharmacistModule) },
  { path: 'prescription', loadChildren: () => import('./prescription/prescription.module').then(m => m.PrescriptionModule) },
  { path: '**', redirectTo: 'home' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }