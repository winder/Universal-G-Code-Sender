![Universal Gcode Sender](https://raw.githubusercontent.com/winder/Universal-G-Code-Sender/master/ugs-platform/branding/src/main/nbm-branding/core/core.jar/org/netbeans/core/startup/splash.gif "UGS Splash Image")

Universal Gcode Sender is a Java based, [GRBL](https://github.com/gnea/grbl/) compatible and cross platform G-Code sender. Use this program to run a GRBL controlled CNC machine. 

Online documentation: http://winder.github.io/ugs_website/
Discussion forum: https://groups.google.com/forum/#!forum/universal-gcode-sender

Technical details:

* [JSSC](https://github.com/scream3r/java-simple-serial-connector) or [JSerialComm](https://github.com/Fazecast/jSerialComm) for serial communication (RXTX was used up through v1.0.8)
* [JogAmp](https://jogamp.org/) for OpenGL
* Built with [Netbeans Platform](https://netbeans.org/features/platform/)
* Developed with NetBeans 8.0.2 or later

## Downloads

Universal Gcode Sender comes in two flavors. **UGS Classic** with a very clean and lightwight user interface and the newer and more feature packed **UGS Platform**.

### Nightly builds
Nightly builds of the most recent code with the latest features and bug fixes. 

[![Build Status](https://travis-ci.org/winder/Universal-G-Code-Sender.svg?branch=master)](https://travis-ci.org/winder/Universal-G-Code-Sender)

| Variant | Java Version Required | Description |
|-|-|-|
| [UGS Classic](http://bit.ly/1hftIhy)  | 8+ | The classic UGS interface. |
| [UGS Platform](http://bit.ly/1DkClRW) | 8+ | The next generation platform-based interface. |

"Somewhat stable" nightly build. In an effort to move towards a proper 2.0.0 release we have these "Somewhat stable" builds, they are similar to a release candidate but less formal.

| Variant | Java Version Required | Description | Release date |
|-|-|-|-|
| [UGS Classic](http://bit.ly/2ssmIqt) | 8+ | Somewhat stable UGS classic interface. | July 28, 2017 |
| [UGS Platform](http://bit.ly/2rk84y2)| 8+ | Somewhat stable platform-based next generation interface. | July 28, 2017 |


### Releases

| Version | Java Version Required | Release date |
| ------- | --------------------- | ------------ |
| [UGS Classic 1.0.9](http://bit.ly/1M6z2ys) | 7+ | 11 Nov, 2015 |
| [UGS Classic 1.0.8](http://bit.ly/1BSKon6) | 7+ | 25 Feb, 2015 |
| [UGS Classic 1.0.7](http://bit.ly/1dNrLAy) | 7+ | 20 Mar, 2014 |
| [UGS Classic 1.0.6](http://bit.ly/16q7obd) | 6+ | 15 Mar, 2013 |

Older releases can be found [here](https://github.com/winder/builds/tree/master/UniversalGCodeSender)

## Running 

Make sure you have [Java](https://java.com/en/download/manual.jsp) installed. 

Download either **UGS Classic** or **UGS Platform** and unzip the .zip file.

* For **UGS Classic** simply double click the jar file. On some platforms you may need to run the included start script.
* For **UGS Platform** run the start script: ```bin/ugsplatform``` 

Note for MacOSX users:
For version 1.0.8 and earlier you may need to create a "/var/lock" directory on OSX to fix a bug in the serial library. To do this open the Terminal application and run the following two commands:

```bash
sudo mkdir /var/lock
sudo chmod 777 /var/lock
```


## Screenshots

### UGS Platform

UGS Platform main window

![UGS Platform](https://github.com/winder/Universal-G-Code-Sender/raw/master/pictures/2.0_platform_ugs_platform.png "UGS Platform")

Customizable panel layout

![Customizable panel layout](https://github.com/winder/Universal-G-Code-Sender/raw/master/pictures/2.0_platform_customizable_panels.png "Customizable panel layout")

Menu actions with customizable keybindings

![Actions](https://github.com/winder/Universal-G-Code-Sender/raw/master/pictures/2.0_platform_actions_menu.png "Actions")
![Plugins](https://github.com/winder/Universal-G-Code-Sender/raw/master/pictures/2.0_platform_plugins_menu.png "Plugins")

One of many plugins

![Dowel Maker](https://github.com/winder/Universal-G-Code-Sender/raw/master/pictures/2.0_platform_dowel_maker_plugin.png "Dowel maker plugin")

## Development

For development the [Maven](http://maven.apache.org) build tool is used.

#### Start the application

UGS Classic: 
```bash
mvn install
mvn exec:java -Dexec.mainClass="com.willwinder.universalgcodesender.MainWindow" -pl ugs-core
```

UGS Platform: 
```bash
mvn install
mvn nbm:run-platform -pl ugs-platform/application
```


#### Execute all tests

```bash
mvn test
```


#### Building the self-executing JAR

```bash
mvn install
mvn package -pl ugs-core
```


#### Build a UniversalGcodeSender.zip release file

```bash
mvn package assembly:assembly
```

## Changelog

1.0.9 -> 2.0
* Constant memory! Optimized every part of UGS to run in a fixed amount of memory, no more slowness because a file is too large.
* 'Cancel' now issue a feed hold and soft reset to stop the machine faster.
* Macro substitutions. Use {machine_x/y/z} {work_x/y/z} to substitute the current machine/work coordinates, {prompt|name} can be used to ask for values in a popup dialog.
* UGS now queries GRBL for its current state and settings in order to configure itself in a more intelligent manner. (Thanks Phil!)
* Improved gcode parser. Heavily refactored and validated with a new suite of unit tests, the gcode parser is now much more powerful than before and can be augmented customizable command processors.
* Controllers can now be added and configured with a JSON file, see resources/firmware_config
* Added restore default buttons on settings pages.
* Plane selection support: G17, G18, G19
* Setup wizard for CNC controllers

1.0.8 -> 1.0.9
* Many performance improvements.
* New serial library: JSSC
* Updated 3D library.
* Maven build script (Thanks nitram509!)
* TinyG support enabled.
* XLCD support.
* Bug fixes: INCH/MM jog units, visualizer fixes (crashes and OSX support), "Null stream" error, localization crashes, 
* Translations: French, Greek, Dutch, Italian

1.0.7 -> 1.0.8
* PendantUI server - connect to UGS with your smartphone or tablet. (jvabob)
* Translations: German, Spanish, Italian, Afrikaans
* Many bugfixes and stability improvements.

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
