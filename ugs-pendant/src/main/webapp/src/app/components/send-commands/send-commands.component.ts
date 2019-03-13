import { Component, OnInit } from '@angular/core';
import { StatusService } from '../../services/status.service';
import { MachineService } from '../../services/machine.service'
import { FilesService } from '../../services/files.service'
import { Status } from '../../model/status';
import { StateEnum } from '../../model/state-enum';
import { HttpClient, HttpHeaders } from '@angular/common/http';


@Component({
  selector: 'app-send-commands',
  templateUrl: './send-commands.component.html',
  styleUrls: ['./send-commands.component.scss']
})
export class SendCommandsComponent implements OnInit {

  constructor() { }

  public ngOnInit() {

  }

}
