import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ChartModule } from 'primeng/chart';
import { CardModule } from 'primeng/card';
import { ChartsComponent } from './charts.component';

@NgModule({
	declarations: [ChartsComponent],
	imports: [CommonModule, ChartModule, CardModule],
	exports: [ChartsComponent]
})
export class ChartsModule {}



