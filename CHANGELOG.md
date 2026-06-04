# Changelog

All notable changes to the Shumen app will be documented in this file.

## [1.8] - 2024-05-30

### Added
- **Configurable Alarm Volume**: Added a new slider in the Settings screen to adjust the loudness of the alarm sound (0-100%).

## [1.7] - 2024-05-29

### Added
- **Custom Alarm Sounds**: Users can now select system ringtones/alarms or provide their own custom audio files for the noise alert.
- **Fall-back Protection**: Integrated automatic fallback to the industrial "Super Alarm" if a custom sound fails to load or is unavailable.

## [1.6] - 2024-05-28

### Added
- **Super Alarm System**: Layered dual `ToneGenerator` instances to create a piercing "industrial siren" effect. The alarm now uses multiple dissonant tones simultaneously for maximum audibility and urgency.

## [1.5] - 2024-05-27

### Added
- **Configurable Alarm Duration**: Users can now set how long the audible and visual alarms last (1-10 seconds) via a new setting in the Settings screen.

## [1.4] - 2024-05-26

### Fixed
- **Slider Responsiveness**: Resolved an issue where the Alert Level slider would appear frozen on some TV devices. Added local state buffering for immediate visual feedback during D-pad adjustment.
- **TV Instructions**: Added explicit "Use LEFT/RIGHT on your remote" instruction to the Alert Level setting.

## [1.3] - 2024-05-25

### Added
- **3-Second Audible Alarm**: Integrated `ToneGenerator` to play a clear alert tone when noise exceeds the user-defined Alert Level.
- **Pulsing Visual Alarm**: High-visibility red overlay with animated icons for immediate attention.
- **Auto-Pause Monitoring**: Smart logic to pause microphone processing during the alarm period, preventing feedback and self-triggering.

### Changed
- **Terminology Update**: Renamed "Noise Threshold" to **"Alert Level (dB)"** for clearer user communication.
- **Precision Adjustment**: Increased slider granularity to **1 dB increments** (40-100 dB) for professional-grade control.
- **Meter Refinement**: Added rounded accents to the Linear DAW-style meter for a more polished aesthetic.
- **Version Update**: Incremented `versionCode` to 4 and `versionName` to 1.3.

### Fixed
- **System Theme Bleed-through**: Enforced an explicit black surface layer to prevent white backgrounds on some Android TV 11 devices.

## [1.1] - 2024-05-24

### Added
- **DAW-style Linear Meter**: Replaced the circular meter with a vertical, segment-based LED level meter for a professional audio aesthetic.
- **"Quiet Please" Overlay**: Enhanced Quiet Mode with a full-screen, high-contrast visual alert for prolonged noise disruptions.
- **Pure Black OLED Theme**: Forced the application to use a pure black (`#000000`) background for maximum contrast and energy efficiency on TVs.

### Changed
- **Version Update**: Incremented `versionCode` to 2 and `versionName` to 1.1.
- **D-pad Navigation Refinement**: Improved focus management and visual feedback for TV remote interaction.

### Fixed
- **Layout Stability**: Implemented weight-based layouts and fixed-width containers to prevent UI "jumping" when decibel values change digits.

---

## [1.0] - 2024-05-23

### Added
- Initial release.
- Real-time dB monitoring using `AudioRecord`.
- Basic Settings screen for threshold and sensitivity adjustment.
- Navigation 3 implementation.
- Material 3 theme with neon accent colors.
- Adaptive app icon for Google TV.
