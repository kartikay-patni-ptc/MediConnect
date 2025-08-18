import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AiChatPageComponent } from './ai-chat.page';

const routes: Routes = [
	{ path: '', component: AiChatPageComponent }
];

@NgModule({
	imports: [RouterModule.forChild(routes)],
	exports: [RouterModule]
})
export class AiChatRoutingModule {}



