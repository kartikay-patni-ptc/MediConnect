import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { DashboardComponent } from './dashboard/dashboard.component';
import { DoctorRoutingModule } from './doctor-routing.module';


// PrimeNG modules
import { PanelMenuModule } from 'primeng/panelmenu';
import { CardModule } from 'primeng/card';
import { CalendarModule } from 'primeng/calendar';
import { ButtonModule } from 'primeng/button';
import { AvatarModule } from 'primeng/avatar';
import { TableModule } from 'primeng/table';
import { BadgeModule } from 'primeng/badge';
import { DividerModule } from 'primeng/divider';
import { TagModule } from 'primeng/tag';
import { ToolbarModule } from 'primeng/toolbar';
import { InputTextModule } from 'primeng/inputtext';
import { BreadcrumbModule } from 'primeng/breadcrumb';
import { SidebarModule } from 'primeng/sidebar';
import { TooltipModule } from 'primeng/tooltip';
import { ToastModule } from 'primeng/toast';
import { SkeletonModule } from 'primeng/skeleton';
import { MessageService } from 'primeng/api';
import { MenuModule } from 'primeng/menu';
import { SplitButtonModule } from 'primeng/splitbutton';
import { DialogModule } from 'primeng/dialog';
import { FileUploadModule } from 'primeng/fileupload';

@NgModule({
  declarations: [DashboardComponent],
  imports: [
    DoctorRoutingModule,
    CommonModule,
    FormsModule,
    HttpClientModule,
    PanelMenuModule,
    CardModule,
    CalendarModule,
    ButtonModule,
    AvatarModule,
    TableModule,
    BadgeModule,
    DividerModule,
    TagModule,
    ToolbarModule,
    InputTextModule,
    BreadcrumbModule,
    SidebarModule,
    TooltipModule,
    ToastModule,
    SkeletonModule,
    MenuModule,
    SplitButtonModule,
    DialogModule,
    FileUploadModule
  ],
  providers: [MessageService]
})
export class DoctorModule { }
