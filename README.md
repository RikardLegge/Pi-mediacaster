Pi-mediacaster - ALPHA
==============
## Description
This is a small Java project which enables devices to stream media to each other and display the content.(A bit like DLNA, but since i haven't found a DLNA renderer for my raspberry pi which supported hardware accelerated playback, I decided to build my own.) The initial idea was for an Android phone to be able to send media to a Raspberry pi, in the same way as chrome cast/DLNA devices work. NOT WITH A CHROMECAST DEVICE, NOR WITH THE SAME PROTOCOL.

In the current state, the image viewing is available on systems like Linux, OS X and Windows, since it uses Java Swing to render the content. The video on the other hand currently uses "omxplayer" for Raspberry pi/Linux, as well as the "youtube-dl" to display video. The android application is also a work in progress, but has most of the core functionality built into it. 

This is a result of a weekend hack together, with little to no prior knowledge on sockets, streams or the Linux shell.

**Help is deeply appreciated if you find this project interesting!**

## Features
+ Stream media from an Android device to any Raspberry PI or other computer running the Java server.
+ **Beam** images and video from URL links with a single press of a button. (Youtube.com, mm)
+ Remote image manipulation > Move, zoom and rotate from device.
+ Remote video control > Play, pause and seek.
+ Easy to use

## Installation
### Raspberry PI - **Dependencies**
This is the first project I host on Github, so the installation guide might not be complete.

As always

    sudo apt-get update

omxplayer

    sudo apt-get install omxplayer
    
youtube-dl

    sudo apt-get install youtube-dl
For latest version

    sudo youtube-dl -h
    
Java-1.7

    sudo apt-get install oracle-java7-jdk

If sound does not work, try (shell file for this can be found in PI - Media renderer/Resources/sound.sh)

    sudo modprobe snd_bcm2835

Tip 1: To start the server on startx, create a new file called ".xinitrc" in your home folder. Ex. "/home/pi/.xinitrc"
Add the line `java -jar location_of_the_server`, for me it looks like `java -jar /home/pi/Downloads/MedisServer.jar`.

Tip 2: Use [tmux](http://tmux.sourceforge.net/) to be able to stop a ssh session without the x11(startx) session to stop. In other words, make the server run without an active ssh connection.

Download the source to this project / clone the github repository, and do an ordinary build with eclipse. There is alternatively a built version in the package. (WARNING: Might not always be up to date.)

### Android
Make sure to have enabled development mode on the Android device. Alternatively enable "Install from unknown sources."
Download the source to this project / clone the github repository, and do an ordinary build with eclipse, build for Android or "Export android application". There is alternatively a built version in the package. (WARNING: Might not always be up to date.)

## Help
There is currently no help section.
Please send me a message if you have any questions not answered in this readme file.

## Todo
+ **Video** - Improve integration with the native player. 
+ **Socket** - Check socket before sending information, currently hangs for a couple of seconds when no server is available.
+ **Documentation** - Add some more documentation.

## Contributors
Creator: Rikard Legge (Ledge)

## Inspiration
This project was inspired by both the "Google Chrome cast", as well as "PICast", which both seek to turn any ordinary screen, into a smart one, with the ability to easily display content. Since I recently go a Raspberry pi I sought the opportunity to make an application which made it easier to view media on a bigger screen.

## Disclaimer
This project is in no way related or affiliated to either “Google” or the “PICast” project.
