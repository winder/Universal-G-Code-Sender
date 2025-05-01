# UGS FX

This is a variant of the application that is intended to supersede the classic edition
and with time, maybe also the platform edition.

The motivation for using JavaFX is that it is getting harder to create flexible and nice looking UI:s 
with Java Swing. There are also a simplistic 3D scene graph and rendering API which is 
needed to create a more interactive visualizer. 

The platform edition utilizes the Netbeans Platform which provides a ton of cool features, but is 
quite heavy to work with. We are also constrained to the NetBeans way of doing things.

## Icons

Whenever I need a new icon I look at the filled Phosphor icons:
https://phosphoricons.com/

Simply add them as SVG:s and use the SvgLoader to take care of the loading. It will also
take care of the color tinting and sizing.