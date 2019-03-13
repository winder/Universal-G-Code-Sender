import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';
import { Subject } from 'rxjs/Subject';
import { timer, interval } from 'rxjs';
import { switchMap, tap, retryWhen, delayWhen } from 'rxjs/operators';
import 'rxjs/add/operator/map'

import { Status } from '../model/status';
import { StateEnum } from '../model/state-enum';
import { Position } from '../model/position';


@Injectable({
  providedIn: 'root'
})
export class StatusService {
  private statusSubject:Subject<Status> = new Subject<Status>();

  constructor(private http:HttpClient) { }

  getStatus(): Observable<Status> {
    return this.statusSubject;
  }

  /**
   * Starts a timer and refreshes the status with event intervals
   */
  start() {
    interval(200)
    .pipe(
      switchMap(_ => this.refreshStatus()),
      retryWhen(errors =>
        // Retry on errors
        errors.pipe(
          // On error send an invalid status
          tap(error => {
            let status = new Status();
            status.state = StateEnum.UNAVAILABLE;
            this.statusSubject.next(status);
          }),
          // Restart in 5 seconds
          delayWhen(error => timer(5000))
        )
      )
    )
    .subscribe();
  }

  refreshStatus():Observable<Status> {
    return this.http.get<Status>('/api/v1/status/getStatus')
      .map(response => {
        let status = new Status();
        status.state = response.state;
        status.fileName = response.fileName;
        status.rowCount = response.rowCount;
        status.completedRowCount = response.completedRowCount;
        status.remainingRowCount = response.remainingRowCount;
        status.sendDuration = response.sendDuration;
        status.sendRemainingDuration = response.sendRemainingDuration;

        if (response.workCoord) {
          status.workCoord = new Position(response.workCoord.x, response.workCoord.y, response.workCoord.z, response.workCoord.units);
        }

        if (response.machineCoord) {
          status.machineCoord = new Position(response.machineCoord.x, response.machineCoord.y, response.machineCoord.z, response.machineCoord.units);
        }
        return status;
      })
      .pipe(
        tap(status => this.statusSubject.next(status))
      );
  }
}
