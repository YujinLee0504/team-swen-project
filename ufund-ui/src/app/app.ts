import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { AppService } from './app-service';
import { finalize } from 'rxjs';
import { Cache } from './basket-cache';
import { Pledge } from './pledge-interface';
@Component({
  selector: 'app-root',
  templateUrl: './app.html',
  standalone: false,
  styleUrl: './app.css'
})
export class App {
  private app = inject(AppService);
  private http = inject(HttpClient);
  private router = inject(Router);
  constructor() {
    this.app.logIn(undefined, undefined);
  }
  loggedIn(): boolean {
    return this.app.loggedIn;
  }
  logout(): void {
    this.http.post('logout', {}).pipe(
      finalize(() => {
        this.app.loggedIn = false;
        this.router.navigateByUrl('/home');
      })
    ).subscribe();
  }
  home(): void {
    this.router.navigateByUrl('/home');
  }
  worldMap(): void {
    this.router.navigateByUrl('/world-map');
  }

  isManager(): boolean {
    return this.app.isManager;
  }

  isAdminAccount(): boolean {
    return Cache.userName === "admin" && this.loggedIn();
  }

  checkout(): void {
    this.http.get<{ pledges: Pledge[]; messages: string[] }>('pledgeBasket/user/' + Cache.currentUserId + '/need/').subscribe({
      next: (data) => {
        const msgs = data?.messages;
        if (msgs != null && msgs.length > 0) {
          this.app.displayAlert(msgs.join('\n\n'));
        }
        Cache.cacheBasket(data?.pledges ?? []);
        this.router.navigateByUrl('/checkout');
      },
      error: (err) => {
        this.app.displayAlert('Unable to check out.'); console.error(err);
      }
    });
  }
}
