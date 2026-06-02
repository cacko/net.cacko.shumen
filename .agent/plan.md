# Project Plan

A noise meter application named 'shumen' for television supporting Google TV 11. The app monitors ambient noise levels and provides visual feedback/alerts if noise exceeds thresholds.

## Project Brief

# Project Brief: Shumen - TV Noise Meter

Shumen is a specialized noise monitoring application designed for Google TV (Android 11+). It helps users maintain an optimal viewing environment by monitoring ambient noise levels in real-time and providing unobtrusive visual alerts when the environment becomes too loud.

## Features

*   **Real-time Ambient Noise Monitoring**: A live decibel (dB) meter that visualizes current room noise levels directly on the TV screen.
*   **Visual Threshold Alerts**: Dynamic UI feedback (such as color shifts or subtle overlays) that triggers when ambient noise exceeds a user-defined threshold.
*   **Adjustable Sensitivity Settings**: A simple configuration interface to set the noise limit and alert intensity, tailored for different room sizes and viewing preferences.
*   **TV-Optimized "Quiet" Mode**: A dedicated mode that dims the screen or displays a "Quiet Please" graphic when sound levels disrupt the viewing experience for a prolonged period.

## High-Level Tech Stack

*   **Language**: Kotlin
*   **UI Framework**: Jetpack Compose with **Material Design 3 (M3)** for a modern, TV-friendly aesthetic.
*   **Navigation**: **Jetpack Navigation 3** (state-driven) to handle transitions between the monitor and settings screens.
*   **Adaptive Strategy**: **Compose Material Adaptive** library to ensure the UI scales correctly for various television resolutions and aspect ratios.
*   **Concurrency**: **Kotlin Coroutines and Flow** for efficient, non-blocking capture and processing of audio data from the microphone.
*   **Audio Capture**: Android's `AudioRecord` API for high-precision, real-time decibel calculation.
*   **Lifecycle Management**: **Lifecycle Runtime Compose** to ensure microphone monitoring starts and stops correctly with the app's visibility.

## Implementation Steps
**Total Duration:** 30m 21s

### Task_1_Foundations_and_Audio_Logic: Configure the project for Google TV (leanback), set up the Material 3 theme with a vibrant color scheme and edge-to-edge support, and implement the core AudioRecord logic to calculate decibel levels with proper permission handling.
- **Status:** COMPLETED
- **Updates:** Configured project for Google TV, implemented Material 3 theme with vibrant colors, and added AudioRecord logic for decibel calculation. Handled RECORD_AUDIO permission.
- **Acceptance Criteria:**
  - AndroidManifest.xml includes TV hardware and leanback features
  - Material 3 theme implemented with vibrant colors
  - AudioRecord captures audio and calculates dB levels accurately
  - RECORD_AUDIO permission is handled at runtime
- **Duration:** 21m 58s

### Task_2_Monitor_Screen_and_Alerts: Develop the main Monitor screen using Jetpack Compose, featuring a real-time dB meter visualization and dynamic visual alerts (color shifts or overlays) for noise thresholds, including the TV-optimized 'Quiet' mode.
- **Status:** COMPLETED
- **Updates:** Developed the main Monitor screen with a real-time dB meter, visual alerts, and Quiet Mode. The UI follows Material Design 3 guidelines.
- **Acceptance Criteria:**
  - Monitor screen displays live decibel updates
  - Visual alerts trigger when noise exceeds thresholds
  - Quiet Mode display logic triggers appropriately
  - The implemented UI follows Material Design 3 guidelines
- **Duration:** 2m 14s

### Task_3_Settings_and_Data_Persistence: Build the Settings screen using Navigation 3 to allow users to adjust noise limits and sensitivity, persisting these values using Jetpack DataStore and ensuring they affect the Monitor screen behavior.
- **Status:** COMPLETED
- **Updates:** Implemented Settings screen with Jetpack DataStore for persistence. Users can adjust noise threshold and sensitivity, which reactively updates the Monitor screen. Navigation 3 is fully integrated.
- **Acceptance Criteria:**
  - Navigation between Monitor and Settings screens is seamless
  - Users can adjust and save threshold and sensitivity settings
  - Settings are persistent across app restarts
- **Duration:** 54s

### Task_4_Icon_UI_Refinement_and_Verification: Create an adaptive app icon suitable for a noise meter, refine the UI for TV-specific interaction (D-pad focus management), and perform a final verification of the entire application stability and requirements.
- **Status:** COMPLETED
- **Updates:** Final verification completed by critic agent. App is stable, functionally complete, and optimized for TV. D-pad navigation and adaptive icon are implemented correctly. M3 compliance and edge-to-edge support verified.
- **Acceptance Criteria:**
  - Adaptive app icon matches app function
  - UI is fully navigable via TV remote (D-pad)
  - Project builds and runs without crashes
  - All existing and new tests pass
- **Duration:** 5m 15s

