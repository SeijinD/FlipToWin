# FlipToWin 🎮

**FlipToWin** is a modern, high-performance Jetpack Compose library for implementing "Flip to Win" style games in Android applications. Built with **Kotlin 2.0** and following **Clean Architecture** principles, it provides a premium, production-ready user experience with smooth animations and robust state management.

![fliptowin](https://github.com/user-attachments/assets/835dcc26-46b4-4e91-83be-0828ebe07500)

## ✨ Features

### Technical Strengths
- **Unidirectional Data Flow (UDF)**: Predictable, immutable state management using `StateFlow`.
- **Backend-First Reveal**: Integrated safety boundary — prize claims are confirmed via repository before any visual reveal animations begin.
- **Clean Architecture**: Strict separation of Domain, Data, and UI layers for maximum testability and maintainability.
- **Hilt-Ready**: Fully compatible with Dagger Hilt for seamless dependency injection.
- **Coil 3 Integration**: Optimized image handling with proactive **pre-loading and caching** for instant reward display.

### UI & UX
- **Performance Animations**: Choreographed flip, wiggle, zoom, and translation animations optimized for standard and foldable devices.
- **Responsive Layouts**: Automatically recalculates grid geometry and animation targets on screen rotation or resize.
- **Mirror-Correction**: Proprietary icon orientation correction during 180° 3D rotations.
- **Native Accessibility**: Built-in `contentDescription` mappings and Haptic Feedback synced to animation midpoints.

## 🚀 Getting Started

### 1. Basic Integration

Add the `FlipToWinScreen` to your Compose UI. It requires a `FlipToWinViewModel` (usually injected via Hilt).

```kotlin
FlipToWinScreen(
    viewModel = hiltViewModel(),
    onResult = { rewardType -> 
        // Logic for successful reward reveal
    },
    onError = { code ->
        // Handle initialization or configuration failures
    },
    onClaimError = { message ->
        // Handle award claiming errors (e.g. network failure)
    }
)
```

### 2. Architecture Overview

The library is organized into three distinct layers:

- **UI Layer (`ui`)**: Passive Composables, StateFlow-driven ViewModels, and immutable state models (`FlipToWinUiState`).
- **Domain Layer (`domain`)**: Pure Kotlin business logic, Use Cases, and Repository contracts.
- **Data Layer (`data`)**: Repository implementations (includes a `MockFlipToWinRepository` for demo/testing).

## 🧪 Testing

The project includes a comprehensive suite of unit tests for state mapping and animation logic:

```bash
./gradlew :fliptowin:testDebugUnitTest
```

Key coverage areas in `FlipToWinUiMapperTest.kt`:
- Selection state preservation on configuration changes.
- Duplicate reward prevention.
- "No Win" state prioritization and grid distribution.
- Edge cases for reward pools and grid sizes.

## 🛠 Tech Stack

- **Kotlin 2.0** (K2 Compiler)
- **Jetpack Compose** (Material 3)
- **Kotlin Coroutines & Flow**
- **Dagger Hilt**
- **Coil 3** (Image Loading & Pre-fetching)
- **kotlinx.collections.immutable** (Persistent Collections)

## 🤝 Contributing

Contributions are welcome! Whether you have ideas for new animation styles, configuration parameters, or have found a bug, feel free to open an **issue** or submit a **pull request**.

## 👤 Author

**Georgios (SeijinD)**  
Android Developer | [GitHub](https://github.com/SeijinD) | [LinkedIn](https://www.linkedin.com/in/seijind/)

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.
