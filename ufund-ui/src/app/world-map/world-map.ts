import { Component, inject, OnInit, AfterViewInit } from '@angular/core';
import { AppService } from '../app-service';
import { Router } from '@angular/router';
import * as L from 'leaflet';

@Component({
  selector: 'app-world-map',
  standalone: false,
  templateUrl: './world-map.html',
  styleUrl: './world-map.css'
})
export class WorldMap implements AfterViewInit {
  private app = inject(AppService);
  private router = inject(Router);
  
  map!: L.Map;

  ngAfterViewInit() {
    this.initMap();
    this.loadUserMarkers();
  }

  private initMap() {
    this.map = L.map('map').setView([20, 0], 2);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap contributors'
    }).addTo(this.map);
  }

  private loadUserMarkers() {
  this.app.getWorldMap().subscribe({
    next: (users) => {
      users.forEach(user => {
        const marker = L.marker([user.latitude, user.longitude]).addTo(this.map);
        
        marker.bindTooltip(user.username, {
          permanent: true,
          direction: 'top',
          className: 'user-label'
        });

        marker.on('click', () => {
          this.router.navigate(['/home'], { queryParams: { creatorName: user.username } });
        });
      });
    },
    error: (err) => console.error("Could not load map data", err)
  });
}
}