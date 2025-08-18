import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TableModule } from 'primeng/table';
import { CardModule } from 'primeng/card';
import { ButtonModule } from 'primeng/button';
import { AccordionModule } from 'primeng/accordion';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { InputTextareaModule } from 'primeng/inputtextarea';
import { HistoryComponent } from './history.component';

@NgModule({
	declarations: [HistoryComponent],
    imports: [CommonModule, FormsModule, TableModule, CardModule, ButtonModule, AccordionModule, DialogModule, InputTextModule, InputTextareaModule],
	exports: [HistoryComponent]
})
export class HistoryModule {}



