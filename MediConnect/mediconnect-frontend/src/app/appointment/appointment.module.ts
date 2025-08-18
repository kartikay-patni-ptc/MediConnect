import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { DialogModule } from 'primeng/dialog';
import { DropdownModule } from 'primeng/dropdown';
import { InputTextareaModule } from 'primeng/inputtextarea';
import { ButtonModule } from 'primeng/button';
import { BookAppointmentDialogComponent } from './book-appointment-dialog.component';

@NgModule({
  declarations: [BookAppointmentDialogComponent],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    DialogModule,
    DropdownModule,
    InputTextareaModule,
    ButtonModule
  ],
  exports: [BookAppointmentDialogComponent]
})
export class AppointmentModule {}

