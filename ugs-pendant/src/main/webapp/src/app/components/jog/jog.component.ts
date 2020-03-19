import {Component, OnInit} from '@angular/core';
import {MachineService} from '../../services/machine.service'
import {SettingsService} from '../../services/settings.service'
import {Settings} from '../../model/settings'

@Component({
    selector: 'app-jog',
    templateUrl: './jog.component.html',
    styleUrls: ['./jog.component.scss']
})
export class JogComponent implements OnInit {

    private settings: Settings;

    constructor(private machineService: MachineService, private settingsService: SettingsService) {
        this.settings = new Settings();
    }

    ngOnInit() {
        this.settingsService.getSettings()
            .subscribe((settings) => {
                this.settings = settings;
            })
    }

    jog(x: number, y: number, z: number) {
        this.machineService.jog(x, y, z)
            .subscribe(() => {
                    console.log("Jogging " + x + ", " + y + ", " + z);
                },
                error => {
                    // TODO handle this error
                    console.log("Got error", error);
                })
    }

    onFeedRateChange(feedRate: number) {
        this.settings.jogFeedRate = feedRate;
        this.settingsService.setSettings(this.settings).subscribe();
    }

    onStepSizeXYChange(stepSize: number) {
        this.settings.jogStepSizeXY = stepSize;
        this.settingsService.setSettings(this.settings).subscribe();
    }

    onStepSizeZChange(stepSize: number) {
        this.settings.jogStepSizeZ = stepSize;
        this.settingsService.setSettings(this.settings).subscribe();
    }

    getStepSizeXY(): number {
        let currentStepSize = this.settings.jogStepSizeXY;
        return JogComponent.getStepSize(currentStepSize);
    }

    getStepSizeZ(): number {
        let currentStepSize = this.settings.jogStepSizeZ;
        return JogComponent.getStepSize(currentStepSize);
    }

    getStepSizeFeed(): number {
        let currentStepSize = this.settings.jogFeedRate;
        return JogComponent.getStepSize(currentStepSize);
    }

    private static getStepSize(currentStepSize: number) {
        if (currentStepSize < 0.01) {
            return 0.001;
        } else if (currentStepSize < 0.1) {
            return 0.01;
        } else if (currentStepSize < 1) {
            return 0.1;
        } else if (currentStepSize < 10) {
            return 1;
        } else if (currentStepSize < 100) {
            return 10;
        } else if (currentStepSize < 1000) {
            return 100;
        }

        return 1000;
    }
}
