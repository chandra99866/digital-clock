# Clock Controller — Android Studio project

A polished native Android starter app for controlling a digital clock over **Classic Bluetooth RFCOMM/SPP**. Every interactive button gives a Toast feedback message, including connection, disconnect, preset commands, and custom command submission.

## Important protocol note
The uploaded APK's available analysis indicates the standard SPP UUID `00001101-0000-1000-8000-00805F9B34FB`, but its actual application-specific clock command strings were not confirmed. This project therefore uses clearly labeled example commands (`ON`, `OFF`, `NIGHT`, `TIME`) and appends a newline to each sent command. **Edit the CMD_* and LINE_END constants at the top of MainActivity.java (commands and line ending) to match your actual controller firmware before use.**

## Build
1. Open this folder in Android Studio (recent version with Android Gradle Plugin 8.7.3 support).
2. Allow Gradle sync to finish; install Android SDK platform 35 if prompted.
3. Build > Build APK(s).
4. Install the generated debug APK on an Android device.
5. Pair the clock controller from Android Settings > Bluetooth first, then open the app and select the paired device.

## Current scope
- Classic Bluetooth paired-device picker and RFCOMM SPP connection.
- Connection status, disconnect action, preset command buttons, and custom text command.
- Toast messages for user actions and success/failure feedback.
- No BLE scanner/GATT support yet; BLE uses a different protocol and requires the controller's service/characteristic UUIDs.

## Permissions and safety
Bluetooth Connect permission is requested on Android 12+. Only connect to devices you own or are authorized to control. The app does not collect analytics or send data over the internet.
