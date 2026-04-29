import { Component, inject } from '@angular/core';
import { AppService } from '../app-service';
import { Cache } from '../basket-cache'

@Component({
  selector: 'app-home',
  standalone: false,
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class Home {
  title = 'test';

  private app = inject(AppService);

  loggedIn() : boolean {
    return this.app.loggedIn;
  }

  userName(): string {
    return Cache.userName;
  }
}
