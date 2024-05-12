export type Settings = {
  jogFeedRate: number;
  jogStepSizeXY: number;
  preferredUnits: "MM" | "INCH";
  jogStepSizeZ: number;
  port: string;
  portRate: string;
  firmwareVersion: string;
  useZStepSize: boolean;
};
