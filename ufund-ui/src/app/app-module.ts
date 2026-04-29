import { NgModule, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { routes } from './app-routing-module';
import { xhrInterceptor } from './xhr-interceptor';
import { BrowserModule } from '@angular/platform-browser';
import { AppRoutingModule } from './app-routing-module';
import { App } from './app';
import { Login } from './login/login';
import { Home } from './home/home';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { Checkout } from './checkout/checkout';
import { CupboardSearch } from './cupboard-search/cupboard-search';
import { Register } from './register/register';
import { NeedEditor } from './need-editor/need-editor';
import { CommonModule } from '@angular/common';
import { PledgeEditor } from './pledge-editor/pledge-editor';
import { WorldMap } from './world-map/world-map';
import { NeedArchive } from './need-archive/need-archive';
import { GlobalModal } from './global-modal/global-modal';

@NgModule({
  declarations: [
    App,
    Login,
    Home,
    Checkout,
    CupboardSearch,
    Register,
    NeedEditor,
    PledgeEditor,
    WorldMap,
    NeedArchive,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule,
    ReactiveFormsModule,
    CommonModule,
    GlobalModal
  ],
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient(withInterceptors([xhrInterceptor]))
  ],
  bootstrap: [App]
})
export class AppModule { }
