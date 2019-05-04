import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';

import { AxisEnum } from '../model/axis-enum';

@Injectable({
  providedIn: 'root'
})
export class MachineService {

  constructor(private http:HttpClient) { }

  connect(): Observable<string[]> {
    return this.http.get<string[]>('/api/v1/machine/connect');
  }

  disconnect(): Observable<string[]> {
    return this.http.get<string[]>('/api/v1/machine/disconnect');
  }

  getBaudRateList(): Observable<string[]> {
    return this.http.get<string[]>('/api/v1/machine/getBaudRateList');
  }

  getSelectedBaudRate(): Observable<string> {
    return this.http.get<any>('/api/v1/machine/getSelectedBaudRate').map(baudRate => baudRate.selectedBaudRate);
  }

  setSelectedBaudRate(baudRate:string): Observable<any> {
    return this.http.post('/api/v1/machine/setSelectedBaudRate?baudRate=' + baudRate, null);
  }

  getPortList(): Observable<string[]> {
    return this.http.get<string[]>('/api/v1/machine/getPortList');
  }

  getSelectedPort(): Observable<string> {
    return this.http.get<any>('/api/v1/machine/getSelectedPort').map(port => port.selectedPort);
  }

  setSelectedPort(port:string): Observable<any> {
    return this.http.post('/api/v1/machine/setSelectedPort?port=' + port, null);
  }

  getFirmwareList(): Observable<string[]> {
    return this.http.get<string[]>('/api/v1/machine/getFirmwareList');
  }

  getSelectedFirmware(): Observable<string> {
    return this.http.get<any>('/api/v1/machine/getSelectedFirmware').map(firmware => firmware.selectedFirmware);
  }

  setSelectedFirmware(firmware:string): Observable<any> {
    return this.http.post('/api/v1/machine/setSelectedFirmware?firmware=' + firmware, null);
  }

  killAlarm(): Observable<any> {
    return this.http.get('/api/v1/machine/killAlarm');
  }

  homeMachine(): Observable<any> {
    return this.http.get('/api/v1/machine/homeMachine');
  }

  softReset(): Observable<any> {
    return this.http.get('/api/v1/machine/softReset');
  }

  returnToZero(): Observable<any> {
    return this.http.get('/api/v1/machine/returnToZero');
  }

  resetToZero(axis:AxisEnum = null): Observable<any> {
    if(axis == null) {
      return this.http.get('/api/v1/machine/resetToZero');
    } else {
      return this.http.get('/api/v1/machine/resetToZero?axis=' + axis);
    }
  }

  jog(x:number, y:number, z:number): Observable<any> {
    return this.http.get('/api/v1/machine/jog?x=' + x + '&y=' + y + '&z=' + z);
  }

  sendCommands(commands:string): Observable<any> {
    var gcodeCommands = {
      commands: commands
    };

    return this.http.post('/api/v1/machine/sendGcode', gcodeCommands);
  }
}
