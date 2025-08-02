import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

@Component({
  selector: 'app-verify-doctor',
  templateUrl: './verify-doctor.component.html',
  styleUrl: './verify-doctor.component.css'
})
export class VerifyDoctorComponent implements OnInit {
  verifyForm!: FormGroup;

  constructor(private fb: FormBuilder, private http: HttpClient) {}

  ngOnInit(): void {
    this.verifyForm = this.fb.group({
      name: ['', Validators.required],
      council: ['', Validators.required],
      registrationNumber: ['', Validators.required],
      year: ['']
    });
  }

  onVerify() {
    const data = this.verifyForm.value;
    this.http.post('/api/verify-doctor', data).subscribe({
      next: res => {
        // Show verified message or redirect
      },
      error: err => {
        // Show error
      }
    });
  }
}
