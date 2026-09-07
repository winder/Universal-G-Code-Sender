import { Coordinate } from "./Coordinate";

export type Status = {
  machineCoord: Coordinate;
  workCoord: Coordinate;
  feedSpeed: number;
  spindleSpeed: number;
  accessoryStates: {
    spindleCW: boolean;
    flood: boolean;
    mist: boolean;
  };
  state: string;
  pins: {
    x: boolean;
    y: boolean;
    z: boolean;
    a: boolean;
    b: boolean;
    c: boolean;
    probe: boolean;
    door: boolean;
    hold: boolean;
    softReset: boolean;
    cycleStart: boolean;
  }
};
