import { Component, inject } from '@angular/core';
import { AppService } from '../app-service';
import { AsyncPipe } from '@angular/common'; // 1. Import the pipe

@Component({
  selector: 'app-global-modal',
  standalone: true,
  imports: [AsyncPipe],
  templateUrl: './global-modal.html',
  styleUrl: './global-modal.css'
})
export class GlobalModal {
  private app = inject(AppService);
  message$ = this.app.alertMessage$;
  confirm$ = this.app.confirmMessage$;

  close() {
    this.app.closeAlert();
  }

  cancelConfirm() {
    this.app.resolveConfirm(false);
  }

  acceptConfirm() {
    this.app.resolveConfirm(true);
  }
}