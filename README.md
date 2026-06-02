# Shumen - TV Noise Meter

Shumen is a specialized noise monitoring application designed for Google TV (Android 11+). It helps users maintain an optimal viewing environment by monitoring ambient noise levels in real-time and providing visual alerts when the environment becomes too loud.

## 🚀 Features

- **Real-time Monitoring**: High-precision decibel calculation using a DAW-style linear LED meter.
- **Visual Threshold Alerts**: Dynamic UI feedback and background glows when noise exceeds user-defined limits.
- **Quiet Mode**: A dedicated "Quiet Please" overlay that triggers if noise persists above the threshold, perfect for movies or sleeping environments.
- **Customizable Settings**: Adjust noise limits and microphone sensitivity to match your room's acoustics.
- **Always Dark Mode**: A pure black (`#000000`) theme optimized for OLED TV efficiency and high-contrast viewing.
- **TV Optimized**: Full D-pad navigation support for seamless interaction with a standard TV remote.

## 🛠 Tech Stack

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material Design 3 (M3)
- **Navigation**: [Jetpack Navigation 3](https://developer.android.com/guide/navigation) (State-driven)
- **Audio Capture**: Android `AudioRecord` API for real-time PCM processing.
- **Data Persistence**: [Jetpack DataStore](https://developer.android.com/topic/libraries/architecture/datastore) (Preferences) for settings.
- **Architecture**: MVVM with Kotlin Coroutines and Flow.

## 🏁 Getting Started

1. **Clone the repository**:
   ```bash
   git clone https://github.com/your-repo/shumen.git
   ```
2. **Open in Android Studio**:
   Ensure you have the latest version of Android Studio (Ladybug or newer).
3. **Build and Run**:
   Connect your Google TV device or use an emulator and run the `:app:assembleDebug` task.
4. **Grant Permissions**:
   The app requires `RECORD_AUDIO` permission to monitor ambient noise.

## ⚖ License
MIT License - See the project for details.
