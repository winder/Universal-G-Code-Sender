export type Macro = {
  uuid: string;
  gcode: string;
  description: string | undefined;
  name: string;
  // Dashboard-only styling - undefined on any macro that predates this or
  // was created/edited from the native UGS desktop app. See Macro.java.
  color?: string;
  icon?: string;
};
