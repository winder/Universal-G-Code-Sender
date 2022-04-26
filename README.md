![Universal G-Code Sender](https://github.com/winder/Universal-G-Code-Sender/raw/master/pictures/ugs_header.png "UGS Splash Image")


[![Last commit](https://img.shields.io/github/last-commit/winder/Universal-G-Code-Sender.svg?maxAge=1800)](https://github.com/winder/Universal-G-Code-Sender/commits/master)
[![Build Status](https://app.travis-ci.com/winder/Universal-G-Code-Sender.svg?branch=master)](https://app.travis-ci.com/github/winder/Universal-G-Code-Sender)
[![Codebeat badge](https://codebeat.co/badges/48cc1265-2f6b-4163-8a8a-964acc073100)](https://codebeat.co/projects/github-com-winder-universal-g-code-sender-master)
[![Releases](https://img.shields.io/github/v/release/winder/Universal-G-Code-Sender)](https://github.com/winder/Universal-G-Code-Sender/releases)

Universal G-Code Sender is a Java based, cross platform G-Code sender, compatible with [GRBL](https://github.com/gnea/grbl/), [TinyG](https://github.com/synthetos/TinyG), [g2core](https://github.com/synthetos/g2) and [Smoothieware](http://smoothieware.org/).

Online documentation and releases: https://winder.github.io/ugs_website/<br/>
Discussion forum: https://groups.google.com/forum/#!forum/universal-gcode-sender

Technical details:

* [JSSC](https://github.com/scream3r/java-simple-serial-connector) or [JSerialComm](https://github.com/Fazecast/jSerialComm) for serial communication
* [JogAmp](https://jogamp.org/) for OpenGL
* [Netbeans Platform](https://netbeans.org/features/platform/)
* [JTS](https://github.com/locationtech/jts) for geometric transformations
* [Batik](https://xmlgraphics.apache.org/batik/) for reading SVG

## Downloads

🚩 There is currently a problem with the download server. 
Please use the download links under the latest release page [here](https://github.com/winder/Universal-G-Code-Sender/releases/tag/v2.0.11). 

Sorry for the inconvenience.

## Screenshots

### UGS Platform

UGS Platform main window

![UGS Platform](https://github.com/winder/Universal-G-Code-Sender/raw/master/pictures/2.0_platform_ugs_platform.png "UGS Platform")

Customizable panel layout

![Customizable panel layout](https://github.com/winder/Universal-G-Code-Sender/raw/master/pictures/2.0_platform_customizable_panels.png "Customizable panel layout")

Menu actions with customizable keybindings

![Actions](https://github.com/winder/Universal-G-Code-Sender/raw/master/pictures/2.0_platform_actions_menu.png "Actions")

Menu with plugins

![Plugins](https://github.com/winder/Universal-G-Code-Sender/raw/master/pictures/2.0_platform_plugins_menu.png "Plugins")

One of many plugins

![Dowel Maker](https://github.com/winder/Universal-G-Code-Sender/raw/master/pictures/2.0_platform_dowel_maker_plugin.png "Dowel maker plugin")

Basic gcode editor

![Basic gcode editor](https://github.com/winder/Universal-G-Code-Sender/raw/master/pictures/2.0_platform_editor.png "Basic gcode editor")

Vector graphics designer for generating GCode toolpaths

![Designer](https://github.com/winder/Universal-G-Code-Sender/raw/master/pictures/2.0_platform_designer.png "Basic gcode editor")


### UGS Classic

UGS Classic main window

![Classic main window](https://winder.github.io/ugs_website/img/screenshots/finished.png)

UGS Classic with visualizer

![Classic visualizer](https://winder.github.io/ugs_website/img/screenshots/visualizer.png)


## Development
<details><summary>Show details on how to compile the software</summary>
<p>

For development we use [Maven](http://maven.apache.org) and [Java](https://github.com/AdoptOpenJDK/openjdk13-binaries/releases/tag/jdk-13.0.1%2B9) for compiling. We rely on a specific version of Java 13.0.1-9 is needed due to compatibility issues with a library we depend on.

#### Compiling and starting the application

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
mvn package -pl ugs-classic
```


#### Build a UniversalGcodeSender.zip release file

```bash
mvn package assembly:assembly
```

#### Develop via IntelliJ

If you are more used to IntelliJ, you can also build, run and debug it there.

- Run  `mvn nbm:run-platform -pl ugs-platform/application` once via terminal to build everything
- Import the Source, `File` -> `New` -> `Project from existing Sources`
- Setup a new "Run Configuration", `Java Application`, with following settings:
  - Main Class: `org.netbeans.Main`
  - VM Options: `-Dnetbeans.user=$ProjectFileDir$/ugs-platform/application/target/userdir -Dnetbeans.home=$ProjectFileDir$/ugs-platform/application/target/ugsplatform/platform -Dnetbeans.logger.console=true -Dnetbeans.indexing.noFileRefresh=true -Dnetbeans.dirs="$ProjectFileDir$/ugs-platform/application/target/ugsplatform/ugsplatform:$ProjectFileDir$/ugs-platform/application/target/ugsplatform/platform:$ProjectFileDir$/ugs-platform/application/target/ugsplatform/ide:$ProjectFileDir$/ugs-platform/application/target/ugsplatform/extra:$ProjectFileDir$/ugs-platform/application/target/ugsplatform/java"`
  - Program arguments: `--branding ugsplatform`
  - Working dir: `$ProjectFileDir$`
  - Use classpath of module: `ugs-platform-app` 
- There is a [runConfiguration](.idea/runConfigurations/UGS_Platform.xml) in the repository, which should be available after importing the project

</p>
</details>
