; Gcode for a circle
G17 G20 G90 G94 G54 M0 M5 M9 ; Setup
G0 Z0.25 F5
X-0.5 Y0.
G1 Z0.
(arcs...)
G2 X0. Y-0.5 I0.5 J0.
G2 X-0.5 Y0. I0. J0.5