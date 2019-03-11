import { Component, OnInit } from '@angular/core';
import { MachineService } from '../../services/machine.service'

@Component({
  selector: 'app-jog',
  templateUrl: './jog.component.html',
  styleUrls: ['./jog.component.scss']
})
export class JogComponent implements OnInit {

  private feedRate:number = 0;

  constructor(private machineService:MachineService) {
  }

  ngOnInit() {
    this.machineService.getJogFeedRate()
      .subscribe((feedRate) => {
        this.feedRate = feedRate;
      });
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
