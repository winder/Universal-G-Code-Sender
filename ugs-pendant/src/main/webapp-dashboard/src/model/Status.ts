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
  overrides: {
    feed: number;
    rapid: number;
    spindle: number;
  };
  // Only present on a REST fetchStatus() response (sourced from the gcode
  // parser's M7/M8/M9 modal state, refreshed via "$G") - not carried by the
  // WebSocket status push, so statusSlice preserves the last known value
  // across WS updates instead of resetting it.
  floodCoolantOn?: boolean;
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
