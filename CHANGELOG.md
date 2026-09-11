# Dashboard Changelog

Notable changes to the touchscreen dashboard (`/dashboard`), most recent first. The classic pendant (`/`) is unaffected unless noted.

## 2026-09-11

- Added Full Screen and Zoom +/- controls to the top bar, for easier one-tap use on a touchscreen (zoom level is remembered between visits).
- Fixed the macro editor list rows changing height when selected (the name wrapping from one line to two once the reorder/delete icons appeared) - every row now always reserves 2 lines' worth of height, so selecting one doesn't shift the others.
- Fixed the Probe panel's Run button floating on top of the settings fields in a narrow split pane - same root cause as the earlier macro Gcode field fix (a flex child was allowed to collapse to 0 height while its content kept rendering at full size).
- Restored the macro list's reorder/delete icons in the compact (split) list - widened the column and gave the name text room to shrink around them on hover/select, instead of removing the icons there entirely.
- Long macro names now wrap instead of truncating with an ellipsis - a second line on the run buttons, up to two lines in the macro editor's list.
- Fixed macro names getting covered by the reorder/delete icons when selected in the compact (split) macro list - there isn't room there for both, so those actions now only appear in the full-width Macros tab.
- Fixed macro names disappearing entirely in the compact (split) macro list - a side effect of the previous fix for the hover-jump issue.
- Fixed the split right-pane header not staying lined up above the right pane, the macro list jumping around as you move the mouse over it (its reorder/delete icons reflowed the row on hover), and widened the macro list column so it's not cramped before selecting anything.
- Split now defaults to a true, responsive 50/50 (was a fixed pixel width, heavily favoring one side on a wide screen), and the left/right pane selectors now sit on one row instead of two.
- Fixed the macro editor's Gcode field overlapping the fields below it in a narrow split pane, added a way to exit Split back to a single panel (tap Split again), and raised the minimum split pane width so there's always enough room for the macro editor's list and form.
- Turned **Split** into an independent two-pane layout: pick any of Visualize/Edit/Macros/Probe for the left and right panes separately (picking one that's already showing on the other side swaps them instead of duplicating), with a draggable divider between them. Split moved to the far right of the tab bar.
- Added a probe/touch-off feature: a new **Probe** tab in the center panel for Z-probe, single-face X/Y touch-off, and X/Y-center and bore/rectangle-center probing, each with its own diagram, a settings form (feed rates, retract, probe diameter, plate thickness, max travel), and a confirm-before-run dialog. Fixed a real bug along the way where a failed probe could be silently treated as a successful one.
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
