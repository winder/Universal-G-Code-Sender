export class Position {
  private _units: string = "MM";
  private _x: number = 0;
  private _y: number = 0;
  private _z: number = 0;

  constructor(x:number = 0, y:number = 0, z:number = 0, units:string = "MM") {
    this._x = x;
    this._y = y;
    this._z = z;
    this._units = units;
  }

  get x(): number {
    return (Math.round(this._x * 1000) / 1000);
  }

  set x(x:number) {
    this._x = x;
  }

  get y(): number {
    return (Math.round(this._y * 1000) / 1000);
  }

  set y(y:number) {
    this._y = y;
  }

  get z(): number {
    return (Math.round(this._z * 1000) / 1000);
  }

  set z(z:number) {
    this._z = z;
  }

  get units(): string {
    return this._units;
  }

  set units(units:string) {
    this._units = units;
  }
}
