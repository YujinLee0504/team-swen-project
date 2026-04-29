import { Component, inject, OnDestroy, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AppService } from '../app-service';
import { FrontendNeed } from '../frontend/types';
import { Cache } from '../basket-cache'

@Component({
  selector: 'app-need-archive',
  standalone: false,
  templateUrl: './need-archive.html',
  styleUrl: './need-archive.css',
})
export class NeedArchive implements OnInit, OnDestroy {
  private appService = inject(AppService);
  private router = inject(Router);

  needs: FrontendNeed[] = [];

  confirmPermanentDeleteId: number | null = null;
  loadError = false;
  private pollingId: ReturnType<typeof setInterval> | null = null;
  ngOnInit(): void {
    if (!this.appService.loggedIn || !this.appService.isManager) {
      this.router.navigateByUrl('/home');
      return;
    }
    this.load();
    this.pollingId = setInterval(() => this.load(), 3000);
  }

  ngOnDestroy(): void {
    if (this.pollingId != null) {
      clearInterval(this.pollingId);
      this.pollingId = null;
    }
  }


  load(): void {
    this.loadError = false;
    this.appService.getArchivedNeeds().subscribe({
      next: (data) => {
        this.needs = data ?? [];
  },
      error: (err) => {
        this.loadError = true;
        console.error('Error loading archived needs:', err);
      },
    });
  }

  restore(need: FrontendNeed): void {
    this.appService.restoreNeed(need.id).subscribe({
      next: () => 
        this.load(),
      error: (err) => {
        console.error('Error restoring need:', err);
      },
    });
  }

  isAdminAccount() {
      return Cache.userName === "admin";
    }
  
    isLocalManagerUser(userId: number) { // return true if is admin OR food bank manager & matches ID
      if (this.isAdminAccount()) return true;
  
      return Cache.currentUserId === userId;
    }

  permaDeleteConfirmation(need: FrontendNeed): void {
    this.confirmPermanentDeleteId = need.id;
  }

  confirmPermanentDelete(): void {
    if (this.confirmPermanentDeleteId == null) {
      return;
    }
    const id = this.confirmPermanentDeleteId;
    this.appService.permanentlyDeleteNeed(id).subscribe({
      next: () => {
        this.confirmPermanentDeleteId = null;
        this.load();
      },
      error: (err) => {
        this.appService.displayAlert('Failed to delete permanently. Please try again!');
        console.error(err);
      },
    });
  }

  cancelPermanentDelete(): void {
    this.confirmPermanentDeleteId = null;
  }

  deletedLabel(need: FrontendNeed): string {
    const t = need.time_deleted;
    if (t == null || t === 0) {
      return 'Recently deleted';
    }
    return new Date(t).toLocaleString();
  }


  backToCupboard(): void {
    this.router.navigateByUrl('/home');
  }



}
