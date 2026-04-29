import { Component, inject } from '@angular/core';
import { AppService } from '../app-service';
import { FrontendNeed } from '../frontend/types';
import { Location } from '@angular/common'
import { Cache } from '../basket-cache'

@Component({
  selector: 'app-need-editor',
  standalone: false,
  templateUrl: './need-editor.html',
  styleUrl: './need-editor.css',
})
export class NeedEditor {
  app = inject(AppService);

  constructor(private location: Location) { }

  name: string = '';
  desc: string = '';
  icon: string = '';
  type: string = '';
  cost: number = 1.00;
  quantity: number = 5;

  ngOnInit() {
    if (this.app.isCreatingNeed()) {
      this.app.currentlyEditingNeed = {
        id: 0,
        creatorId: Cache.currentUserId,
        creatorName: Cache.userName,
        desc: '',
        icon: '',
        name: '',
        cost: 0.01,
        quantity: 0,
        type: 'Food',
        completionPercentage: 0
      };
    } else if (this.app.isEditingNeed() && this.app.currentlyEditingNeed) {
      const need = this.app.currentlyEditingNeed;
      this.name = need.name;
      this.desc = need.desc;
      this.icon = need.icon || ''; // optional
      this.type = need.type;
      this.cost = need.cost;
      this.quantity = need.quantity;
    }
  }

  isGenerating: boolean = false;

  generateAIDescription() {
    this.isGenerating = true;

    let prompt = `Generate a short, professional description for a donation need. 
                Name: ${this.name}, 
                Category: ${this.type}.`;

    if (this.cost > 0) prompt += ` The cost per unit is $${this.cost.toFixed(2)}.`;
    if (this.quantity > 0) prompt += ` We are looking to collect ${this.quantity} units.`;

    prompt += ` Keep the description under 3 sentences and focus on how this helps the community.`;
    prompt += ` Write ONLY the paragraph. Do not include a title like "Donation Need".`;

    this.app.getPrompt(prompt).subscribe({
      next: (aiResponse: string) => {
        this.desc = aiResponse.trim();
        this.isGenerating = false;
      },
      error: (err) => {
        console.error("Harold is having a nap:", err);
        this.isGenerating = false;
        this.desc = "Sorry, I couldn't generate a description right now. Please try again!";
      }
    });
  }

  generateEmoji() {
    if (!this.name || !this.type || this.isGenerating) return;

    this.isGenerating = true;
    this.app.getEmoji(this.name, this.type).subscribe({
      next: (icon) => {
        this.icon = icon;
        this.isGenerating = false;
      },
      error: () => {
        this.isGenerating = false;
      }
    });
  }

  removeIcon() {
    this.icon = '';
  }

  onCancel() {
    this.location.back();
  }

  onSubmit() {
    if (this.app.currentlyEditingNeed == null) return;
    let need: FrontendNeed = this.app.currentlyEditingNeed as FrontendNeed;

    need.name = this.name;
    need.desc = this.desc;
    need.icon = this.icon;
    need.type = this.type;
    need.cost = this.cost;
    need.quantity = this.quantity;

    console.log(need);

    if (this.app.isCreatingNeed()) {
      this.app.createNeed(need).subscribe({
        next: () => { this.location.back() },
        error: (err) => { this.app.displayAlert("Unable to create this Need. You may have a duplicate name."); console.error(err) }
      });
    } else if (this.app.isEditingNeed()) {
      this.app.editNeed(need).subscribe({
        next: () => { this.location.back(); },
        error: (err) => { this.app.displayAlert("Unable to save edits."); console.error(err) }
      });
    }
  }
}
