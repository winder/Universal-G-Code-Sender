# UGS CLI

This provides a terminal based client for Universal Gcode Sender, making it possible to connect to a controller and send
G-code files without any fancy GUI's. The program uses the same stable and well tested backend as *UGS Classic* and
*UGS Platform*.

## Usage
Run the software with java -jar, then combine with these options:

```
 -b,--baud <baudrate>           Baud rate to connect with.
 -c,--controller <controller>   What type of controller firmware we are
                                connecting to, defaults to "GRBL". These
                                are the available firmwares: [GRBL, TinyG,
                                Testing (Delay), Smoothie Board, Testing]
 -d,--daemon                    Starts in daemon mode providing a web
                                pendant UI
 -dr,--driver <driver>          Sets and saves the connection driver
                                setting. These are the available drivers:
                                [JSERIALCOMM, JSSC, TCP]
 -f,--file <filename>           Opens a file for streaming to controller
                                and will exit upon completion.
 -h,--help                      Prints the help information.
 -ho,--home                     If a homing process should be done before
                                any gcode files are sent to the
                                controller.
 -l,--list                      Lists all available ports.
 -p,--port <port>               Which port for the controller to connect
                                to. I.e /dev/ttyUSB0 (on Unix-like systems
                                or COM4 (on windows).
 -pp,--print-progressbar        Prints the progress of the file stream
 -ps,--print-stream             Prints the streamed lines to console
 -r,--reset-alarm               Resets any alarm
 -v,--version                   Prints the software version.
 -w,--workspace <dir>           Sets and saves the workspace directory
                                setting
 ```
 
 ## Example
 
```
# java -jar ugs-cli-2.0-SNAPSHOT.jar --controller GRBL --port /dev/ttyUSB0 --baud 115200 --print-progressbar --file test.gcode

Connected to "Grbl 0.9z" on  baud 115200
Running file "test.gcode"
test.gcode  52% │██████████████████████▉                    │  55/105 (0:00:06 / 0:00:05) 
```
