import { Component, HostListener, OnDestroy, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../auth/auth.service';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit, OnDestroy {
  isLoggedIn = false;
  currentUser: any;
  scrolled = false;
  mobileOpen = false;
  contactForm!: FormGroup;
  submitting = false;
  sent = false;
  private intersectionObserver?: IntersectionObserver;

  constructor(private router: Router, private authService: AuthService, private fb: FormBuilder) {
    this.isLoggedIn = this.authService.isLoggedIn();
    if (this.isLoggedIn) {
      this.currentUser = this.authService.getCurrentUser();
    }
  }

  ngOnInit(): void {
    this.contactForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      message: ['', [Validators.required, Validators.minLength(10)]]
    });

    // Intersection Observer for reveal animations
    if ('IntersectionObserver' in window) {
      this.intersectionObserver = new IntersectionObserver(entries => {
        entries.forEach(entry => {
          if (entry.isIntersecting) {
            (entry.target as HTMLElement).classList.add('reveal-visible');
            this.intersectionObserver?.unobserve(entry.target);
          }
        });
      }, { threshold: 0.15 });

      const sections = document.querySelectorAll('.section.reveal');
      sections.forEach(sec => this.intersectionObserver?.observe(sec));
    }
  }

  ngOnDestroy(): void {
    this.intersectionObserver?.disconnect();
  }

  @HostListener('window:scroll')
  onWindowScroll() {
    this.scrolled = window.scrollY > 10;
  }

  toggleMobile() {
    this.mobileOpen = !this.mobileOpen;
  }

  goToLogin() {
    this.router.navigate(['/auth/login']);
  }

  goToRegister() {
    this.router.navigate(['/auth/signup']);
  }

  goToDashboard() {
    if (this.currentUser?.role === 'DOCTOR') {
      this.router.navigate(['/doctor/dashboard']);
    } else if (this.currentUser?.role === 'PHARMACIST') {
      this.router.navigate(['/pharmacist/dashboard']);
    }
    else if (this.currentUser?.role === 'PATIENT') {
      this.router.navigate(['/patient/dashboard']);
    }
  }

  logout() {
    this.authService.logout();
    this.isLoggedIn = false;
    this.currentUser = null;
    this.router.navigate(['/home']);
  }

  scrollTo(sectionId: string) {
    const target = document.getElementById(sectionId);
    if (!target) { return; }
    target.scrollIntoView({ behavior: 'smooth', block: 'start' });
    if (this.mobileOpen) {
      this.mobileOpen = false;
    }
  }

  submitContact() {
    if (this.contactForm.invalid) { return; }
    this.submitting = true;
    // Simulate async submission
    setTimeout(() => {
      this.submitting = false;
      this.sent = true;
      this.contactForm.reset();
      setTimeout(() => { this.sent = false; }, 3000);
    }, 1000);
  }
}