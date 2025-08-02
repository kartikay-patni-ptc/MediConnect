import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MessageService } from 'primeng/api';

@Component({
  selector: 'app-signup',
  templateUrl: './signup.component.html',
  styleUrls: ['./signup.component.css']
})
export class SignupComponent implements OnInit {
  signupForm!: FormGroup;
  roles = [
    { label: 'Doctor', value: 'doctor' },
    { label: 'Pharmacist', value: 'pharmacist' },
    { label: 'Patient', value: 'patient' }
  ];

  constructor(private fb: FormBuilder, private messageService: MessageService) {}

  ngOnInit(): void {
    this.signupForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [
        Validators.required,
        Validators.minLength(8),
        this.securePasswordValidator
      ]],
      confirmPassword: ['', Validators.required],
      role: ['', Validators.required]
    }, {
      validators: this.passwordMatchValidator
    });
  }

  // Custom validator for secure password
  securePasswordValidator(control: any) {
    const value = control.value || '';
    // At least 8 chars, 1 uppercase, 1 lowercase, 1 number, 1 special char
    const pattern = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*()_+\-=[\]{};':"\\|,.<>/?]).{8,}$/;
    return pattern.test(value) ? null : { insecurePassword: true };
  }
  passwordMatchValidator(form: FormGroup) {
    return form.get('password')?.value === form.get('confirmPassword')?.value
      ? null : { mismatch: true };
  }

  onSubmit() {
    if (this.signupForm.valid) {
      this.messageService.add({
        severity: 'success',
        summary: 'Signup Successful',
        detail: 'Your account has been created!'
      });
      const selectedRole = this.signupForm.get('role')?.value;
      setTimeout(() => {
        if (selectedRole === 'doctor') {
          window.location.href = '/auth/verify-doctor';
        }
        else {
        window.location.href = '/auth/login';
      }
      }, 1200);
    } else {
      this.signupForm.markAllAsTouched();
      const passwordControl = this.signupForm.get('password');
      if (passwordControl?.errors?.['insecurePassword']) {
        this.messageService.add(({
          severity: 'error',
          summary: 'Weak Password',
          detail: 'Password must have:\n- 8+ characters\n- Uppercase & lowercase\n- Number\n- Special character',
          escape: false
        }) as any);
      }
    }
  }
}
