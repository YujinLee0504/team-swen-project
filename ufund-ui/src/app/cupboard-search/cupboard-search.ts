import { Component, inject, OnDestroy, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { AppService } from '../app-service';
import { FrontendNeed } from '../frontend/types';
import { Router, ActivatedRoute } from '@angular/router';
import { Cache } from '../basket-cache';

@Component({
  selector: 'app-cupboard-search',
  standalone: false,
  templateUrl: './cupboard-search.html',
  styleUrl: './cupboard-search.css',
})
export class CupboardSearch implements OnInit, OnDestroy {
  private app = inject(AppService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private http = inject(HttpClient);

  needs: FrontendNeed[] = [];
  allNeeds: FrontendNeed[] = []; // Store full list for client-side filtering
  selectedCreator: string | null = null;
  private currentQuery: string = "";
  private pollingId: ReturnType<typeof setInterval> | null = null;

  ngOnInit(): void {
    this.route.queryParamMap.subscribe(params => {
      this.selectedCreator = params.get('creatorName');
      this.search(this.currentQuery);
    });
    this.pollingId = setInterval(() => {
      this.search(this.currentQuery);
    }, 3000);
  }

  ngOnDestroy(): void {
    if (this.pollingId != null) {
      clearInterval(this.pollingId);
      this.pollingId = null;
    }
  }

  search(query: string) {
    this.currentQuery = query;
    this.app.searchNeeds(query).subscribe({
      next: (data) => {
        const expandedIds = new Set(
          this.allNeeds.filter(n => n.isExpanded).map(n => n.id)
        );

        this.allNeeds = data.map(need => ({ // ensure that expanded needs retain that status
          ...need,
          isExpanded: expandedIds.has(need.id)
        }));

        this.applyFilters();
      },
      error: (err) => { this.app.displayAlert("There was an error while searching."); console.error(err); }
    })
  }

  // New method to handle logic for creator filtering
  applyFilters() {
    if (this.selectedCreator) {
      this.needs = this.allNeeds.filter(n => n.creatorName === this.selectedCreator);
    } else {
      this.needs = this.allNeeds;
    }
  }

  filterByCreator(name: string) {
    // Navigate to the same page but with the query param
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { creatorName: name },
      queryParamsHandling: 'merge'
    });
  }

  clearCreatorFilter() {
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { creatorName: null }, // Removes the param
      queryParamsHandling: 'merge'
    });
  }

  isManager() {
    return this.app.isManager;
  }

  isUser() {
    return !this.app.isManager;
  }

  isAdminAccount() {
    return Cache.userName === "admin";
  }

  isLocalManagerUser(userId: number) { // return true if user OR is admin OR food bank manager & matches ID
    if (this.isAdminAccount()) return true;

    if (this.isUser()) return true;

    return Cache.currentUserId === userId;
  }

  editNeed(need: FrontendNeed) {
    this.app.setCurrentlyEditingNeed(need);
    this.app.setEditingNeed();
    this.router.navigateByUrl('/editor');
  }
  deleteNeed(need: FrontendNeed) {
    if (!this.isManager()) return;
    this.app.deleteNeed(need.id).subscribe({
      next: () => { this.search(this.currentQuery); },
      error: (err) => { this.app.displayAlert("Failed to delete. Please try again!"); console.error(err) }
    });
  }

  addPledge(need: FrontendNeed) {
    if (!this.isUser()) return;
    this.app.setCurrentlyEditingNeed(need);
    this.router.navigateByUrl('pledge');
  }


  goToArchive() {
    if (!this.isManager()) return;
    this.router.navigateByUrl('/archive');
  }

  createNeed() {
    // this.app.setCurrentlyEditingNeed(need);
    this.app.setCreatingNeed();
    this.router.navigateByUrl('/editor');

    /*
    let need: FrontendNeed = {
      id: 0,
      name: 'New Need',
      cost: 100,
      quantity: 100,
      type: 'Test',
      completionPercentage: 0
    }
      */
  }
}
