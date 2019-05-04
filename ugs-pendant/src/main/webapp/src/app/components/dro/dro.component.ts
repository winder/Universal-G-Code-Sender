import { Component, OnInit } from '@angular/core';
import { Observable} from 'rxjs/Rx';
import { throttle, map, distinctUntilChanged} from 'rxjs/operators';

import { StatusService } from '../../services/status.service';
import { MachineService } from '../../services/machine.service';
import { SettingsService } from '../../services/settings.service';
import { Status } from '../../model/status';
import { Settings } from '../../model/settings';
import { StateEnum } from '../../model/state-enum';
import { AxisEnum } from '../../model/axis-enum';

@Component({
  selector: 'app-dro',
  templateUrl: './dro.component.html',
  styleUrls: ['./dro.component.scss']
})
export class DroComponent implements OnInit {

  private status:Status;
  private settings:Settings;
  private stateClass:string;
  private preferredUnits:string = Settings.UNITS_MM;

  constructor(private statusService:StatusService, private machineService:MachineService, private settingsService:SettingsService) { }

  ngOnInit() {
    this.stateClass = 'alert-dark';
    this.status = new Status();
    this.statusService.getStatus()
      .subscribe(data => {
        this.status = data;
        switch(this.status.state) {
          case 'IDLE':
          case 'SLEEP':
            this.stateClass = 'alert-info';
            break;
          case 'RUN':
          case 'HOME':
          case 'JOG':
          case 'CHECK':
            this.stateClass = 'alert alert-success';
            break;
          case 'HOLD':
            this.stateClass = 'alert alert-warning';
            break;
          case 'ALARM':
            this.stateClass = 'alert alert-danger';
            break;
          default:
            this.stateClass = 'alert-dark';
        }
      });

    this.settingsService.getSettings()
      .subscribe(data => {
        this.settings = data;
        this.preferredUnits = data.preferredUnits;
      });
    this.settingsService.refreshSettings().subscribe();
  }

  killAlarm() {
    this.machineService.killAlarm().subscribe();
  }

  isStatusAlarm() {
    return this.status.state == StateEnum.ALARM;
  }

  resetToZero(axis:AxisEnum) {
    this.machineService.resetToZero(axis).subscribe();
  }

  toggleUnits() {
    if(this.settings.preferredUnits == Settings.UNITS_MM) {
      this.settings.preferredUnits = Settings.UNITS_INCH;
    } else {
      this.settings.preferredUnits = Settings.UNITS_MM;
    }
    this.settingsService.setSettings(this.settings).subscribe();
  }
}
