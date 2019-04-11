import { Component, OnInit } from '@angular/core';
import { MachineService } from '../../services/machine.service'

@Component({
  selector: 'app-machine-control',
  templateUrl: './machine-control.component.html',
  styleUrls: ['./machine-control.component.scss']
})
export class MachineControlComponent implements OnInit {

  constructor(private machineService:MachineService) { }

  ngOnInit() {
  }

  killAlarm() {
    this.machineService.killAlarm()
      .subscribe(() => {
        console.log("Killed alarm!");
      },
      error => {
        // TODO handle this error
        console.log("Got error", error);
      })
  }

  homeMachine() {
    this.machineService.homeMachine()
      .subscribe(() => {
        console.log("Home machine");
      },
      error => {
        // TODO handle this error
        console.log("Got error", error);
      })
  }

  returnToZero() {
    this.machineService.returnToZero()
      .subscribe(() => {
        console.log("Return to zero");
      },
      error => {
        // TODO handle this error
        console.log("Got error", error);
      })
  }

  softReset() {
    this.machineService.softReset()
      .subscribe(() => {
        console.log("Soft reset");
      },
      error => {
        // TODO handle this error
        console.log("Got error", error);
      })
  }

  resetToZero() {
    this.machineService.resetToZero()
      .subscribe(() => {
        console.log("Reset zero");
      },
      error => {
        // TODO handle this error
        console.log("Got error", error);
      });
  }

  disconnect() {
    this.machineService.disconnect()
      .subscribe(() => {
        console.log("Disconnect");
      },
      error => {
        // TODO handle this error
        console.log("Got error", error);
      });
  }
}
