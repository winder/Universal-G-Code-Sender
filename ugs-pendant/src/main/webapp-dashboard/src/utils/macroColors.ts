import type { CSSProperties } from "react";

// Curated palette rather than a full color wheel for the preset row - all
// pulled from the same "400" shade family as the dashboard's existing green
// accent (#4ade80 is Tailwind's green-400) so any pick still reads as part
// of one coherent set instead of a clashing grab-bag. A native <input
// type="color"> covers anything outside this set (see MacroColorPicker).
export const MACRO_COLOR_PRESETS = [
  "#4ade80", // green (matches the dashboard's own accent)
  "#60a5fa", // blue
  "#fbbf24", // amber
  "#f87171", // red
  "#c084fc", // purple
  "#f472b6", // pink
  "#22d3ee", // cyan
  "#fb923c", // orange
];

// Soft fill + hard outline, both derived from one hex value so a macro's
// color is a single pick rather than separately choosing an alpha.
export function macroColorStyle(color: string | undefined | null): CSSProperties {
  if (!color) {
    return {};
  }
  return {
    borderColor: color,
    backgroundColor: hexToRgba(color, 0.15),
    color,
  };
}

function hexToRgba(hex: string, alpha: number): string {
  const normalized = hex.replace("#", "");
  const value = normalized.length === 3
    ? normalized.split("").map((c) => c + c).join("")
    : normalized;
  const r = parseInt(value.slice(0, 2), 16);
  const g = parseInt(value.slice(2, 4), 16);
  const b = parseInt(value.slice(4, 6), 16);
  if (Number.isNaN(r) || Number.isNaN(g) || Number.isNaN(b)) {
    return hex;
  }
  return `rgba(${r}, ${g}, ${b}, ${alpha})`;
}
