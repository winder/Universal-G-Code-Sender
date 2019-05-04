export class Macro {
  private _name: string;
  private _description: string;
  private _gcode: string;

  constructor(name:string = '', description:string = '', gcode:string = '') {
    this._name = name;
    this._description = description;
    this._gcode = gcode;
  }

  get name(): string {
    return this._name;
  }

  set name(name:string) {
    this._name = name;
  }

  get description():string {
    return this._description;
  }

  set description(description:string) {
    this._description = description;
  }

  get gcode(): string {
    return this._gcode;
  }

  set gcode(gcode:string) {
    this._gcode = gcode;
  }
}
