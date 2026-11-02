# Modern Android Scientific Calculator & Tools App

A modern, full-featured Android Calculator and Converter application built with **Kotlin**, **Jetpack Compose**, **Material 3**, and **Room Database**.

---

## ✨ Features

### 1. 🧰 Tools & Converters Hub
The top app bar now features a dedicated **Converters & Tools Hub button** (`GridView` icon) replacing the standalone theme palette button:
- **Instant Search**: Search bar to instantly filter across all 15+ converters and calculators.
- **Categorized Sections**: Clean Material 3 cards organized into:
  - 🌟 **Unit Converters**
  - 💰 **Finance & Tax**
  - 🧘 **Health & Utilities**
  - 🔢 **Programmer**

#### A. 🔄 Complete Unit Converters Suite
- 💵 **Live Internet Currency Converter**:
  - Automatically fetches real-time exchange rates from public exchange API (`open.er-api.com` / `api.exchangerate-api.com`) only when you open or click the Currency Converter.
  - **Smart Daily Caching**: Caches rates for the day to avoid wasteful background network calls; auto-updates daily upon opening.
  - **Live Status Header**: Displays real-time indicator (`🟢 Live Exchange Rates (Internet)` or `⚠️ Saved Offline Rates`) with last updated timestamp and a manual 1-tap refresh button.
  - Supported currencies: USD, EUR, GBP, INR, JPY, CAD, AUD, CHF, CNY, AED, SGD, SAR, NZD, ZAR, BRL, and more.
- 📏 **Length**: Meter, Kilometer, Centimeter, Millimeter, Mile, Yard, Foot, Inch, Nautical Mile.
- 📐 **Area**: Square Meter, Square Kilometer, Square Foot, Square Yard, Square Mile, Acre, Hectare, Square Inch.
- 🧪 **Volume**: Liter, Milliliter, Cubic Meter, US Gallon, US Quart, US Pint, US Cup, US Fluid Ounce, Imperial Gallon.
- ⚖️ **Weight / Mass**: Kilogram, Gram, Milligram, Metric Ton, Pound, Ounce, Stone, Carat.
- 🌡️ **Temperature**: Celsius (°C), Fahrenheit (°F), Kelvin (K).
- 🚗 **Speed**: Meter/second (m/s), Kilometer/hour (km/h), Miles/hour (mph), Knot (kn), Feet/second (ft/s).
- ⏱️ **Pressure**: Pascal (Pa), Kilopascal (kPa), Bar, PSI, Standard Atmosphere (atm), Torr / mmHg.
- ⚡ **Power**: Watt (W), Kilowatt (kW), Megawatt (MW), Horsepower (hp / metric PS), BTU/h, Foot-pound/second.
- **Dedicated On-Screen Converter Numpad**: Replaced the cluttered all-conversions list with a full, dedicated 4×4 on-screen Numpad (`0-9`, `00`, `.`, `AC`, `⌫`, `±`, `⇌`), eliminating system software keyboard popups and allowing instantaneous, real-time conversion typing with live blinking cursor and quick copy.

#### B. 💻 Programmer Number System Converter
- **Simultaneous Display**: Shows Decimal (DEC), Hexadecimal (HEX), Octal (OCT), and Binary (BIN) at the same time.
- **Intelligent Base-Aware Keypad**: Automatically enables/disables keys based on the selected base (e.g. only 0-1 for binary, 0-7 for octal, 0-9 for decimal, 0-F for hexadecimal).
- **One-Tap Copy**: Quick copy for any base value with toast confirmation.

#### C. 💰 Financial Calculators
- 🏦 **Loan & EMI Calculator**: Computes Monthly EMI, Total Interest Payable, Total Payment, and displays an interactive Principal vs. Interest percentage ratio bar.
- 📈 **Investment & SIP Calculator**: Supports both Monthly SIP and Lump Sum (Compound Interest) investments, calculating Total Invested, Estimated Wealth Gain, Total Future Value, and visual ratio bar.
- 🧾 **GST Calculator**: Supports **Add GST** (Exclusive to Gross) and **Remove GST** (Inclusive to Net) with standard slabs (3%, 5%, 12%, 18%, 28%) and custom percentages, showing complete CGST/SGST breakdowns.
- 🏷️ **Discount Calculator**: Computes final price to pay, total savings, and effective discount with optional extra coupon/club discount.

#### D. 🧘 Health & Date Calculators
- 🏃 **BMI Calculator**: Metric (cm / kg) and Imperial (ft+in / lbs) modes, real-time BMI score, color-coded health category chips (Underweight, Normal, Overweight, Obese Classes 1-3), and ideal healthy weight range.
- 🎂 **Age Calculator**: Exact age breakdown (Years, Months, Days), countdown to next birthday (Months, Days), total days/weeks/hours lived, and the day of the week born on.

---

### 2. 🧪 Full Scientific Calculator Mode & Instant Toggle
- **Top Mode Bar with Integrated Controls**:
  - The mode bar sitting directly above the keypad in Scientific Mode houses all primary calculation toggles in a single convenient header row:
    - **`[ 🔢 Standard / 📐 Scientific ]`**: Instant pill toggle to switch between standard arithmetic and scientific modes.
    - **`[ DEG / RAD ]`**: 1-tap angle mode toggle.
    - **`[ 2nd ]`**: Invert mode toggle (turns on secondary inverse and cubic functions).
    - **`[ ⌫ ]`**: Minimalist symbol-only backspace button.
- **Brackets & Percentage in Standard Mode**:
  - **Separate Bracket Buttons**: `(` (open bracket) and `)` (close bracket) have individual, dedicated buttons in the top keypad row (`AC`, `(`, `)`, `÷`) and are **never** merged into a confusing single button.
  - **Percentage Button**: `%` is placed directly in the bottom row beside `0`, `.`, and `=` for immediate access without entering scientific mode.
- **Optimized 5-Column × 7-Row Scientific Grid**:
  - Portrait Scientific Mode features an optimized, uniform **5-column grid** designed for effortless one-handed use, balanced proportions, and comfortable touch targets:
    - **Mode Bar**: `[ Standard ]` pill | `[ DEG / RAD ]` | `[ 2nd ]` | `[ ⌫ ]` Backspace.
    - **Row 1**: `[ sin (sin⁻¹) ]` `[ cos (cos⁻¹) ]` `[ tan (tan⁻¹) ]` `[ ln (eˣ) ]` `[ log (10ˣ) ]`
    - **Row 2**: `[ sinh (sinh⁻¹) ]` `[ cosh (cosh⁻¹) ]` `[ tanh (tanh⁻¹) ]` `[ |x| (1/x) ]` `[ x² (x³) ]`
    - **Row 3**: `[ e ]` `[ AC ]` `[ ( ]` `[ ) ]` `[ ÷ ]`
    - **Row 4**: `[ √(³√) ]` `[ 7 ]` `[ 8 ]` `[ 9 ]` `[ × ]`
    - **Row 5**: `[ ^ ]` `[ 4 ]` `[ 5 ]` `[ 6 ]` `[ − ]`
    - **Row 6**: `[ π ]` `[ 1 ]` `[ 2 ]` `[ 3 ]` `[ + ]`
    - **Row 7**: `[ ! ]` `[ 0 ]` `[ . ]` `[ % ]` `[ = ]`
  - **Adaptive Viewport Scaling**: Button heights dynamically scale (`38.dp` on compact <620dp screens, up to `49.dp` on tall screens) with adjusted vertical spacing (`3.dp` to `5.dp`) so that the expression display never squishes or clips.
- **Uniform 4-Column Keypad Layout in Standard Mode**:
  - **Row 1**: `[ AC ]` `[ ( ]` `[ ) ]` `[ ÷ ]`
  - **Row 2**: `[ 7 ]` `[ 8 ]` `[ 9 ]` `[ × ]`
  - **Row 3**: `[ 4 ]` `[ 5 ]` `[ 6 ]` `[ − ]`
  - **Row 4**: `[ 1 ]` `[ 2 ]` `[ 3 ]` `[ + ]`
  - **Row 5**: `[ 0 ]` `[ . ]` `[ % ]` `[ = ]`
  - Directly above `÷` sits the Backspace button `[ ⌫ ]` in the mode bar!
- **Comprehensive Scientific & Hyperbolic Functions**:
  - 📐 **Trigonometry**: `sin`, `cos`, `tan`, `sin⁻¹` (asin), `cos⁻¹` (acos), `tan⁻¹` (atan).
  - 🌐 **Hyperbolic**: `sinh`, `cosh`, `tanh`, `sinh⁻¹` (asinh), `cosh⁻¹` (acosh), `tanh⁻¹` (atanh).
  - 🔄 **Angle Modes**: 1-tap `DEG` / `RAD` toggle chip with exact boundary evaluations (`sin(30°)=0.5`, `cos(90°)=0`, `tan(45°)=1`).
  - ⚡ **2nd / INV Mode**: Switches primary functions to inverse functions (`sin⁻¹`, `cos⁻¹`, `tan⁻¹`, `sinh⁻¹`, `cosh⁻¹`, `tanh⁻¹`, `eˣ`, `10ˣ`, `1/x`, `x³`, `³√`).
  - 📈 **Logarithms, Roots & Powers**: `ln` (natural log), `log` (log base 10), `x²`, `x³`, `^` (xʸ), `√` (square root), `³√` (cube root), `1/x` (reciprocal).
  - 🔢 **Constants & Advanced**: `π`, `e`, `!` (factorial), `%` (percentage), `|x|` (absolute value), `(`, `)`.
  - 🤝 **Implicit Multiplication**: Supports `2sin(30)`, `5ln(e)`, `2π`, `2³√8`, `(2+3)cos(0)`.

---

### 3. 📱 Adaptive Landscape Mode Optimization
The app dynamically detects device orientation and switches to an optimized two-pane side-by-side layout:
- **Main Calculator**:
  - **Left Pane (40% width)**: Full-height `DisplaySection` with spacious expression and result view, eliminating all vertical squishing and scrolling issues.
  - **Right Pane (60% width)**: 5 proportional rows scaling automatically to available screen height via vertical weights (`Modifier.weight(1f)`), ensuring zero buttons or bottom rows (`0`, `.`, `=`) are ever cut off.
  - **Scientific Mode in Landscape (Optimized 7-Column × 5-Row Grid)**:
    - **Mode Bar**: `[ Standard ]` pill | `[ DEG / RAD ]` | `[ 2nd ]` | `[ ⌫ ]` Backspace.
    - **Row 1**: `[ sin (sin⁻¹) ]` `[ cos (cos⁻¹) ]` `[ tan (tan⁻¹) ]` `[ AC ]` `[ ( ]` `[ ) ]` `[ ÷ ]`
    - **Row 2**: `[ sinh (sinh⁻¹) ]` `[ cosh (cosh⁻¹) ]` `[ tanh (tanh⁻¹) ]` `[ 7 ]` `[ 8 ]` `[ 9 ]` `[ × ]`
    - **Row 3**: `[ ln (eˣ) ]` `[ log (10ˣ) ]` `[ |x| (1/x) ]` `[ 4 ]` `[ 5 ]` `[ 6 ]` `[ − ]`
    - **Row 4**: `[ e ]` `[ π ]` `[ x² (x³) ]` `[ 1 ]` `[ 2 ]` `[ 3 ]` `[ + ]`
    - **Row 5**: `[ √(³√) ]` `[ ^ ]` `[ ! ]` `[ 0 ]` `[ . ]` `[ % ]` `[ = ]`
    - All rows and buttons flexibly scale to the viewport with `.fillMaxHeight()` and `.weight(1f)`.
- **Tools & Converters in Landscape**:
  - **Tools Hub**: Expands from 2 columns to a 4-column responsive dashboard.
  - **Unit Converter**: Left pane houses category selector chips, live currency banner, From & To cards with live swap; right pane houses the full-height dedicated on-screen Converter Numpad.
  - **Number System**: Left pane displays all 4 base cards (HEX, DEC, OCT, BIN) with copy buttons; right pane houses the programmer keypad.
  - **Financial Calculators** (Loan EMI, Investment SIP, GST, Discount): Left pane for inputs; right pane for live results, charts, and breakdown bars.
  - **Health Calculators** (BMI, Age): Left pane for inputs/date picker; right pane for BMI score, category badge, exact age, and birthday countdown.

---

### 4. 🎯 Interactive Cursor & Precision Backspace
- **Interactive Blinking Cursor**:
  - Displays a clean, rounded, animated blinking vertical cursor line (`ElectricBlue` / theme accent) right in the expression display.
  - **1-Tap Direct Placement**: Tap anywhere in the expression text to place the cursor between any characters, digits, or operators.
  - **Start / End Targeting**: Tapping in empty space before or after the expression automatically moves the cursor to index 0 or the end of the expression.
  - **Smart Horizontal Auto-Scroll**: When working with long expressions, moving the cursor or typing automatically scrolls the viewport smoothly to ensure the active cursor is always visible.
  - **Post-Calculation Editing**: Tapping the expression after pressing `=` immediately reactivates editing mode at the exact tapped character so you can correct typos or adjust expressions without starting over.
- **Precision Backspace & Mid-Expression Editing**:
  - **Character Deletion at Cursor**: Pressing Backspace (`⌫`) deletes the character immediately preceding the cursor, allowing seamless typo correction anywhere in the expression.
  - **Smart Multi-Character Token Deletion**: Deleting before scientific functions (e.g., `sin(`, `cos⁻¹(`, `log(`, `10^(`, `ln(`, `abs(`), powers (`^2`, `^3`, `^(-1)`), or operators with spacing (` + `, ` − `, ` × `, ` ÷ `, ` ^ `) deletes the entire token as a single cohesive unit.
  - **Mid-Expression Number & Decimal Insertion**: Typing digits, decimals, or functions inserts them directly at the cursor position with smart rules (e.g. preventing duplicate decimal points in the same number segment).

---

### 5. 🧮 Smart Arithmetic Evaluation & Negative Numbers
- **Trailing Arithmetic Operator Evaluation**:
  - Entering expressions ending with an arithmetic operator (`+`, `-`, `−`, `*`, `×`, `/`, `÷`) after completing a calculation directly evaluates the completed answer when pressing `=` (and shows it in live preview):
    - `6+3+` $\rightarrow$ `= 9`
    - `6+3-` $\rightarrow$ `= 9`
    - `5+9+7+2+` $\rightarrow$ `= 23`
    - `5*6*` $\rightarrow$ `= 30`
    - `10/5*` $\rightarrow$ `= 2`
  - Incomplete operations or single operands followed by an operator safely return `"Incomplete expression"`:
    - `5*=` $\rightarrow$ `Incomplete expression`
    - `5+=` $\rightarrow$ `Incomplete expression`
    - `5/=` $\rightarrow$ `Incomplete expression`
    - `5-=` $\rightarrow$ `Incomplete expression`
    - `^=`, `%=`, `!=` $\rightarrow$ `Incomplete expression` or syntax error
- **Seamless Negative Number Arithmetic**:
  - Directly multiply, divide, power, or compute with negative numbers without needing sign-toggle keys:
    - `6 × -3` $\rightarrow$ `= -18`
    - `6 ÷ -2` $\rightarrow$ `= -3`
    - `2 ^ -3` $\rightarrow$ `= 0.125`
    - `10 + -5` $\rightarrow$ `= 5`
    - `-6 * -3` $\rightarrow$ `= 18`
  - Tapping `−` after `×`, `÷`, `^`, or `(` automatically appends the negative sign instead of replacing the operator.

---

### 3. 🎨 Modern Overflow Menu & Theme System
- **Ultra-Modern 3-Dot Dropdown Menu**:
  - Redesigned with a sleek 20dp rounded squircle card with subtle glowing border and AMOLED dark elevation.
  - **Tinted Icon Containers**: Dedicated squircle icon containers matching app accents:
    - 📜 **History**: Electric blue badge with subtitle "Past calculations".
    - 🎨 **Theme**: Purple badge with subtitle "Light & Dark AMOLED".
    - ℹ️ **App Info**: Emerald badge with subtitle "Version 1.0.0 & tools".
- **Theme Modes**:
  - **System Default**: Follows the Android OS system setting, automatically mapping dark mode to AMOLED.
  - **Light Mode**: Crisp, high-contrast palette tailored for bright environments.
  - **Dark Mode (AMOLED Pitch Black)**: Pure `#000000` deep OLED black theme with glowing electric blue accents, matching the custom 3D app icon and conserving battery.
- **Persistence**: Remembers selected theme across app restarts via `ThemePreferences`.

---

### 4. 🖼️ Adaptive Dual Launcher Icons (Square & Round)
- **Round Launcher Icon**: Dedicated circular 3D calculator icon with the glowing blue outer disc.
- **Square Launcher Icon**: Original square 3D calculator icon for square/squircle phone launchers.
- Both formats generated across all density buckets (`mdpi`, `hdpi`, `xhdpi`, `xxhdpi`, `xxxhdpi`).

---

### 5. ⚡ Seamless AMOLED Launch Experience
- Android 12+ Splash Screen (`android:windowSplashScreenBackground`) and base `windowBackground` are set to pure `#000000` pitch black, eliminating white launch flashes.

---

### 6. 📜 Calculation History
- **Persistent Storage**: Built using **Room Database** (`calculation_history` table) with reactive `Flow`.
- **Interactive Recall**: Tap on any expression to reload it, or tap on any result to insert it into the active calculation.

---

## 🚀 How to Open & Run in Android Studio

1. Open **Android Studio**.
2. Select **Open** and choose the project directory:
   ```
   zealous-fermi/
   ```
3. Connect an Android phone or start an emulator and click the green **Run (▶)** button.
4. The compiled APK is available at:
   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```
