import {Component, OnInit} from '@angular/core';

import {StatusService} from '../../services/status.service';
import {MachineService} from '../../services/machine.service';
import {SettingsService} from '../../services/settings.service';
import {Status} from '../../model/status';
import {Settings} from '../../model/settings';
import {StateEnum} from '../../model/state-enum';
import {AxisEnum} from '../../model/axis-enum';
import {faBackspace} from "@fortawesome/free-solid-svg-icons";

@Component({
    selector: 'app-dro',
    templateUrl: './dro.component.html',
    styleUrls: ['./dro.component.scss']
})
export class DroComponent implements OnInit {
    public AxisEnum = AxisEnum;
    public faBackspace = faBackspace;
    private _status: Status;
    private _settings: Settings;
    private _stateClass: string;
    private _preferredUnits: string = Settings.UNITS_MM;

    constructor(private statusService: StatusService, private machineService: MachineService, private settingsService: SettingsService) {
    }

    ngOnInit() {
        this._stateClass = 'alert-dark';
        this._status = new Status();
        this.statusService.getStatus()
            .subscribe(data => {
                this._status = data;
                switch (this._status.state) {
                    case 'IDLE':
                    case 'SLEEP':
                        this._stateClass = 'alert-info';
                        break;
                    case 'RUN':
                    case 'HOME':
                    case 'JOG':
                    case 'CHECK':
                        this._stateClass = 'alert alert-success';
                        break;
                    case 'HOLD':
                        this._stateClass = 'alert alert-warning';
                        break;
                    case 'ALARM':
                        this._stateClass = 'alert alert-danger';
                        break;
                    default:
                        this._stateClass = 'alert-dark';
                }
            });

        this.settingsService.getSettings()
            .subscribe(data => {
                this._settings = data;
                this._preferredUnits = data.preferredUnits;
            });
        this.settingsService.refreshSettings().subscribe();
    }

    killAlarm() {
        this.machineService.killAlarm().subscribe();
    }

    isStatusAlarm() {
        return this._status.state == StateEnum.ALARM;
    }

    resetToZero(axis: AxisEnum) {
        this.machineService.resetToZero(axis).subscribe();
    }

    toggleUnits() {
        if (this._settings.preferredUnits == Settings.UNITS_MM) {
            this._settings.preferredUnits = Settings.UNITS_INCH;
        } else {
            this._settings.preferredUnits = Settings.UNITS_MM;
        }
        this.settingsService.setSettings(this._settings).subscribe();
    }

    get status(): Status {
        return this._status;
    }

    get stateClass(): string {
        return this._stateClass;
    }

    get preferredUnits(): string {
        return this._preferredUnits;
    }
}
