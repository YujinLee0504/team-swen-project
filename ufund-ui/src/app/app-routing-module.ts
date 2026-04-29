import { NgModule } from '@angular/core';
import { RouterModule, Routes, provideRouter } from '@angular/router';
import { authGuard } from './auth-guard';
import { Login } from './login/login'
import { Home } from './home/home'
import { Checkout } from './checkout/checkout';
import { Register } from './register/register';
import { NeedEditor } from './need-editor/need-editor';
import { PledgeEditor } from './pledge-editor/pledge-editor';
import { WorldMap } from './world-map/world-map';
import { NeedArchive } from './need-archive/need-archive';
export const routes: Routes = [
  { path: 'home', component: Home},
  { path: 'register', component: Register},
  { path: 'login', component: Login, canActivate: [authGuard]},
  { path: 'checkout', component: Checkout },
  { path: 'editor', component: NeedEditor },
  { path: 'pledge', component: PledgeEditor },
  { path: 'world-map', component: WorldMap },
  { path: 'archive', component: NeedArchive },
  { path: '', pathMatch: 'full', redirectTo: 'home'},
  { path: '**', redirectTo: 'home' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
