export class Settings {
  public static readonly UNITS_MM:string = "MM";
  public static readonly UNITS_INCH:string = "INCH";

  private _jogFeedRate:number = 0;
  private _jogStepSizeXY:number = 0;
  private _jogStepSizeZ:number = 0;
  private _preferredUnits:string;

  constructor() {
    this.preferredUnits = Settings.UNITS_MM;
  }

  set jogFeedRate(jogFeedRate:number) {
    this._jogFeedRate = jogFeedRate;
  }

  get jogFeedRate():number {
    return this._jogFeedRate;
  }

  set jogStepSizeXY(jogStepSize:number) {
    this._jogStepSizeXY = jogStepSize;
  }

  get jogStepSizeXY():number {
    return this._jogStepSizeXY;
  }

  set jogStepSizeZ(jogStepSize:number) {
    this._jogStepSizeZ = jogStepSize;
  }

  get jogStepSizeZ():number {
    return this._jogStepSizeZ;
  }

  set preferredUnits(preferredUnits:string) {
    this._preferredUnits = preferredUnits;
  }

  get preferredUnits():string {
    return this._preferredUnits;
  }
}
