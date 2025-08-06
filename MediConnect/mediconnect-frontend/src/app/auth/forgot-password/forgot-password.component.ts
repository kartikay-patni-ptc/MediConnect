import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MessageService } from 'primeng/api';
import { Router } from '@angular/router';
import { AuthService } from '../auth.service';

@Component({
  selector: 'app-forgot-password',
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.css']
})
export class ForgotPasswordComponent implements OnInit {
  forgotPasswordForm!: FormGroup;
  resetPasswordForm!: FormGroup;
  loading = false;
  showResetForm = false;
  username = '';

  constructor(
    private fb: FormBuilder,
    private messageService: MessageService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.forgotPasswordForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3)]]
    });

    this.resetPasswordForm = this.fb.group({
      newPassword: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', [Validators.required]]
    }, { validators: this.passwordMatchValidator });
  }

  passwordMatchValidator(form: FormGroup) {
    const password = form.get('newPassword')?.value;
    const confirmPassword = form.get('confirmPassword')?.value;
    return password === confirmPassword ? null : { passwordMismatch: true };
  }

  onForgotPassword() {
    if (this.forgotPasswordForm.valid) {
      this.loading = true;
      const request = { username: this.forgotPasswordForm.value.username };
      
      this.authService.forgotPassword(request).subscribe({
        next: (response: any) => {
          this.loading = false;
          this.username = request.username;
          this.showResetForm = true;
          
          this.messageService.add({
            severity: 'success',
            summary: 'Reset Instructions Sent',
            detail: 'Password reset instructions have been sent to your email.'
          });
        },
        error: (error) => {
          this.loading = false;
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: error.error || 'Failed to send reset instructions'
          });
        }
      });
    }
  }

  onResetPassword() {
    if (this.resetPasswordForm.valid) {
      this.loading = true;
      const request = {
        username: this.username,
        newPassword: this.resetPasswordForm.value.newPassword
      };
      
      this.authService.resetPassword(request).subscribe({
        next: (response) => {
          this.loading = false;
          this.messageService.add({
            severity: 'success',
            summary: 'Password Reset Successful',
            detail: 'Your password has been reset successfully. You can now login with your new password.'
          });
          
          // Reset forms and go back to login
          setTimeout(() => {
            this.router.navigate(['/auth/login']);
          }, 2000);
        },
        error: (error) => {
          this.loading = false;
          this.messageService.add({
            severity: 'error',
            summary: 'Reset Failed',
            detail: error.error || 'Failed to reset password'
          });
        }
      });
    }
  }

  goBackToLogin() {
    this.showResetForm = false;
    this.forgotPasswordForm.reset();
    this.resetPasswordForm.reset();
    this.router.navigate(['/auth/login']);
  }
}