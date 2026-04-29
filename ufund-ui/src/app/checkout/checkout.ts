import { Component, inject } from '@angular/core';
import { Location } from '@angular/common'

import { Pledge } from '../pledge-interface'
import { CommonModule, DecimalPipe } from '@angular/common';
import { Cache } from '../basket-cache'
import { AppService } from '../app-service';
import { FrontendNeed } from '../frontend/types';
import { Router } from '@angular/router';

@Component({
  selector: 'app-checkout',
  standalone: false,
  templateUrl: './checkout.html',
  styleUrl: './checkout.css',
})
export class Checkout {
  constructor(private location: Location, private router: Router) { }

  private app = inject(AppService);

  pledgeBasketListCache: Pledge[] = Cache.cache;

  private needMap: Map<number, FrontendNeed> = new Map<number, FrontendNeed>();
  private needNameMap: Map<number, string> = new Map<number, string>();

  ngOnInit() {
    this.loadCache();
  }

  loadCache() {
    let ids: number[] = [];
    this.pledgeBasketListCache.forEach((pledge) => {
      if (!ids.includes(pledge.needId)) ids.push(pledge.needId);
    });

    this.app.getNeedIds(ids)?.subscribe({
      next: (needs) => {
        (needs as FrontendNeed[]).forEach((need) => {
          if (!need.is_deleted) {
            this.needMap.set(need.id, need);
            this.needNameMap.set(need.id, need.name)
          } }
        );
        console.log(this.needNameMap);
      },
      error: (err) => { this.app.displayAlert("Failed to load Need names from server into cache."); console.error(err) }
    })
  }

  needExistsLocally(id: number) {
    return this.needNameMap.get(id) ? true : false;
  }

  getNeedName(id: number) {
    return this.needNameMap.get(id) || "Product";
  }

  private getCheckoutContribution(pledge: Pledge, need: FrontendNeed): number {
    const quantityContribution = pledge.quantityPledged ?? pledge.quantity ?? 0;
    const moneyContribution = need.cost > 0 ? (pledge.moneyPledged ?? pledge.money ?? 0) / need.cost : 0;
    return quantityContribution + moneyContribution;
  }

  private getSurplusWarnings(): string[] {
    const basketContributionByNeed = new Map<number, number>();

    this.pledgeBasketListCache.forEach((pledge) => {
      const need = this.needMap.get(pledge.needId);
      if (!need) {
        return;
      }

      const runningTotal = basketContributionByNeed.get(pledge.needId) ?? 0;
      basketContributionByNeed.set(
        pledge.needId,
        runningTotal + this.getCheckoutContribution(pledge, need)
      );
    });

    return Array.from(basketContributionByNeed.entries())
      .map(([needId, addedQuantity]) => {
        const need = this.needMap.get(needId);
        if (!need) {
          return null;
        }

        const alreadyFulfilled = need.quantityFulfilled ?? 0;
        const remaining = Math.max(need.quantity - alreadyFulfilled, 0);
        const surplus = addedQuantity - remaining;

        if (surplus <= 0) {
          return null;
        }

        return `${need.name} would go over by ${surplus.toFixed(2)} item(s).`;
      })
      .filter((warning): warning is string => warning !== null);
  }

  goBack() {
    this.location.back();
  }

  removePledge(pledgeId: number) {
    this.app.removePledge(Cache.currentUserId, pledgeId).subscribe({
      next: () => {
        /*
        this.router.navigateByUrl('/', { skipLocationChange: true }).then(() => {
          this.router.navigate(['/checkout']);
        });
        */
        this.location.back();
      },
      error: (err) => { this.app.displayAlert("Failed to remove Pledge from checkout."); console.error(err) }
    });
  }

  async confirmCheckout() {
    const surplusWarnings = this.getSurplusWarnings();
    if (surplusWarnings.length > 0) {
      const shouldProceed = await this.app.confirmAction(
        `This checkout will create a surplus:\n\n${surplusWarnings.join('\n')}\n\nDo you want to proceed?`,
        'Proceed',
        'Cancel'
      );

      if (!shouldProceed) {
        return;
      }
    }

    this.app.checkout().subscribe({
      next: () => { this.location.back(); },
      error: (err) => { this.app.displayAlert("Unable to check out."); console.error(err) }
    });;
  }
}
