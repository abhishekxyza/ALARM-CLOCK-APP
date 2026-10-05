# AeroAlarm ⏰✨

> **The Ultimate Unbreakable Native Android Alarm Clock powered by Gemini AI, CameraX Barcode Tasks, & Pitch-Black One UI Aesthetics.**

AeroAlarm is a feature-rich, high-performance native Android application engineered in **Java**. It combines natural language AI voice control, CameraX + ML Kit barcode dismissal tasks, dynamic anime character artwork, sleep analytics, and an elegant One UI pitch-black dark interface.

---

## 🌟 Key Features

### 1. ✨ Gemini AI Voice & Text Assistant
- **Natural Language Parsing**: Powered by the **Google GenAI SDK (`gemini-1.5-flash`)**. Parse complex commands like *"Set an alarm for 7:30 AM labeled Gym"* or *"Switch to World Clock"*.
- **Voice Activation (`🎤 SpeechRecognizer`)**: Speak your command using Android's native speech-to-text voice recognition.
- **Permanent AI UI Access**: Dedicated sparkling AI button (`✨`) in the header across all tabs for instant one-tap control.
- **Robust Fallback Engine**: Built-in regex fallback parser ensures commands execute instantly even during network drops.

### 2. 📱 One UI Pitch-Black 4-Tab Suite
- **Alarm Tab**: Set precise alarms down to the second (`08:00:00 AM`). Includes quick repeat presets (`Mon to Fri`, `Sat, Sun`, `Sunday`, `Every day`).
- **World Clock Tab**: Interactive **Analog Clock Dial** (`AnalogClockView`) + **Digital Clock** toggle (`00:00:27`). Live time offsets for global cities (Tokyo, London, New York, Paris, Sydney, Dubai, etc.).
- **Timer Tab**: Precision Hour/Minute/Second pickers, preset tasks ("Meeting", "Sleep", "Exercise", "Dinner time"), live countdown, and circular action controls (`X` Reset, `▶` Play/Pause, `🔔` Sound picker).
- **Stopwatch Tab**: High-precision 10ms millisecond display (`00:00.00`), Lap history with lap durations and cumulative total time.

### 3. 📷 CameraX + ML Kit Barcode Dismissal Task
- **Unbreakable Task Dismissal**: Force yourself out of bed by locking the alarm dismissal button until you scan a registered physical barcode (e.g. toothpaste, coffee jar) using live CameraX feed and Google ML Kit.

### 4. 🖼️ Dynamic Anime Artwork & Custom Wallpapers
- **Anime Backgrounds**: Automatically fetches random anime character artwork using **Retrofit + Gson** via the Jikan API.
- **Oshi Lock**: Lock in your favorite anime character using custom image URLs.
- **App Wallpaper Themes**: Choose from **Pitch Black** (`#000000`), **Midnight Navy** (`#0f172a`), **Dark Purple** (`#1e1b4b`), **Dark Emerald** (`#064e3b`), or select a **Custom Photo from Gallery** via SAF & Glide.

### 5. 🔊 Unbreakable Alarm Service & Altitude Ascent
- **Foreground Service**: Bypasses lock screen using `fullScreenIntent` and `mediaPlayback` foreground service.
- **Altitude Ascent Volume Ramp**: Volume scales gradually from 10% to 100% every 20 seconds to prevent morning shocks.

### 6. 📊 Sleep Analytics & System Settings
- **Room Database**: Local Room DB (`SleepStatEntity`, `SleepStatDao`, `AppDatabase`) tracks 7-day average wake-up speed.
- **Full Settings Interactivity**: Date & Time sub-settings (Auto-Set Time, Date/Time pickers, 24-Hour Format toggle, Automatic Time Zone, System Dual Clock) and National/Regional Public Holidays country selector.

---

## 🛠️ Tech Stack & Architecture

| Category | Technology / Library |
| :--- | :--- |
| **Language** | Java (Native Android API 24+) |
| **Build System** | Gradle Kotlin DSL (`build.gradle.kts`) |
| **AI SDK** | Google GenAI SDK (`com.google.ai.client.generativeai:generativeai`) |
| **Camera & Vision** | CameraX (`androidx.camera`), Google ML Kit Barcode Scanning |
| **Database** | Room Persistence Library (`androidx.room`) |
| **Networking** | Retrofit 2 & Gson |
| **Image Loading** | Glide |
| **Asynchronous & Threading** | Executors, Handler/Looper, Guava Futures |

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Jellyfish (2023.3.1) or newer
- JDK 17
- Android Device / Emulator running Android 7.0 (API 24) or higher

### Installation
1. **Clone the Repository**:
   ```bash
   git clone https://github.com/abhishekxyza/ALARM-CLOCK-APP.git
   cd ALARM-CLOCK-APP
   ```
2. **Open in Android Studio**:
   - Open Android Studio -> `File` -> `Open` -> Select the `ALARM-CLOCK-APP` folder.
   - Allow Gradle to sync dependencies.
3. **Configure Gemini API Key**:
   - Open `MainActivity.java` and insert your Gemini API Key in `showAiAssistantDialog()`:
     ```java
     String apiKey = "YOUR_GEMINI_API_KEY_HERE";
     ```
4. **Build & Run**:
   - Connect your Android device or start an emulator.
   - Click `Run` (or press `Shift + F10`) to deploy the app.

---

## 📄 License
Licensed under the [MIT License](LICENSE). Built with ❤️ by [Abhishek](https://github.com/abhishekxyza).
