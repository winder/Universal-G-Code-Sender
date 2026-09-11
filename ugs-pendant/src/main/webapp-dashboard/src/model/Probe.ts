import { Coordinate } from "./Coordinate";

export type ProbeOperation = "Z" | "X_NEG" | "X_POS" | "Y_NEG" | "Y_POS" | "X_CENTER" | "Y_CENTER" | "CENTER";

export type ProbeSettings = {
  feedRateFast: number;
  feedRateSlow: number;
  retractDistance: number;
  delayAfterRetract: number;
  probeDiameter: number;
  plateThickness: number;
  maxTravel: number;
  compensateSoftLimits: boolean;
};

export type ProbeResult = {
  success: boolean;
  message?: string;
  probedPosition?: Coordinate;
};
