# Arif_Ip_Tv Android starter project

This is an Android Studio project for a basic M3U/M3U8 IPTV player.

## Included
- Android phone launcher and Android TV launcher category
- Add an M3U playlist URL
- Open a local M3U playlist file
- Parse `#EXTINF` entries and show channel names
- HLS playback through AndroidX Media3 ExoPlayer
- Dark purple UI

## Build an APK
1. Install Android Studio on Windows.
2. Open this folder (`Arif_Ip_Tv`) in Android Studio.
3. Allow Gradle sync and install any requested Android SDK components.
4. Select **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
5. Find the debug APK at `app/build/outputs/apk/debug/app-debug.apk`.

## Notes
- This is source code, not a precompiled APK.
- Internet is required to download remote playlists and play online streams.
- Local M3U files can be opened from the device. A local playlist can still point to remote streams.
- Offline playback requires local media files on the device; no stream is downloaded by this app.
- Use only streams and playlists you own or are authorized to access.
- Some streams require headers, cookies, DRM, VPN, or a provider app and may not play in a generic player.
- A private address such as `10.x.x.x` works only on the same reachable private network/VPN.

## Build APK without installing Android Studio (cloud build)
1. Create a GitHub repository and upload the contents of this project to the repository (keep the `.github/workflows/android.yml` file).
2. Open the repository's **Actions** tab.
3. Select **Build Arif_Ip_Tv APK** and press **Run workflow** (or push to `main`).
4. When the run finishes, open it and download the `Arif_Ip_Tv-debug-apk` artifact.
5. Extract the downloaded artifact ZIP to get `app-debug.apk`.
