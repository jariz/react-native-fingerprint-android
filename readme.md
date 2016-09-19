#react-native-fingerprint-android

Full fingerprint authentication for react native (android only).

<img src="https://i.imgur.com/maUaQGV.gif" width="250">  

_Pictured: the example project, located at `/example`_


##Example  
This is a simplified version. There are a few more concerns you should be aware of. [see 'Watch out!'](#watch-out)  
For the full version, see the `example` directory.

```js
import Fingerprint from 'react-native-fingerprint-android';
import { ToastAndroid as Toast } from 'react-native';

(async() => {
    const hardware = await Fingerprint.isHardwareDetected();
    const permission = await Fingerprint.hasPermission();
    const enrolled = await Fingerprint.hasEnrolledFingerprints();

    if (!hardware | !permission | !enrolled) {
        let message = !enrolled ? 'No fingerprints registered.' : !hardware ? 'This device doesn\'t support fingerprint scanning.' : 'App has no permission.'
        Toast.show(message, Toast.SHORT);
        return;
    }

    try {
        await Fingerprint.authenticate(warning => {
            Toast.show(`Try again: ${warning.message}`, Toast.SHORT);
        });
    } catch(error) {
        Toast.show(`Try again: ${error.message}`, Toast.SHORT);
    }

    Toast.show("Auth successful!", Toast.SHORT);
})();
```


##API

All functions & constants are static.

####`.authenticate(warningCallback:?(response:FingerprintError) => {}):Promise<null>`  
Starts authentication flow, with a optional callback for warning messages, instructing your user why authentication failed.  
Returns a Promise.
######Resolving
Authentication was successful if this promise gets resolved.  
There are no parameters.
  
######Rejection
Authentication has failed if the promise gets rejected.  
Callback will receive a single parameter with the following structure: (example)  

```json
{
    "code": 1,
    "message": "The hardware is unavailable. Try again later."
}
```

This code will be match one of the following constants in the FingerprintAndroid module:

| Constant                                               | Description                                                                                                                                                                                                                                                                                     |
|--------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| FingerprintAndroid.FINGERPRINT_ERROR_CANCELED          | The operation was canceled because the fingerprint sensor is unavailable.                                                                                                                                                                                                                       |
| FingerprintAndroid.FINGERPRINT_ERROR_HW_UNAVAILABLE    | The hardware is unavailable.                                                                                                                                                                                                                                                                    |
| FingerprintAndroid.FINGERPRINT_ERROR_LOCKOUT           | The operation was canceled because the API is locked out due to too many attempts.                                                                                                                                                                                                              |
| FingerprintAndroid.FINGERPRINT_ERROR_NO_SPACE          | Error state returned for operations like enrollment; the operation cannot be completed because there's not enough storage remaining to complete the operation.                                                                                                                                  |
| FingerprintAndroid.FINGERPRINT_ERROR_TIMEOUT           | Error state returned when the current request has been running too long.                                                                                                                                                                                                                        |
| FingerprintAndroid.FINGERPRINT_ERROR_UNABLE_TO_PROCESS | Error state returned when the sensor was unable to process the current image.                                                                                                                                                                                                                   |

_For more info on the constants, [see Android FingerprintManager docs](https://developer.android.com/reference/android/hardware/fingerprint/FingerprintManager.html)_

######warningCallback
warningCallback is the only and optional parameter to `.authenticate()`.  
If present, warningCallback gets called with a single parameter, a object with the following structure:  
```json
{
    "code": 1,
    "message": "Only acquired a partial fingerprint. Try again."
}
```

This code will be match one of the following constants in FingerprintAndroid:  

| Constant                                             | Description                                                                                                                                                                                                                                                                                     |
|------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| FingerprintAndroid.FINGERPRINT_ACQUIRED_IMAGER_DIRTY | The fingerprint image was too noisy due to suspected or detected dirt on the sensor.                                                                                                                                                                                                            |
| FingerprintAndroid.FINGERPRINT_ACQUIRED_INSUFFICIENT | The fingerprint image was too noisy to process due to a detected condition                                                                                                                                                                                                                      |
| FingerprintAndroid.FINGERPRINT_ACQUIRED_PARTIAL      | Only a partial fingerprint image was detected.                                                                                                                                                                                                                                                  |
| FingerprintAndroid.FINGERPRINT_ACQUIRED_TOO_FAST     | The fingerprint image was incomplete due to quick motion.                                                                                                                                                                                                                                       |
| FingerprintAndroid.FINGERPRINT_ACQUIRED_TOO_SLOW     | The fingerprint image was unreadable due to lack of motion.                                                                                                                                                                                                                                     |
| FingerprintAndroid.FINGERPRINT_ACQUIRED_AUTH_FAILED  | Custom constant added by react-native-fingerprint-android, to simplify API. This code is used when a [fingerprint was recognized but not valid.](https://developer.android.com/reference/android/hardware/fingerprint/FingerprintManager.AuthenticationCallback.html#onAuthenticationFailed()) |

_For more info on the constants, [see Android FingerprintManager docs](https://developer.android.com/reference/android/hardware/fingerprint/FingerprintManager.html)_

####`.isAuthenticationCanceled(): Promise<boolean>`
Tells you whether or not authentication is running or not.

####`.hasPermission(): Promise<boolean>`
Will check if `android.permission.USE_FINGERPRINT` is granted to this app. (should always return true if you add the permission to your AndroidManifest...)

####`hasEnrolledFingerprints(): Promise<boolean>`
Determine if there is at least one fingerprint enrolled.  

####`isHardwareDetected(): Promise<boolean>`
Determine if fingerprint hardware is present and functional.

##Watch out!
React Native Fingerprint Android is mostly just a set of bindings to Android FingerprintManager.  
Alas, _it's very low level_. You are still responsible for:

- Making sure the device has fingerprints enrolled by calling `FingerprintAndroid.hasEnrolledFingerprints()` (if you don't check this before starting authentication, **any valid fingerprint will be accepted**)  
- Making sure your app has proper permissions setup (see installation guide below)
- Making sure device has supported hardware by calling `FingerprintAndroid.isHardwareDetected()` 
- [Making sure you display the correct icon, as defined by the design guidelines.](https://material.google.com/patterns/fingerprint.html)
- Restarting authentication if screen turns off. (see example project for on an example on how to do that)

If you don't do any of the checks before calling `FingerprintAndroid.authenticate`, it will either **directly fail, or security problems.**

##Installation
`npm i react-native-fingerprint-android --save`
  
Whether you're using the automatic installation method or not, don't forget to add the permission to your manifest:


`android/app/src/main/AndroidManifest.xml` 
 ```diff
 <manifest xmlns:android="http://schemas.android.com/apk/res/android" package="com.example" android:versionCode="1" android:versionName="1.0">
     <uses-permission android:name="android.permission.INTERNET" />
     <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW"/>
+    <uses-permission android:name="android.permission.USE_FINGERPRINT" />
 ```

###Automatic installation (recommended)
Run `react-native link` after npm install.
That should be it.

###Manual installation
Same old, same old...

`android/app/build.gradle`
```diff
dependencies {
+   compile project(path: ':react-native-fingerprint-android')
    compile fileTree(dir: "libs", include: ["*.jar"])
    compile "com.android.support:appcompat-v7:23.0.1"
    compile "com.facebook.react:react-native:+"  // From node_modules
}
```

`android/settings.gradle`
```diff
include ':app'
+include 'react-native-fingerprint-android'
+project(':react-native-fingerprint-android').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-fingerprint-android/android')
```

`android/app/src/main/java/com.your.package/MainApplication.java`
```diff
        @Override
        protected List<ReactPackage> getPackages() {
            return Arrays.<ReactPackage>asList(
-                   new MainReactPackage()
+                   new MainReactPackage(),
+                   new FingerprintPackage()
            );
        }
```

##Todo
- [ ] CryptoObject support     
- [ ] [Samsung fingerprint SDK?](http://developer.samsung.com/galaxy/pass)