G21 ; millimeters
G90 ; absolute coordinate
G17 ; XY plane
G94 ; units per minute feed rate mode
M3 S1000 ; Turning on spindle

; Go to safety height
G0 Z5

; Go to zero location
G0 X0 Y0
G0 Z0

; Create rectangle
G1 X0 Y0 F1000
G1 Y10
G1 X10
G1 Y0
G1 X0

; Turning off spindle
M5
