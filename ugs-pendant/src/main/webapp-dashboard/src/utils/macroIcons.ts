import {
  faHouse,
  faCrosshairs,
  faPlay,
  faPause,
  faStop,
  faWrench,
  faFan,
  faBolt,
  faRuler,
  faGear,
  faStar,
  faThumbtack,
  faFire,
  faDroplet,
  IconDefinition,
} from "@fortawesome/free-solid-svg-icons";

// Preset icon keys stored on Macro.icon (see Macro.java) - a short stable
// string rather than the FontAwesome import name itself, so this lookup
// table is free to change/extend later without touching stored data.
export const MACRO_ICONS: Record<string, IconDefinition> = {
  home: faHouse,
  crosshairs: faCrosshairs,
  play: faPlay,
  pause: faPause,
  stop: faStop,
  wrench: faWrench,
  fan: faFan,
  bolt: faBolt,
  ruler: faRuler,
  gear: faGear,
  star: faStar,
  pin: faThumbtack,
  fire: faFire,
  droplet: faDroplet,
};

export const MACRO_ICON_KEYS = Object.keys(MACRO_ICONS);
