# Installation
1. Clone this repository by entering this command into *Bash*:
```bash
git clone git@github.com:david-read-iii/Vine-Soundboard.git
```
2. Then, open the root project directory in *Android Studio*.

# Configuration
## Specify a Google AdMob App ID in local.properties
1. Register for a Google AdMob account from [this site](https://admob.google.com/home/).
2. Add an app to your Google AdMob account.
3. Append the following line into the `local.properties` file in your root project directory. Replace `ca-app-pub-xxxxxxxxxxxxxxxx~yyyyyyyyyy` with the App ID of the app you just added in your Google AdMob account:
```properties
ADMOB_APP_ID=ca-app-pub-xxxxxxxxxxxxxxxx~yyyyyyyyyy
```
## Specify an Ad Unit ID for the ad banner in MainActivity in local.properties
1. Create a new ad unit for the app you just added to your Google AdMob account. Select `Banner` when going through the creation wizard.
2. Append the following line into the `local.properties` file in your root project directory. Replace `ca-app-pub-xxxxxxxxxxxxxxxx/yyyyyyyyyy` with the App Unit ID of the ad unit you just created. Replacing `ca-app-pub-xxxxxxxxxxxxxxxx/yyyyyyyyyy` with `ca-app-pub-3940256099942544/6300978111` should be sufficient for testing purposes:
```properties
MAIN_ACTIVITY_AD_UNIT_ID=ca-app-pub-xxxxxxxxxxxxxxxx/yyyyyyyyyy
```
## Copy your own MP3 files for the soundboard to use
1. Create an `assets` directory in the `/app/src/main` directory.
2. Copy your own MP3 files into the `assets` directory for the soundboard to use. Make sure you specify the *Title* MP3 attribute for each file.

# Build APK or Android App Bundle
1. From *Android Studio*, go to the *Build* menu.
2. Go to the *Build Bundle(s) / APK(s)* menu.
3. Select either *Build APK(s)* or *Build Bundle(s)*.
4. Wait for a notification to pop signifying the operation completion.
5. Select *locate* in the notification to navigate to the file location of the built artifact.
6. Install the artifact on your Android device and try it out.

# Maintainers
This project is maintained by:
* [David Read](http://github.com/david-read-iii)
