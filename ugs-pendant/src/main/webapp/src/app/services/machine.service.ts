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
    return this.http.get<string[]>('/api/machine/connect');
  }

  disconnect(): Observable<string[]> {
    return this.http.get<string[]>('/api/machine/disconnect');
  }

  getBaudRateList(): Observable<string[]> {
    return this.http.get<string[]>('/api/machine/getBaudRateList');
  }

  getSelectedBaudRate(): Observable<string> {
    return this.http.get<any>('/api/machine/getSelectedBaudRate').map(baudRate => baudRate.selectedBaudRate);
  }

  setSelectedBaudRate(baudRate:string): Observable<any> {
    return this.http.post('/api/machine/setSelectedBaudRate?baudRate=' + baudRate, null);
  }

  getPortList(): Observable<string[]> {
    return this.http.get<string[]>('/api/machine/getPortList');
  }

  getSelectedPort(): Observable<string> {
    return this.http.get<any>('/api/machine/getSelectedPort').map(port => port.selectedPort);
  }

  getFirmwareList(): Observable<string[]> {
    return this.http.get<string[]>('/api/machine/getFirmwareList');
  }

  getSelectedFirmware(): Observable<string> {
    return this.http.get<any>('/api/machine/getSelectedFirmware').map(firmware => firmware.selectedFirmware);
  }

  setSelectedFirmware(firmware:string): Observable<any> {
    return this.http.post('/api/machine/setSelectedFirmware?firmware=' + firmware, null);
  }

  killAlarm(): Observable<any> {
    return this.http.get('/api/machine/killAlarm');
  }

  homeMachine(): Observable<any> {
    return this.http.get('/api/machine/homeMachine');
  }

  softReset(): Observable<any> {
    return this.http.get('/api/machine/softReset');
  }

  returnToZero(): Observable<any> {
    return this.http.get('/api/machine/returnToZero');
  }

  resetToZero(axis:AxisEnum = null): Observable<any> {
    if(axis == null) {
      return this.http.get('/api/machine/resetToZero');
    } else {
      return this.http.get('/api/machine/resetToZero?axis=' + axis);
    }
  }

  jog(x:number, y:number, z:number): Observable<any> {
    return this.http.get('/api/machine/jog?x=' + x + '&y=' + y + '&z=' + z);
  }

  send(): Observable<any> {
    return this.http.get('/api/machine/send');
  }

  pause(): Observable<any> {
    return this.http.get('/api/machine/pause');
  }

  cancel(): Observable<any> {
    return this.http.get('/api/machine/cancel');
  }
}
