Universal GcodeSender is a Java based GRBL compatibl cross platform G-Code sender. Use this program to run a GRBL controlled CNC machine.

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
[1.0.5 32-bit](http://bit.ly/Xz4U1R)
<br />
[1.0.5 64-bit](http://bit.ly/SGKfMN)
<br />
[Older releases can be found on the downloads page](https://github.com/winder/builds/tree/master/UniversalGCodeSender)
<br />

![Command table tab during a file send](https://github.com/winder/Universal-G-Code-Sender/raw/master/pictures/sending_file_1.0.5.png "Command table tab during a file send.")
![Finished sending a file](https://github.com/winder/Universal-G-Code-Sender/raw/master/pictures/finished_sending_file_1.0.5.png "Popup after finishing a file send.")

Technical details:
* RXTX for serial communication.
* JogAmp for OpenGL.
* Utilizes One-Jar to bundle all dependencies into a single runnable .jar file.
* Developed with NetBeans 7.2.1 or later.
* For development you will need to install RXTX.
* To build a release open the 'Files' pane and right click build.xml, then 
  select 'Run Target' > 'onejar-build-release'. A new directory named 'release'
  will contain the .zip file.

Changelog
---------
1.0.5 -> 1.0.6
* 3D gcode visualizer!
* - color coded line segments
* - real time tool position
* - real time gcode buffer position
* Complete all-in-one release, 32 and 64 bit linux/windows/mac
* Settings saved between runs (thanks lazyzero)
* Due to popular demand, RaspberryPi support. (not tested)
* Unit tests added, over 1200 lines of unit test code.

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
