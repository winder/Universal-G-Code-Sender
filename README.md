Universal GcodeSender is a Java based GRBL compatibl cross platform G-Code sender. The original implementation is based off of Otto Hermansson GcodeSender GUI.

![Console during a file send](https://github.com/winder/Universal-G-Code-Sender/raw/master/pictures/console_tab.png "Console during a file send.")
![Command table tab during a file send](https://github.com/winder/Universal-G-Code-Sender/raw/master/pictures/command_table_tab.png "Command table tab during a file send.")
![Console during a file send](https://github.com/winder/Universal-G-Code-Sender/raw/master/pictures/manualcontrol.png "Generate jog commands with some buttons.")

The primary purpose is to connect with a GRBL device and stream Gcode commands.

To run simply unzip the .zip file and double click the .jar file.
On some platforms you will need to run 'UniversalGcodeSender.sh' instead.

Note for MAC users:
You may need to create a "/var/lock" directory on OSX to fix a bug in the serial
library. To do this open the Terminal application and run the following two
commands:
<br />
   sudo mkdir /var/lock
<br />
   sudo chmod 777 /var/lock
<br />


Key features:
* Select serial port in GUI.
* Execute commands directly.
* Execute commands from a file.
* Override speed in file (By looking for F commands and calculating % change).

Improvements over the original GcodeSender:
* Cross platform - use the same GUI application on OSX / Windows / Linux.
* Command history - Re-run prior manual commands with up/down arrow.
* Manual jogging, click buttons to move machine.
* Duration timer while sending a file.
* Pause / Resume while sending a file.
* Table view linking the GRBL response to each command as run.
* Faster - tracks GRBL's internal buffer to send multiple commands at a time.
* More features planned.

Technical details:
* RXTX for serial communication.
* Utilizes One-Jar to bundle all dependencies into a single runnable .jar file.
* Developed with NetBeans 7.1.2 or later.
* For development you will need to install RXTX.
* To build a release open the 'Files' pane and right click build.xml, then 
  select 'Run Target' > 'onejar-dist-all-zip'. A new directory called 'release'
  will contain the .zip files.

Downloads
---------
[1.0.4 32-bit](https://github.com/winder/Universal-G-Code-Sender/raw/master/releases/UniversalGcodeSender-v1.0.4-all32.zip)
<br />
[1.0.4 64-bit](https://github.com/winder/Universal-G-Code-Sender/raw/master/releases/UniversalGcodeSender-v1.0.4-all64.zip)
<br />
[Older versions can be found on the downloads page](https://github.com/winder/Universal-G-Code-Sender/tree/master/releases)
<br />

Changelog
---------
1.0.4 -> 1.0.5
* Job duration estimate now displayed when running a file.
* Real-time machine position display for GRBL v8.0c.
* Display for most recent GcodeComment.
* Bug fixes - no more dropped commands!

1.0.3 -> 1.0.4
* The step size spinner now goes to less than 1.
* New buttons on the manual control page for common functions.
* Manual X/Y coordinates can now be modified with arrow keys.
* New start scripts added to release zip files.
* Changed speed override to a percentage rather than absolute.
* Bug fixes.

1.0.2 -> 1.0.3
* Manual jogging mode, control machine with buttons in the UI.
* Automatically skip blank lines and comments when sending a file.
* Bug fixes.

1.0.1 -> 1.0.2
* Grbl version checking.
* Real-time pause/resume commands used for Grbl v0.8.
* Comment filtering - parses out comments before sending commands.
* Bug fixes.

1.0 -> 1.0.1
* New distribution jar - Application no longer requires RXTX to be installed!
* Added table view for file streaming.
* Added Pause/Resume button.
* Usability improvements.
* Many bug fixes.
* Renamed package to com.willwinder.universalgcodesender.
