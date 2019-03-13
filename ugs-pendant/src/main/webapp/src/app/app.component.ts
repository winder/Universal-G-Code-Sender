import { Component, OnInit } from '@angular/core';
import { StatusService } from './services/status.service';
import { Status } from './model/status';
import { StateEnum } from './model/state-enum';
import { SettingsService } from './services/settings.service';
import { Settings } from './model/settings';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
  private status: Status = new Status();
  constructor(private statusService:StatusService, private settingsService:SettingsService) { }

  ngOnInit() {
    this.statusService.start();
    this.statusService.getStatus().subscribe(status => this.status = status);

    this.settingsService.start();
  }

  isConnectedToPendantAPI():boolean {
    return this.status.state != StateEnum.UNAVAILABLE;
  }

  isConnectedToController():boolean {
    return this.status.state != StateEnum.UNAVAILABLE && this.status.state != StateEnum.DISCONNECTED;
  }
}
