# Dashboard Changelog

Notable changes to the touchscreen dashboard (`/dashboard`), most recent first. The classic pendant (`/`) is unaffected unless noted.

## 2026-09-11

- Restyled the Toolbox buttons (Home, Unlock, Soft reset, etc.) to fit the dark theme instead of solid white.
- Darkened text inputs across the dashboard to match the dark theme, and gave the macro editor's gcode field the same syntax highlighting as the main gcode editor.
- Fixed the macro editor so Save/Discard stay visible without scrolling, and fixed a long macro name (with no spaces) distorting the width of neighboring run buttons.
- Added a full macro editor: a new **Macros** tab in the center panel to create, edit, delete, and reorder macros, set a color and icon per macro (shown on the run buttons), and export/import the whole macro list as JSON. A confirm dialog guards against losing unsaved edits.
- Matched the verbose console toggle's on-color to the dashboard's green accent.

## 2026-09-10

- Added a toggle-gated verbose console output option (off by default; only sent over the connection when turned on).
- Fixed the connection-health indicator dropping to amber/red while the machine sat idle with a perfectly healthy connection.
- Redesigned the Feed/Rapid/Spindle overrides as a single segmented control with a live progress bar, and switched coolant to a real-time toggle synced from the controller.

## 2026-09-09

- Added live Feed/Rapid/Spindle override controls to the dashboard.
