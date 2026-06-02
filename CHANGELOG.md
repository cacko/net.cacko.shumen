# Changelog

All notable changes to the Shumen app will be documented in this file.

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
