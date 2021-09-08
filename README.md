# tt-rfid-passive-api-testapp-android

The RfidPassiveApiTestappAndroid app depends on two modules:

 * [rfidpassiveapilib](https://github.com/tertiumtechnology/tt-rfid-passive-api-lib-android)
 * [txrxlib](https://github.com/tertiumtechnology/tt-txrx-lib-android)

The current project configuration require that the main project and all modules folder should reside in the same directory, as in the example below:

```bash
AndroidProjectsFolder/
¦
+-- tt-rfid-passive-api-testapp-android/
  ¦
  +-- app/
  +-- settings.gradle
  ...
+-- tt-rfid-passive-api-lib-android/
  ¦
  +-- rfidpassiveapilib/
  ...
+-- tt-txrx-lib-android/
  ¦
  +-- txrxlib/
  ...
...
```

If needed, you can change the settings.gradle file inside the main project folder, according to your project structure.

## Requirements for version 2.0 and later
Migration to api level 30 (Android 11) require the replacement of original support library APIs with the new androidx
 APIs (see [here](https://developer.android.com/jetpack/androidx/migrate) for more instructions) and the ACCESS_FINE_LOCATION permission

The `supportLibrayVersion` variable, defined as ext in the root project build.gradle file, must be replaced with the new `androidxAnnotationVersion` variable (current value is 1.2.0)