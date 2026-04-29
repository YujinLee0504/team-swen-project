import { Component, inject } from '@angular/core';
import { AppService } from '../app-service';
import { FrontendNeed } from '../frontend/types';
import { Location } from '@angular/common'
import { Pledge } from '../pledge-interface';
import { Cache } from '../basket-cache';

@Component({
  selector: 'app-pledge-editor',
  standalone: false,
  templateUrl: './pledge-editor.html',
  styleUrl: './pledge-editor.css',
})
export class PledgeEditor {
  app = inject(AppService);

  constructor(private location: Location) { }

  name: string = "???";
  money: number = 0;
  quantity: number = 0;

  ngOnInit() {
    this.name = this.app.currentlyEditingNeed?.name || "???";
  }

  onCancel() {
    this.location.back();
  }

  onSubmit() {
    if (this.app.currentlyEditingNeed == null) return;
    let pledge: Pledge = {
      id: 0,
      money: this.money,
      moneyPledged: this.money,
      needId: this.app.currentlyEditingNeed.id,
      ownerId: Cache.currentUserId,
      quantity: this.quantity,
      quantityPledged: this.quantity
    }

    this.app.addPledge(pledge).subscribe({
        next: () => { this.location.back() },
        error: (err) => { this.app.displayAlert("Failed to create pledge! Please try again."); console.error(err) }
      });

  }
}
