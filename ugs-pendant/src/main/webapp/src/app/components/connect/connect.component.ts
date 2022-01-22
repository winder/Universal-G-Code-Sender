import {Component, OnInit} from '@angular/core';
import {MachineService} from '../../services/machine.service'

@Component({
    selector: 'app-connect',
    templateUrl: './connect.component.html',
    styleUrls: ['./connect.component.scss']
})
export class ConnectComponent implements OnInit {

    private _firmwareList: string[];
    private _selectedFirmware: string;
    private _portList: string[];
    private _selectedPort: string;
    private _baudRateList: string[];
    private _selectedBaudRate: string;

    constructor(private machineService: MachineService) {
    }

    ngOnInit() {
        this.updateData();
    }

    updateData() {
        this.machineService.getSelectedFirmware().subscribe(selectedFirmware => {
            this._selectedFirmware = selectedFirmware;
        });

        this.machineService.getFirmwareList().subscribe(firmwareList => {
            this._firmwareList = firmwareList;
        });

        this.machineService.getSelectedPort().subscribe(selectedPort => {
            this._selectedPort = selectedPort;
        });

        this.machineService.getPortList().subscribe(portList => {
            this._portList = portList;
        });

        this.machineService.getBaudRateList().subscribe(baudRateList => {
            this._baudRateList = baudRateList;
        });

        this.machineService.getSelectedBaudRate().subscribe(baudRate => {
            console.log(baudRate);
            this._selectedBaudRate = baudRate;
        });
    }

    onSelectFirmware($event) {
        this.machineService
            .setSelectedFirmware(this._selectedFirmware)
            .subscribe();
    }

    onSelectBaudRate($event) {
        this.machineService
            .setSelectedBaudRate(this._selectedBaudRate)
            .subscribe();
    }

    onSelectPort($event) {
        this.machineService
            .setSelectedPort(this._selectedPort)
            .subscribe();
    }

    connect() {
        this.machineService
            .connect()
            .subscribe();
    }

    get selectedFirmware() {
        return this._selectedFirmware;
    }

    set selectedFirmware(selectedFirmware: string) {
        this._selectedFirmware = selectedFirmware;
    }

    get selectedPort(): string {
        return this._selectedPort;
    }

    set selectedPort(selectedPort: string) {
        this._selectedPort = selectedPort;
    }

    get firmwareList() {
        return this._firmwareList;
    }

    get selectedBaudRate(): string {
        return this._selectedBaudRate;
    }

    set selectedBaudRate(selectedBaudRate: string) {
        this._selectedBaudRate = selectedBaudRate;
    }

    get portList(): string[] {
        return this._portList;
    }

    get baudRateList(): string[] {
        return this._baudRateList;
    }
}
