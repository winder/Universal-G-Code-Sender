import { Position } from './position'
import { StateEnum } from './state-enum'

export class Status {
  private _state: StateEnum = StateEnum.UNAVAILABLE;
  private _machineCoord: Position = new Position();
  private _workCoord: Position = new Position();
  private _fileName: string;
  private _rowCount: number;
  private _completedRowCount: number;
  private _remainingRowCount: number;
  private _sendDuration: number;
  private _sendRemainingDuration: number;

  constructor() {
  }

  set state(state:StateEnum) {
    this._state = state;
  }

  get state():StateEnum {
    return this._state;
  }

  set machineCoord(machineCoord:Position) {
    this._machineCoord = machineCoord;
  }

  get machineCoord():Position {
    return this._machineCoord;
  }

  set workCoord(workCoord:Position) {
    this._workCoord = workCoord;
  }

  get workCoord():Position {
    return this._workCoord;
  }

  set fileName(fileName:string) {
    this._fileName = fileName;
  }

  get fileName():string {
    return this._fileName;
  }

  set rowCount(rowCount:number) {
    this._rowCount = rowCount;
  }

  get rowCount():number {
    return this._rowCount;
  }

  set completedRowCount(completedRowCount:number) {
    this._completedRowCount = completedRowCount;
  }

  get completedRowCount():number {
    return this._completedRowCount;
  }

  set remainingRowCount(remainingRowCount:number) {
    this._remainingRowCount = remainingRowCount;
  }

  get remainingRowCount():number {
    return this._remainingRowCount;
  }

  set sendDuration(sendDuration:number) {
    this._sendDuration = sendDuration;
  }

  get sendDuration():number {
    return this._sendDuration;
  }

  set sendRemainingDuration(sendRemainingDuration:number) {
    this._sendRemainingDuration = sendRemainingDuration;
  }

  get sendRemainingDuration():number {
    return this._sendRemainingDuration;
  }
}
