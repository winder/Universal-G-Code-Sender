import {Component, OnInit} from '@angular/core';
import {MachineService} from '../../services/machine.service'
import {SettingsService} from '../../services/settings.service'
import {Settings} from '../../model/settings'
import {
    faChevronCircleUp,
    faChevronCircleDown,
    faChevronCircleLeft,
    faChevronCircleRight,
    faArrowCircleUp, faArrowCircleDown
} from "@fortawesome/free-solid-svg-icons";

@Component({
    selector: 'app-jog',
    templateUrl: './jog.component.html',
    styleUrls: ['./jog.component.scss']
})
export class JogComponent implements OnInit {
    public faChevronCircleUp = faChevronCircleUp;
    public faChevronCircleDown = faChevronCircleDown;
    public faChevronCircleLeft = faChevronCircleLeft;
    public faChevronCircleRight = faChevronCircleRight;
    public faArrowCircleUp = faArrowCircleUp;
    public faArrowCircleDown = faArrowCircleDown;

    private _settings: Settings;

    constructor(private machineService: MachineService, private settingsService: SettingsService) {
        this._settings = new Settings();
    }

    get settings(): Settings {
        return this._settings;
    }

    ngOnInit() {
        this.settingsService.getSettings()
            .subscribe((settings) => {
                this._settings = settings;
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

    onFeedRateChange(feedRate: string) {
        feedRate = feedRate.replace(',', '.');
        if (!feedRate.endsWith('.') && !feedRate.endsWith('.0') && !feedRate.endsWith('.00') && this._settings.jogFeedRate !== parseFloat(feedRate)) {
            this._settings.jogFeedRate = parseFloat(feedRate);
            this.settingsService.setSettings(this._settings).subscribe();
        }
    }

    onStepSizeXYChange(stepSize: string) {
        stepSize = stepSize.replace(',', '.');
        if (!stepSize.endsWith('.') && !stepSize.endsWith('.0') && !stepSize.endsWith('.00') && this._settings.jogStepSizeXY !== parseFloat(stepSize)) {
            this._settings.jogStepSizeXY = parseFloat(stepSize);
            this.settingsService.setSettings(this._settings).subscribe();
        }
    }

    onStepSizeZChange(stepSize: string) {
        stepSize = stepSize.replace(',', '.');
        if (!stepSize.endsWith('.') && !stepSize.endsWith('.0') && !stepSize.endsWith('.00') && this._settings.jogStepSizeZ !== parseFloat(stepSize)) {
            this._settings.jogStepSizeZ = parseFloat(stepSize);
            this.settingsService.setSettings(this._settings).subscribe();
        }
    }
}
