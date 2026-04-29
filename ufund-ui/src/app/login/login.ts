import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AppService } from '../app-service';
import { NgIf } from '@angular/common';
@Component({
  selector: 'app-login',
  standalone: false,
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  attempt = false;
  passHidden = true;
  creds = {username: '', password: ''};
  protected app = inject(AppService);
  private router = inject(Router);
  login(): boolean {
    this.attempt = true;
    this.app.logIn(this.creds, () => {
      this.router.navigateByUrl('/');
    });
    return false;
  }
  togglePass() {
    this.passHidden = !this.passHidden;
  }
}
