import { Component, OnInit } from '@angular/core';
import { MachineService } from '../../services/machine.service'
import { StatusService } from '../../services/status.service';
import { Status } from '../../model/status';
import { StateEnum } from '../../model/state-enum';
import { HttpClient, HttpHeaders } from '@angular/common/http';


@Component({
  selector: 'app-send-commands',
  templateUrl: './send-commands.component.html',
  styleUrls: ['./send-commands.component.scss']
})
export class SendCommandsComponent implements OnInit {
  private status:Status;
  private gcodeCommands:string = "";
  constructor(private machineService:MachineService, private statusService:StatusService) { }

  public ngOnInit() {
  this.status = new Status();
      this.statusService.getStatus()
        .subscribe(data => {
          this.status = data;
        });
  }

  public sendCommands() {
    this.machineService.sendCommands(this.gcodeCommands).subscribe();
  }

  public canSend() {
    return this.status.state == StateEnum.IDLE || this.status.state == StateEnum.CHECK;
  }
}
