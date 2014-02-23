Pi-mediacaster INITIAL ALPHA
==============
## Description
This is a small Java project which enables devices to stream media to eachother and display the content. The initial idea was for a Android phone to be able to send information to a Raspberry pi, in the same way as chromecast devices work.
In the current state, the image viewing is available on systems like Linux, OS X and Windows, since it uses Java Swing to render the content. 
The video on the other hand currently uses "omxPlayer" for Raspberry pi, as well as the "youtube-dl" to display video. These functions are currently quite unstable, which means that they sometimes work, and sometimes don't. 

The android application is also a work in progress, since it currently only contains the core functionality for sending content to the server. 

This is a result of a weekend hack together, with little to no prior knowlage on sockets, streams or the linux shell.

## Instalation
### Raspberry PI
This is the first project i host on Github, so the installation might not be complete.

As always: sudo apt-get update

omxplayer: sudo apt-get install omxplayer
    
youtube-dl: sudo apt-get install youtube-dl
For latest version: sudo youtube-dl -h
    
Java-1.7: sudo apt-get install oracle-java7-jdk

If sound does not work: sudo modprobe snd_bcm2835

Tip: To start the server on startx, create a new file called ".xinitrc" in your home folder. Ex. /home/pi/.xinitrc
Add the line "java -jar location_of_the_server", for me it looks like "java -jar /home/pi/Downloads/renderer.jar".

Tip 2: Use tmux(http://tmux.sourceforge.net/) to be able to stop a ssh session without the x11(startx) session to stop.

Download the source to this project / clone the github repo., and do a ordinary build with eclipse. There is alternativey a built version in the backage. 
WARNING: Might not always be up to date.

### Android
Make sure to have enabled development mode on the Android device. Alternatively enable "Install from unknown sources."
Download the source to this project / clone the github repo., and do a ordinary build with eclipse, build for Android. There is alternativey a built version in the backage. 
WARNING: Might not always be up to date. 

## Help
There is currently no help section.

## Todo
+ Clean up the codebase.
+ Make videoviewing stable. 
+ Enable videcontroll (ex. play/pause, forward, backward)
+ Enable Imaage manipulation from client to server. (ex. resize on demand)

## Contributors
Creator: Rikard Legge (Ledge)

## Inspiration
This project was inspired by both the "Google Chromecast", aswell as "PICast", which both seek to be able to turn any ordinary screen, into a smart one, with the ability to easily display content. Since I recently go a Raspberry pi i saught the opurtunity to make an application which made it easier to view media on a bigger screen. 
