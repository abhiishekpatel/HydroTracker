# HydroTrack

Your daily water intake companion for optimal hydration while supplementing with creatine.

## Overview

HydroTrack is a native Android water intake tracking application built with Kotlin and Jetpack Compose, designed specifically for fitness enthusiasts and creatine users who need to maintain consistent hydration levels. With an intuitive interface, gamified streak tracking, and visual progress feedback, HydroTrack makes staying hydrated effortless and engaging.

## Features

- **Visual Progress Tracking**: Watch a water glass fill up as you log your intake throughout the day
- **Quick-Add Buttons**: Log water in common volumes (250ml, 500ml, 750ml, 1L) with a single tap
- **Streak Tracking**: Build streaks of consistent daily hydration to stay motivated and accountable
- **Persistent Storage**: Your data is automatically saved and loads when you return
- **Celebration Animations**: Get rewarded with engaging animations when you hit your 4L daily goal
- **Educational Tips**: Learn hydration best practices tailored for creatine users
- **Smart Daily Reset**: Automatic reset at midnight with intelligent streak management

## Quick Start

### Prerequisites

- Android Studio (latest version with Kotlin support)
- Android SDK 24+ (minimum API level)
- Kotlin 1.8+
- Gradle 8.0+
- Android device or emulator running Android 7.0 (API 24) or higher

### Installation

1. Clone the repository:
```bash
git clone <repository-url>
cd hydrotrack
```

2. Open the project in Android Studio:
   - File → Open → Select the project directory
   - Wait for Gradle sync to complete

3. Build the project:
   - Build → Build Bundle(s) / APK(s) → Build APK(s)

4. Run on device or emulator:
   - Select your device/emulator from the device selector
   - Click the Run button or press Shift+F10

## Usage

### Logging Water Intake

1. Open HydroTrack on your Android device
2. Tap one of the preset buttons (250ml, 500ml, 750ml, or 1L) to log water
3. Watch your progress fill the visual glass
4. Reach 4 liters to complete your daily goal and maintain your streak

### Understanding Your Progress

- **Water Glass**: Fills proportionally as you log intake
- **Percentage Display**: Shows your progress toward the 4L goal
- **Current/Remaining**: View your total intake and how much more you need
- **Streak Counter**: Track your consecutive days of meeting the goal

## Technology Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM (Model-View-ViewModel)
- **Database**: Room Database
- **Storage**: DataStore (SharedPreferences alternative)
- **Build Tool**: Gradle
- **Minimum API**: Android 7.0 (API 24)
- **Target API**: Android 14+ (API 34+)

## Project Structure

```
hydrotrack/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── kotlin/com/hydrotrack/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── ui/
│   │   │   │   │   ├── screens/
│   │   │   │   │   ├── components/
│   │   │   │   │   └── theme/
│   │   │   │   ├── viewmodel/
│   │   │   │   ├── data/
│   │   │   │   │   ├── db/
│   │   │   │   │   └── repository/
│   │   │   │   └── util/
│   │   │   └── AndroidManifest.xml
│   │   └── test/
│   └── build.gradle.kts
├── build.gradle.kts
└── settings.gradle.kts
```

## Key Functional Requirements

### Core Functionality

- Multiple water intake entries per day with cumulative tracking
- Visual glass display with smooth animations (60 FPS)
- Automatic data persistence using browser storage
- Undo function to correct logging mistakes
- Streak increment on goal completion
- Streak reset at midnight if goal not met

### Performance Targets

- Initial page load: < 2 seconds on 3G
- Button response time: < 100ms
- Smooth 60 FPS animations
- Storage operations: < 50ms

## Device Support

| Android Version | API Level | Status |
|---|---|---|
| Android 7.0 (Nougat) | 24 | Minimum (supported) |
| Android 8.0+ (Oreo) | 26+ | Fully supported |
| Android 12+ (S) | 31+ | Fully optimized |
| Android 14+ (UpsideDownCake) | 34+ | Latest target |

**Supported Devices**: All phones and tablets running Android 7.0 or higher

## Development

### Available Commands

```bash
# Build the app
./gradlew build

# Build and install on device/emulator
./gradlew installDebug

# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest

# Clean build
./gradlew clean

# Check for lint issues
./gradlew lint
```

### Development Timeline

**Phase 1 (MVP)**: 4 weeks
- Core functionality and visual display
- Streak tracking and daily reset
- UI polish and animations

**Phase 2 (Enhancement)**: 2 weeks
- Educational content integration
- Performance optimization
- Cross-browser testing

**Phase 3 (Launch)**: 1 week
- User acceptance testing
- Documentation
- Deployment and monitoring

## Target Users

**Primary Persona**: The Fitness Enthusiast

- Ages 18-45, active gym-goers
- Currently using creatine supplementation
- Comfortable with mobile/web apps
- Uses fitness tracking applications

## Success Metrics

| Metric | Target | Measurement |
|--------|--------|-------------|
| Daily Active Users | 1,000 (3 months) | Analytics tracking |
| Goal Completion Rate | 65% daily | Storage analysis |
| Average Streak Length | 7+ days | User data |
| 7-Day Retention | 50% | Cohort analysis |
| Average Session Duration | 30 seconds | Analytics |

## Roadmap

### Version 1.0 (Current)
- Visual water tracking with preset buttons
- Streak management system
- Educational tips for creatine users
- Persistent data storage

### Version 2.0 (Future)
- Push notifications for hydration reminders
- Progressive Web App (PWA) support
- Integration with fitness trackers (Apple Health, Google Fit)
- Customizable daily goals based on body weight
- Historical data visualization and trends
- Social features (challenges, leaderboards)
- Dark mode support
- Multi-language support

## Contributing

We welcome contributions! Please:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## Known Limitations & Risks

- **Browser Storage Limits**: Some browsers have storage limitations; handled with graceful error notifications
- **App Visibility**: Users may forget to open the app daily (future: push notifications)
- **Engagement**: May require additional gamification for sustained user engagement
- **Performance**: Older devices may experience animation slowness (optimizations in progress)

## Troubleshooting

**Data not persisting?**
- Check app permissions in Settings → Apps → HydroTrack
- Ensure the app has permission to access device storage
- Try clearing app cache: Settings → Apps → HydroTrack → Storage → Clear Cache
- If issue persists, uninstall and reinstall the app

**App crashes on startup?**
- Update Android Studio to the latest version
- Sync Gradle files: File → Sync Now
- Try cleaning the project: Build → Clean Project
- Rebuild the app: Build → Rebuild Project

**Animations not smooth?**
- Disable Android Studio debugger while testing performance
- Test on a physical device rather than emulator
- Check if other apps are consuming device resources
- Lower animation duration in device settings

**Button clicks not registering?**
- Ensure Compose version is up to date
- Check logcat for error messages
- Verify touch screen calibration on device

**Build fails with Gradle errors?**
- Update Gradle to version 8.0+
- Ensure Kotlin is updated to 1.8+
- Clear Gradle cache: `./gradlew cleanBuildCache`
- Invalidate Android Studio cache: File → Invalidate Caches

## Resources

- [Kotlin Documentation](https://kotlinlang.org/docs/)
- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Android Architecture Components](https://developer.android.com/guide/architecture)
- [Room Database Guide](https://developer.android.com/training/data-storage/room)
- [DataStore Documentation](https://developer.android.com/topic/libraries/architecture/datastore)
- [International Society of Sports Nutrition - Creatine Guidelines](https://www.issn.org)
- [American Council on Exercise - Hydration Best Practices](https://www.acefitness.org)

## License

[Specify your license here - e.g., MIT, Apache 2.0]

## Support

For issues, questions, or feedback:
- Open an issue on GitHub
- Contact the product team
- Check existing documentation

## Glossary

- **Creatine**: A naturally occurring compound that helps supply energy to muscles, commonly used as a supplement by athletes
- **Hydration**: The process of maintaining adequate water levels in the body
- **Streak**: Consecutive days of achieving the daily 4L hydration goal
- **PWA**: Progressive Web App - a web application that can be installed and work offline

---

**Version**: 1.0  
**Last Updated**: February 16, 2026  
**Status**: Draft
