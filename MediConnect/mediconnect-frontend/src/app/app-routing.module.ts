
import { NgModule } from '@angular/core';
import { RouterModule,Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: 'auth/login', pathMatch: 'full' },
  { path: 'auth', loadChildren: () => import('./auth/auth.module').then(m => m.AuthModule) },
  { path: 'doctor', loadChildren: () => import('./doctor/doctor.module').then(m => m.DoctorModule) },
  // { path: '', redirectTo: 'doctor/dashboard', pathMatch: 'full' },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
