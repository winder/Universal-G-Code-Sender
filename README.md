Universal GcodeSender is a Java based GRBL compatible cross platform G-Code sender. Use this program to run a GRBL controlled CNC machine.

To run simply unzip the .zip file and double click the .jar file.
On some platforms you will need to run an included start script.

Note for MAC users:
You may need to create a "/var/lock" directory on OSX to fix a bug in the serial
library. To do this open the Terminal application and run the following two
commands:
<br />
   sudo mkdir /var/lock
<br />
   sudo chmod 777 /var/lock
<br />

Downloads
---------
[1.0.7](http://bit.ly/1dNrLAy) - Requires Java 7 or higher.
<br />
[1.0.6](http://bit.ly/16q7obd) - Requires Java 6 or higher.
<br />
[Older releases can be found on the downloads page](https://github.com/winder/builds/tree/master/UniversalGCodeSender)
<br />

![Command table tab during a file send](https://github.com/winder/Universal-G-Code-Sender/raw/master/pictures/1.0.6_command_table.png "Command table tab during a file send.")
![3D Visualizer Window](https://github.com/winder/Universal-G-Code-Sender/raw/master/pictures/1.0.6_visualizer.png "Visualizer window during a file send.")
![Finished sending a file](https://github.com/winder/Universal-G-Code-Sender/raw/master/pictures/1.0.6_job_finished.png "Popup after finishing a file send.")
![Finished sending a file](https://github.com/winder/Universal-G-Code-Sender/raw/master/pictures/1.0.6_advanced_machine_control.png "Advanced GRBL control buttons.")

Technical details:
* RXTX for serial communication.
* JogAmp for OpenGL.
* Utilizes One-Jar to bundle all dependencies into a single runnable .jar file.
* Developed with NetBeans 7.2.1 or later.
* For development you will need to install RXTX.
* To build a release using Netbeans open the 'Files' pane and right click build.xml,
  then select 'Run Target' > 'onejar-build-release'. A new directory named 'release'
  will contain the .zip file.

Changelog
---------
1.0.7 -> 1.0.8 (Unreleased)
* (BETA) TinyG support!
* Accurate GRBL job estimates. (michmerr)
* PendantUI server - connect to UGS with your smartphone or tablet. (jvabob)

1.0.6 -> 1.0.7
* Many improvements to code architecture to allow future support for multiple firmwares (TinyG, Marlin, Smoothie, etc).
* New menu bar! Now new features can be added and existing features are more configurable.
* Additional on-the-fly gcode command preprocessing:
* - truncate and round long decimals to configurable length.
* - enforce max command length.
* - remove whitespaces.
* - expand small arcs to G1 commands.
* New option to save preprocessed gcode file to a file.
* Visualizer navigation controls - pan and zoom with the mouse wheel. (michmerr)
* Visualizer Support for radius arcs (phlatboyz sketchup plugin gcode now displays correctly)
* New single-step mode.
* New colored status indicator.
* [Many bugfixes, thanks to UGS contributors](https://github.com/winder/Universal-G-Code-Sender/graphs/contributors)

1.0.5 -> 1.0.6
* 3D gcode visualizer!
* - color coded line segments
* - real time tool position
* - real time gcode buffer position
* Due to popular demand, RaspberryPi support.
* Complete all-in-one release, 32 and 64 bit linux/windows/mac + RaspberryPi
* Settings saved between runs (thanks lazyzero)
* Advanced GRBL control, new buttons for GRBL 0.8c features (gcode check mode, homing, etc)
* Major backend refactoring to make code more stable and testable.
* Unit tests added, over 1300 lines of unit test code.

1.0.4 -> 1.0.5
* Job duration estimate now displayed when running a file.
* Real-time machine position display for GRBL v8.0c.
* Display for most recent GcodeComment.
* Bug fixes - no more dropped commands!
* Overhauled the GUI to display more information.

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
