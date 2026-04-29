import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders, HttpResponse } from '@angular/common/http';
import { Subject, Observable, map } from 'rxjs';
import { Cache } from './basket-cache'
import { FrontendNeed } from './frontend/types';
import { Pledge } from './pledge-interface';

@Injectable({
  providedIn: 'root',
})
export class AppService {
  private confirmResolver: ((confirmed: boolean) => void) | null = null;
  loggedIn = false;
  err = false;
  takenSource = new Subject<boolean>
  usrTaken$ = this.takenSource.asObservable();
  private http = inject(HttpClient);

  public isManager: boolean = false;
  public currentlyEditingNeed: FrontendNeed | null = null;
  public needEditState: boolean | null = null;

  private readonly GROQ_API_KEY = 'REDACTED'; // Thanks, Groq!

  private alertSource = new Subject<string | null>();
  alertMessage$ = this.alertSource.asObservable();
  private confirmSource = new Subject<{ message: string; confirmText: string; cancelText: string } | null>();
  confirmMessage$ = this.confirmSource.asObservable();

  displayAlert(message: string) {
    this.alertSource.next(message);
  }

  closeAlert() {
    this.alertSource.next(null);
  }

  confirmAction(
    message: string,
    confirmText: string = 'Proceed',
    cancelText: string = 'Cancel'
  ): Promise<boolean> {
    if (this.confirmResolver) {
      this.confirmResolver(false);
      this.confirmResolver = null;
    }

    this.confirmSource.next({ message, confirmText, cancelText });

    return new Promise<boolean>((resolve) => {
      this.confirmResolver = resolve;
    });
  }

  resolveConfirm(confirmed: boolean) {
    this.confirmSource.next(null);
    if (this.confirmResolver) {
      const resolver = this.confirmResolver;
      this.confirmResolver = null;
      resolver(confirmed);
    }
  }

  isEditingNeed() { return this.needEditState !== null && this.needEditState }

  isCreatingNeed() { return this.needEditState !== null && !this.needEditState }

  setEditingNeed() { this.needEditState = true }

  setCreatingNeed() { this.needEditState = false }

  setCurrentlyEditingNeed(need: FrontendNeed) { this.currentlyEditingNeed = need; }

  getPrompt(prompt: string): Observable<string> {
    const url = 'https://api.groq.com/openai/v1/chat/completions';
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${this.GROQ_API_KEY}`,
      'Content-Type': 'application/json'
    });

    const body = {
      model: 'llama-3.1-8b-instant',
      messages: [
        { role: 'system', content: 'You are Ask Harold, a helpful assistant for the important food bank ' + Cache.userName + '. Write ONLY the description paragraph. Do NOT include headers, titles, labels, or bold text at the beginning. Start immediately with the description' },
        { role: 'user', content: prompt }
      ],
      temperature: 0.7,
      max_tokens: 150
    };

    return this.http.post<any>(url, body, { headers }).pipe(
      map(res => res.choices[0].message.content)
    );
  }

  getEmoji(itemName: string, itemType: string): Observable<string> {
    const url = 'https://api.groq.com/openai/v1/chat/completions';
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${this.GROQ_API_KEY}`,
      'Content-Type': 'application/json'
    });

    const body = {
      model: 'llama-3.1-8b-instant',
      messages: [
        {
          role: 'system',
          content: 'You are an icon assistant. Return ONLY a single emoji that best represents the item provided. No text, no explanation, no period. Just the emoji.'
        },
        { role: 'user', content: `${itemName} (${itemType})` }
      ],
      temperature: 0.5
    };

    return this.http.post<any>(url, body, { headers }).pipe(
      map(res => res.choices[0].message.content.trim())
    );
  }

  logIn(creds: { username: string; password: string } | undefined, callback?: () => void): void {
    const headers = new HttpHeaders(creds ? {
      authorization: 'Basic ' + btoa(creds.username + ':' + creds.password)
    } : {});

    this.http.get<{ username: string, id: number, accountType: string }>('user/details', {
      headers
    }).subscribe({
      next: (response) => {
        this.loggedIn = !!response?.username;
        if (callback) {
          callback();
        }
        this.isManager = response.accountType === "Manager";
        Cache.cacheId(response.id);
        Cache.cacheName(response.username || "???");
        if (this.loggedIn && response.id != null) {
          this.syncBasketFromServer(response.id);
        }
      },
      error: () => {
        this.loggedIn = false;
        this.err = true;
      }
    });
  }

  getWorldMap() {
    return this.http.get<any[]>('http://localhost:8080/user/worldmap');
  }


  /** Loads basket from server, shows food-bank messages (e.g. archived need removed), updates local cache. */
  private syncBasketFromServer(userId: number): void {
    this.http.get<{ pledges: Pledge[]; messages: string[] }>('pledgeBasket/user/' + userId + '/need/').subscribe({
      next: (data) => {
        const msgs = data?.messages;
        if (msgs != null && msgs.length > 0) {
          this.displayAlert(msgs.join('\n\n'));
        }
        if (data?.pledges != null) {
          Cache.cacheBasket(data.pledges);
        }
      },
      error: () => { /* ignore */ },
    });
  }

  searchNeeds(query: string | null) {
    if (query == null || query.trim().length === 0) {
      return this.http.get<FrontendNeed[]>('cupboard');
    } else {
      return this.http.get<FrontendNeed[]>('cupboard/?name=' + query);
    }
  }

  getNeedIds(ids: number[]) {
    if (ids.length == 0) return;
    const idsString = ids.join(",");
    return this.http.get<FrontendNeed[]>('cupboard/multiple?commaSeparatedIds=' + idsString);
  }

  deleteNeed(id: number) {
    return this.http.delete('cupboard/' + id);
  }

  getArchivedNeeds() {
    return this.http.get<FrontendNeed[]>('cupboard/archive');
  }

  restoreNeed(id: number) {
    return this.http.post<FrontendNeed>('cupboard/archive/' + id + '/restore', {});
  }

  permanentlyDeleteNeed(id: number) {
    return this.http.delete<FrontendNeed>('cupboard/archive/' + id);
  }

  createNeed(need: FrontendNeed) {
    return this.http.post<FrontendNeed>('cupboard/need', need);
  }

  editNeed(need: FrontendNeed) {
    return this.http.put<FrontendNeed>('cupboard', need);
  }

  addPledge(pledge: Pledge) {
    return this.http.get<Pledge>(`pledgeBasket/user/${pledge.ownerId}/pledge?needId=${pledge.needId}&amount=${pledge.quantity}&money=${pledge.money}`)
  }

  removePledge(ownerId: number, pledgeId: number) {
    return this.http.delete<Pledge>(`pledgeBasket/user/${ownerId}/pledge?pledgeId=${pledgeId}`);
  }

  checkout() {
    return this.http.get('pledgeBasket/user/' + Cache.currentUserId + '/checkout/')
  }

  register(
    creds: { username: string; password: string; accountType: string; location?: { lat: number; lng: number } | null },
    callback?: () => void
  ): void {
    const lat = creds.location?.lat ?? 0;
    const lng = creds.location?.lng ?? 0;
    const url = `user/create?username=${creds.username}&password=${creds.password}&accountType=${creds.accountType}&latitude=${lat}&longitude=${lng}`;

    this.http.get<{ username: string, id: number, accountType: string, status: number }>(url, { observe: 'response' }).subscribe({
      next: (response) => {
        if (response instanceof HttpResponse && response.body != null) {
          this.loggedIn = !!response.body.username;
          if (callback) {
            callback();
          }
          this.isManager = response.body.accountType === "Manager";
          Cache.cacheId(response.body.id);
          Cache.cacheName(response.body.username || "???");
          // if (this.loggedIn && response.id != null) { // This was a merge conflict and I don't know how to resolve it
          //   this.syncBasketFromServer(response.id); // It errors if the code is there so it's commented out for now.
          // }
        }
      },
      error: (response) => {
        if (response.status == 409) {
          this.takenSource.next(true);
        }
        this.loggedIn = false;
        this.err = true;
      }
    });
  }
}