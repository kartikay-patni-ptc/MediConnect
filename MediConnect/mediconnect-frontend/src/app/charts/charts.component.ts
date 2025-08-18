import { Component, OnInit } from '@angular/core';
import { ChartData, ChartOptions } from 'chart.js';

@Component({
	selector: 'app-charts',
	templateUrl: './charts.component.html',
	styleUrls: ['./charts.component.css']
})
export class ChartsComponent implements OnInit {
	medicationOverTime: ChartData<'line'> = { labels: [], datasets: [] };
	allergyBreakdown: ChartData<'doughnut'> = { labels: [], datasets: [] };
  options: ChartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { position: 'bottom', labels: { boxWidth: 10 } },
      tooltip: { enabled: true }
    },
    layout: { padding: 8 },
    scales: {
      x: { grid: { display: false }, ticks: { maxTicksLimit: 6 } },
      y: { grid: { color: '#eef2ff' }, ticks: { stepSize: 1 } }
    }
  };

	ngOnInit(): void {
		this.mock();
	}

	private mock() {
		this.medicationOverTime = {
			labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun'],
			datasets: [
				{ label: 'Medications Taken', data: [3, 2, 4, 5, 3, 2], borderColor: '#3b82f6', backgroundColor: 'rgba(59,130,246,.2)', tension: .3 }
			]
		};

		this.allergyBreakdown = {
			labels: ['Pollen', 'Dust', 'Peanuts', 'Seafood'],
			datasets: [
				{ data: [40, 25, 20, 15], backgroundColor: ['#4f46e5', '#06b6d4', '#10b981', '#f59e0b'] }
			]
		};
	}
}



