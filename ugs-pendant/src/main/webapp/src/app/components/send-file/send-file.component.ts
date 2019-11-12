import { Component, OnInit } from '@angular/core';
import { FaIconComponent } from "@fortawesome/angular-fontawesome";

import { StatusService } from '../../services/status.service';
import { MachineService } from '../../services/machine.service'
import { FilesService } from '../../services/files.service'
import { Status } from '../../model/status';
import { StateEnum } from '../../model/state-enum';

@Component({
  selector: 'app-send-file',
  templateUrl: './send-file.component.html',
  styleUrls: ['./send-file.component.scss']
})
export class SendFileComponent implements OnInit {
  private status:Status;
  private progress:number;
  constructor(private statusService:StatusService, private machineService:MachineService, private filesService:FilesService) { }

  public ngOnInit() {
    this.status = new Status();
    this.progress = 0;
    this.statusService.getStatus()
      .subscribe(data => {
        this.status = data;
        this.progress = Math.round(this.status.completedRowCount/this.status.rowCount * 100);
      });
  }

  public send() {
    this.filesService.send().subscribe(() => {
      console.log("Sending file");
    });
  }

  public pause() {
    this.filesService.pause().subscribe(() => {
      console.log("Pausing file");
    });
  }


  public cancel() {
    this.filesService.cancel().subscribe(() => {
      console.log("Canceling file send");
    });
  }

  public isSendingFile():boolean {
    return (this.status.state == StateEnum.RUN || this.status.state == StateEnum.HOLD) && this.status.fileName != "";
  }

  public isReadyToSend():boolean {
    return this.status.state == StateEnum.IDLE && this.status.fileName != "";
  }

  public isReadyToResume():boolean {
    return this.status.state == StateEnum.HOLD;
  }

  public isReadyToPause():boolean {
    return this.status.state == StateEnum.RUN;
  }

  public isReadyToCancel():boolean {
    return this.status.state == StateEnum.RUN || this.status.state == StateEnum.HOLD;
  }

  public isReadyToOpen():boolean {
    return this.status.state != StateEnum.DISCONNECTED && this.status.state != StateEnum.UNKNOWN;
  }

  public formatTime(time:number):string {
    var date = new Date(null);
    date.setSeconds(time / 1000); // specify value for SECONDS here
    var timeString = date.toISOString().substr(11, 8);
    return timeString
  }

  public open(event) {
    let fileList: FileList = event.target.files;
    if(fileList.length > 0) {
      let file: File = fileList[0];

      this.filesService.uploadAndOpen(file).subscribe(() => {
          console.log("Canceling file send");
        });
    }
  }
}
