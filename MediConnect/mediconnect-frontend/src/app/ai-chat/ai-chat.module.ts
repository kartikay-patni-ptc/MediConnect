import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CardModule } from 'primeng/card';
import { ButtonModule } from 'primeng/button';
import { InputTextareaModule } from 'primeng/inputtextarea';
import { InputTextModule } from 'primeng/inputtext';
import { PanelModule } from 'primeng/panel';
import { DialogModule } from 'primeng/dialog';
import { TooltipModule } from 'primeng/tooltip';
import { AiChatPageComponent } from './ai-chat.page';
import { AiChatRoutingModule } from './ai-chat-routing.module';
import { AppointmentModule } from '../appointment/appointment.module';

@NgModule({
    	declarations: [AiChatPageComponent],
    	imports: [
    		CommonModule, 
    		FormsModule, 
    		CardModule, 
    		ButtonModule, 
    		InputTextareaModule, 
    		InputTextModule, 
    		PanelModule, 
    		DialogModule, 
    		TooltipModule,
    		AiChatRoutingModule, 
    		AppointmentModule
    	],
    	exports: [AiChatPageComponent]
})
export class AiChatModule {}


