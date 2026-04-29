import { Component, inject, OnInit } from '@angular/core';
import { FormControl, Validators, ReactiveFormsModule, FormGroup } from '@angular/forms';
import { AppService } from '../app-service';
import { Router } from '@angular/router';
import * as L from 'leaflet';

const iconRetinaUrl = 'assets/marker-icon-2x.png';
const iconUrl = 'assets/marker-icon.png';
const shadowUrl = 'assets/marker-shadow.png';
const iconDefault = L.icon({
  iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
  iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
  shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  tooltipAnchor: [16, -28],
  shadowSize: [41, 41]
});
L.Marker.prototype.options.icon = iconDefault;

@Component({
  selector: 'app-register',
  standalone: false,
  templateUrl: './register.html',
  styleUrl: './register.css',
})
export class Register implements OnInit {
  private app = inject(AppService);
  private router = inject(Router);
  usrTaken = false;
  passHidden = true;
  passConfHidden = true;
  map: L.Map | null = null;
  marker?: L.Marker;

  usernameRegex = '^[A-Z][a-zA-Z0-9]{4,}$';
  passwordRegex = '^(?=.*[^a-zA-Z0-9])[a-zA-Z0-9\\S]{7,}$';

  usernameControl = new FormControl('', [Validators.required, Validators.pattern(this.usernameRegex)]);
  passwordControl = new FormControl('', [Validators.required, Validators.pattern(this.passwordRegex)]);
  passwordConfirmControl = new FormControl('', [Validators.required, Validators.pattern(this.passwordRegex)]);
  accountTypeControl = new FormControl('User', [Validators.required]);
  locationControl = new FormControl<{ lat: number, lng: number } | null>(null);

  ngOnInit() {
    this.app.usrTaken$.subscribe(taken => {
      this.usrTaken = taken;
    });

    this.accountTypeControl.valueChanges.subscribe(val => {
      if (val === 'Manager') {
        this.locationControl.setValidators([Validators.required]);
        setTimeout(() => this.initMap(), 100);
      } else {
        this.locationControl.clearValidators();
        this.locationControl.setValue(null);
        this.destroyMap();
      }
      this.locationControl.updateValueAndValidity();
    });
  }

  initMap() {
    if (this.map) return;

    const mapElement = document.getElementById('map');
    if (!mapElement) return;

    this.map = L.map('map').setView([0, 0], 2);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap contributors'
    }).addTo(this.map);

    this.map.locate({ setView: true, maxZoom: 16 });

    this.map.on('click', (e: L.LeafletMouseEvent) => {
      const { lat, lng } = e.latlng;
      this.updateMarker(lat, lng);
    });

    this.map.on('locationfound', (e: L.LocationEvent) => {
      this.updateMarker(e.latlng.lat, e.latlng.lng);
    });
  }

  destroyMap() {
    if (this.map) {
      this.map.remove();
      this.map = null;
      this.marker = undefined;
    }
  }

  updateMarker(lat: number, lng: number) {
    if (!this.map) return;

    if (this.marker) {
      this.marker.setLatLng([lat, lng]);
    } else {
      this.marker = L.marker([lat, lng]).addTo(this.map);
    }
    this.locationControl.setValue({ lat, lng });
  }

  register(): boolean {
    if (this.usernameControl.valid &&
      this.passwordControl.valid &&
      (this.passwordConfirmControl.value == this.passwordControl.value) &&
      this.accountTypeControl.valid) {

      const isManager = this.accountTypeControl.value === 'Manager';

      if (isManager && !this.locationControl.valid) {
        this.app.displayAlert("Please select a location on the map.");
        return false;
      }

      const coords = isManager ? this.locationControl.value : { lat: 0, lng: 0 };

      const payload = {
        username: this.usernameControl.value!,
        password: this.passwordControl.value!,
        accountType: this.accountTypeControl.value!,
        location: coords
      };

      this.app.register(payload, () => {
        this.app.logIn({ username: payload.username, password: payload.password });
        this.router.navigateByUrl('/');
      });
      return true;
    }
    return false;
  }

  clearAlert() {
    this.usrTaken = false;
  }

  togglePass() {
    this.passHidden = !this.passHidden;
  }

  togglePassConf() {
    this.passConfHidden = !this.passConfHidden;
  }
}