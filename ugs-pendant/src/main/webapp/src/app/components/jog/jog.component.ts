import { Component, OnInit } from '@angular/core';
import { MachineService } from '../../services/machine.service'

@Component({
  selector: 'app-jog',
  templateUrl: './jog.component.html',
  styleUrls: ['./jog.component.scss']
})
export class JogComponent implements OnInit {

  private stepSizes:number[] = [0.01, 0.1, 1.0, 10.0, 100];

  constructor(private machineService:MachineService) {
  }

  ngOnInit() {
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
}
