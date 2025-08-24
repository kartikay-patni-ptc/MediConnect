import { Component, OnInit } from '@angular/core';
import { AuthService } from './auth/auth.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  title = 'mediconnect-frontend';

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    // Validate and cleanup any invalid/expired tokens on app startup
    this.authService.validateAndCleanupToken();
  }
}