import { Component, OnInit } from '@angular/core';
import { MachineService } from '../../services/machine.service'
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-connect',
  templateUrl: './connect.component.html',
  styleUrls: ['./connect.component.scss']
})
export class ConnectComponent implements OnInit {

  private firmwareList:string[];
  private selectedFirmware:string;

  private portList:string[];
  private selectedPort:string;

  private baudRateList:string[];
  private selectedBaudRate:string;

  constructor(private machineService:MachineService) { }

  ngOnInit() {
    this.updateData();
  }

  updateData() {
    this.machineService.getSelectedFirmware().subscribe(selectedFirmware => {
      this.selectedFirmware = selectedFirmware;
    });

    this.machineService.getFirmwareList().subscribe(firmwareList => {
      this.firmwareList = firmwareList;
    });

    this.machineService.getSelectedPort().subscribe(selectedPort => {
      this.selectedPort = selectedPort;
    });

    this.machineService.getPortList().subscribe(portList => {
      this.portList = portList;
    });

    this.machineService.getBaudRateList().subscribe(baudRateList => {
      this.baudRateList = baudRateList;
    });

    this.machineService.getSelectedBaudRate().subscribe(baudRate => {
      console.log(baudRate);
      this.selectedBaudRate = baudRate;
    });
  }

  onSelectFirmware($event) {
    this.machineService
      .setSelectedFirmware(this.selectedFirmware)
      .subscribe();
  }

  onSelectBaudRate($event) {
    this.machineService
      .setSelectedBaudRate(this.selectedBaudRate)
      .subscribe();
  }

  onSelectPort($event) {
    this.machineService
      .setSelectedPort(this.selectedPort)
      .subscribe();
  }

  connect() {
    this.machineService
      .connect()
      .subscribe();
  }
}
