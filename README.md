# 🧮 Modern Android Scientific Calculator & Tools Suite

A modern, full-featured Android Scientific Calculator, Unit Converter, and Financial Suite built with **Kotlin**, **Jetpack Compose**, **Material 3**, and **Room Database**.

---

## ✨ Key Features

### 1. 🧮 Scientific & Standard Calculator
- **Instant Mode Switch**: Sleek pill toggle switch in the header to effortlessly transition between **Standard** and **Scientific** modes.
- **Dedicated Angle Toggle**: Quick-switch between **DEG** (Degrees) and **RAD** (Radians) angle modes.
- **Trigonometric & Hyperbolic Functions**: `sin`, `cos`, `tan`, `sin⁻¹`, `cos⁻¹`, `tan⁻¹`, `sinh`, `cosh`, `tanh`, `sinh⁻¹`, `cosh⁻¹`, `tanh⁻¹`.
- **Advanced Math Operations**: Powers ($x^y$), square roots ($\sqrt{x}$), cube roots ($\sqrt[3]{x}$), factorials ($n!$), natural logarithm ($\ln$), common logarithm ($\log$), and reciprocal ($1/x$).
- **Smart Arithmetic & Implicit Multiplication**: Auto-evaluates trailing operators, handles negative numbers cleanly, and supports implicit syntax like `2π`, `5ln(e)`, `2sin(30)`.
- **Smart Percentages**: Quick calculations for additions (`+10%`), discounts (`-15%`), multiplication, and standalone percentages.

---

### 2. 🎯 Interactive Draggable Cursor & Mid-Expression Editing
- **Droplet Handle & Drag-to-Seek**: Live horizontal cursor dragging across numbers with a comfortable teardrop handle and glowing drag halo.
- **Tactile Haptic Feedback**: Performs subtle haptic ticks as the cursor steps between digits.
- **1-Tap Direct Placement**: Tap anywhere in the expression to place the cursor between any characters or operators.
- **Precision Backspace & Insertions**: Delete characters or multi-character tokens (`sin(`, `cos⁻¹(`, ` + `) at the cursor, and type digits or operations directly in place.

---

### 3. 🌐 Flexible Number Formatting (Indian & International)
- **Indian Numbering System**: Format large values with traditional Indian commas (*Lakhs & Crores*, e.g., `12,34,567`).
- **International Numbering System**: Format values with standard thousands grouping (*Millions & Billions*, e.g., `1,234,567`).
- **Universal Application**: One-tap toggle in the 3-dot menu applies consistently across the calculator and all converter tools.

---

### 4. 🧰 Converters & Tools Hub
Access 15+ specialized utilities directly from the top header:
- **Instant Search**: Quickly filter through all converters and tools with live search.
- **11 Unit Converters**:
  - 💵 **Live Currency Converter**: Automatically fetches and caches real-time internet exchange rates (USD, EUR, GBP, INR, CAD, AUD, JPY, and more).
  - 📏 **Length**: Meter, Kilometer, Centimeter, Millimeter, Mile, Foot, Inch, etc.
  - 📐 **Area**: Square Meter, Square Foot, Acre, Hectare, Square Yard, etc.
  - 🧪 **Volume**: Liter, Milliliter, Gallon, Quart, Pint, Cup, etc.
  - ⚖️ **Weight / Mass**: Kilogram, Gram, Milligram, Metric Ton, Pound, Ounce, etc.
  - 🌡️ **Temperature**: Celsius (°C), Fahrenheit (°F), Kelvin (K).
  - 🚗 **Speed**: km/h, mph, m/s, Knot, ft/s.
  - ⏱️ **Pressure**: Pascal, Bar, PSI, atm, mmHg.
  - ⚡ **Power**: Watt, Kilowatt, Megawatt, Horsepower, BTU/h.
  - 💾 **Data Storage**: Bits, Bytes, KB, MB, GB, TB, PB.
  - 🚀 **Data Speed**: bps, Kbps, Mbps, Gbps, KB/s, MB/s.
  - *All converters include a dedicated on-screen numpad with draggable cursor support.*
- **💻 Programmer Number System**:
  - Simultaneous real-time conversion for **Decimal (DEC)**, **Hexadecimal (HEX)**, **Octal (OCT)**, and **Binary (BIN)**.
  - Base-aware virtual keypad that dynamically enables only valid characters.
- **💰 Finance & Tax Calculators**:
  - **Loan & EMI Calculator**: Monthly EMI, total interest, total payment, and principal-to-interest breakdown bar.
  - **Investment & SIP Calculator**: Monthly SIP and lump-sum wealth projections with visual return graphs.
  - **GST Calculator**: Add/Remove GST with standard slabs (3%, 5%, 12%, 18%, 28%) and custom tax rates.
  - **Discount Calculator**: Final discounted price, total money saved, and optional coupon codes.
- **📅 Date, Time & Health Utilities**:
  - **Date Calculator**: Date difference, duration addition and subtraction.
  - **Time Calculator**: Time interval differences, addition, and subtraction.
  - **Age Calculator**: Exact breakdown in years, months, and days with next birthday countdown.
  - **BMI Calculator**: Body mass index with metric/imperial units and color-coded health ranges.

---

### 5. 🎨 AMOLED Dark Theme & Orientation
- **Pitch Black AMOLED Theme**: Pure `#000000` dark mode optimized for battery conservation and high-contrast OLED displays.
- **Light & System Modes**: High-visibility light mode and system-following theme option.
- **Adaptive Two-Pane Landscape Mode**: Side-by-side display and keypad layouts ensuring zero button squishing on tablets and horizontal phones.
- **Calculation History**: Persistent SQLite/Room database storage with one-tap recall of previous formulas and answers.

---

## 🛠️ Tech Stack & Architecture

- **Language**: Kotlin 2.1
- **UI Toolkit**: Jetpack Compose (Material 3)
- **Local Storage**: Room Database & DataStore Preferences
- **Architecture**: MVVM with unidirectional data flow (UDF)
- **Minimum SDK**: Android 8.0 (API 26)
- **Target SDK**: Android 15 (API 35)

---

## 🚀 Building & Running

1. Clone the repository:
   ```bash
   git clone https://github.com/chiragsharma006/CalculatorApp.git
   ```
2. Open the project in **Android Studio** (Ladybug or newer).
3. Let Gradle sync and click **Run (▶)** on your device or emulator.
4. Alternatively, build the debug APK from the command line:
   ```bash
   ./gradlew assembleDebug
   ```
   The APK will be generated at:
   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```

---

## 📄 License
This project is open source and available under the [MIT License](LICENSE).
