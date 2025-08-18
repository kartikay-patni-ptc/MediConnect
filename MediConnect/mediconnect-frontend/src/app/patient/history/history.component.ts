import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../auth/auth.service';
import { HttpClient } from '@angular/common/http';

@Component({
	selector: 'app-history',
	templateUrl: './history.component.html',
	styleUrls: ['./history.component.css']
})
export class HistoryComponent implements OnInit {
	medications: any[] = [];
	allergies: any[] = [];
	reports: any[] = [];
	isLoading = false;
	showAdd = false;
	form: any = { medicationName: '', dosage: '', frequency: '', startDate: '', endDate: '', notes: '' };
	editingId: number | null = null;

	constructor(private auth: AuthService, private http: HttpClient) {}

	ngOnInit(): void {
		this.loadHistory();
	}

	loadHistory() {
		this.isLoading = true;
		const userId = this.auth.getUserId();
		this.auth.getPatientHistory(userId).subscribe({
			next: (data: any) => {
				this.medications = data.medications || [];
				this.allergies = data.allergies || [];
				this.reports = data.reports || [];
			},
			complete: () => this.isLoading = false,
			error: () => this.isLoading = false
		});
	}

	openAdd() {
		this.showAdd = true;
		this.editingId = null;
		this.form = { medicationName: '', dosage: '', frequency: '', startDate: '', endDate: '', notes: '' };
	}

	edit(m: any) {
		this.showAdd = true;
		this.editingId = m.id;
		this.form = { medicationName: m.medicationName, dosage: m.dosage, frequency: m.frequency, startDate: m.startDate, endDate: m.endDate, notes: m.notes };
	}

	save() {
		const patientId = this.auth.getUserId();
		const payload = { ...this.form };
		const req = this.editingId ? this.auth.updateMedication(this.editingId, payload) : this.auth.addMedication(patientId, payload);
		req.subscribe(() => {
			this.showAdd = false;
			this.loadHistory();
		});
	}

	remove(id: number) {
		this.auth.deleteMedication(id).subscribe(() => this.loadHistory());
	}
}


