# Atlanta Street Art Project
An Android application that allows users to view, find, and navigate to street art throughout Atlanta.

Available on [Google Play](https://play.google.com/store/apps/details?id=comquintonj.github.atlantastreetartproject&hl=en)
<p align="center">
  <img src ="http://i.imgur.com/Ibmcycb.png"/>
</p>

## Release Notes
Current Version: 1.0

### New Features
* Ability to flag / report art has been added
* Ability to submit an artist request has been added 

### Bug Fixes
* Fixed bug that doesn’t change the “Add to tour” text when a piece of art is added to the user’s tour
* Fixed bug where the navigation drawer reopens the selected activity even if the user is already on that activity

### Known Bugs
* Once art has been submitted, the Explore screen needs to be refreshed before the user can see it in the list
* There is no limit to the amount of art that is pulled in on startup
* There can be a slight delay when first opening the app to load the art

## Install Guide

### Prerequisites
* [Android Studio](https://developer.android.com/studio/index.html)
* An Android phone or emulator from Android Studio

### Dependent Libraries
N/A

### Download Instructions
* Download the zip from the [repository](https://github.com/quintonj/AtlantaStreetArtProject) 
  * __Note:__ in the event the application is accepted to the Google Play Store, this link will be updated and the application will be able to be installed directly onto an Android phone from the store

### Build / Installation / Run Instructions 
1. Open Android Studio
2. Direct Android Studio to the folder that contains the repository you have downloaded
3. In the top toolbar, edit the Run Configuration and ensure that the module is set to “app” and the launch activity is set to “Default Activity”
4. Run the application by pressing the green arrow and select the emulator or Android phone that you are using

### Troubleshooting
#### I can’t get the app to run
Ensure that the application’s run configuration is set correctly, as stated above in the Build Instructions. The module should be set to "app", the launch activity should be set to "Default Activity".

#### I don’t see any available devices to run the app on when I press the green arrow
Ensure that you have correctly set up an emulator within Android Studio or make sure that the phone you are using is connected properly. If the problem persists, restart Android Studio or your computer.


