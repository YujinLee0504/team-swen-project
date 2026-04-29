import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AppService } from './app-service';

export const authGuard: CanActivateFn = () => {
  const app = inject(AppService);
  const router = inject(Router);

  if (app.loggedIn) {
    router.navigate(['']);
    return false;
  }
  return true;
};
