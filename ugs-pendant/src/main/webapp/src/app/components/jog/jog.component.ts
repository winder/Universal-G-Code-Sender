import { Component, OnInit } from '@angular/core';
import { MachineService } from '../../services/machine.service'
import { SettingsService } from '../../services/settings.service'
import { Settings } from '../../model/settings'

@Component({
  selector: 'app-jog',
  templateUrl: './jog.component.html',
  styleUrls: ['./jog.component.scss']
})
export class JogComponent implements OnInit {

  private settings:Settings;

  constructor(private machineService:MachineService, private settingsService:SettingsService) {
    this.settings = new Settings();
  }

  ngOnInit() {
    this.settingsService.getSettings()
      .subscribe((settings) => {
        this.settings = settings;
      })
  }

  jog(x:number, y:number, z:number) {
    this.machineService.jog(x, y, z)
      .subscribe(() => {
        console.log("Jogging " + x + ", " + y + ", " + z);
      },
      error => {
        // TODO handle this error
        console.log("Got error", error);
      })
  }

  onFeedRateChange(feedRate:number) {
    this.settings.jogFeedRate = feedRate;
    this.settingsService.setSettings(this.settings).subscribe();
  }

  onStepSizeXYChange(stepSize:number) {
    this.settings.jogStepSizeXY = stepSize;
    this.settingsService.setSettings(this.settings).subscribe();
  }

  onStepSizeZChange(stepSize:number) {
    this.settings.jogStepSizeZ = stepSize;
    this.settingsService.setSettings(this.settings).subscribe();
  }
}
