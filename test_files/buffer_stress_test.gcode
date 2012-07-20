(Commands are very small, many as short as 7 characters.)
(This causes the buffer to fill very close to capacity.)

(Simple manual G-Code for an XY ruler)
(plate 50x50)
(origin is in left south)

(~~~setup~~~)
G21 (mm)
G90 (absolute coordinates)
G0 Z5

(~~~ x ruler ~~~)

G0 X0. Y0.
G1 Z0.
G1 X0. Y5.
G0 Z5.

G0 X10. Y0.
G1 Z0.
G1 X10. Y5.
G0 Z5.

G0 X20. Y0.
G1 Z0.
G1 X20. Y5.
G0 Z5.

G0 X30. Y0.
G1 Z0.
G1 X30. Y5.
G0 Z5.

G0 X40. Y0.
G1 Z0.
G1 X40. Y5.
G0 Z5.

G0 X50. Y0.
G1 Z0.
G1 X50. Y5.
G0 Z5.

(~~~ y ruler ~~~)

G0 X0. Y0.
G1 Z0.
G1 X5. Y0.
G0 Z5.

G0 X0. Y10.
G1 Z0.
G1 X5. Y10.
G0 Z5.

G0 X0. Y20.
G1 Z0.
G1 X5. Y20.
G0 Z5.

G0 X0. Y30.
G1 Z0.
G1 X5. Y30.
G0 Z5.

G0 X0. Y40.
G1 Z0.
G1 X5. Y40.
G0 Z5.

G0 X0. Y50.
G1 Z0.
G1 X5. Y50.
G0 Z5.

(~~~finish~~~)
G0 X0. Y0.
