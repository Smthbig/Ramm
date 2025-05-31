# 🏥 Ramm Healthcare – Smart Clinic Management App

_A modern, offline-first clinic management app for doctors, built with Material You (Material 3) design._

---

## 🚀 Features

### 🧑‍⚕️ Patient Visit Management
- Add/update patient details: name, age (Y/M/D), visit date.
- Track payment mode: Cash / UPI / Online.
- Auto-formatted visit history.
- Long press any visit to **Edit/Delete** patient data.

### 💊 Medicine Billing System
- Dynamic medicine picker with search.
- Add multiple medicines:
  - Quantity
  - Price per unit
  - Discount (%)
  - Auto-calculated final amount
- Live medicine cart preview and deletion.
- Payment mode selection for medicine billing.
- Grand total auto-calculation.

### 📚 Visit History with Filters
- Beautiful **CardView** display.
- Filter by:
  - 📅 Date range (using pickers)
  - 🔍 Keyword (name, phone, age, amount)
- Full patient + bill summary in one view.

### 🖋 Editable Record Dialogs
- Long press visit entry to edit:
  - Name, Age, Date, Payment
  - Medicines, Discounts, Amounts
- Fully styled, scrollable Material 3 dialog.
- Autofill selected data for quick edits.

### 🧾 📤 Export to PDF (New!)
- Generate and export visit and medicine records as **PDF** for sharing or printing.
- Clean, print-friendly format with all necessary data.
- Fully offline – no cloud dependency.

### 🎨 Material You UI (Material 3)
- Modern Material components (Buttons, Dialogs, TextInput).
- Light/Dark mode support.
- Responsive across all Android screen sizes.
- Rounded, clean design everywhere.

### ⚙ Intelligent UX
- Auto-update amounts on quantity/discount change.
- Smart search filtering.
- Live cart calculations.
- EditText validations and user-friendly errors.

### 🔐 Privacy & Offline-first
- 100% offline via **SQLite**
- No internet required – ideal for rural clinics.
- Zero data sharing or cloud syncing by default.

---

## 📸 Screenshots

| Home | Add Patient | Medicine Picker |
|------|-------------|-----------------|
| ![](ss/ss1.jpeg) | ![](ss/ss2.jpeg) | ![](ss/ss3.jpeg) |

| Billing Summary | Visit History | Filters |
|-----------------|----------------|---------|
| ![](ss/ss4.jpeg) | ![](ss/ss5.jpeg) | ![](ss/ss6.jpeg) |

| Editable Dialog | Dark Mode | Export to PDF |
|------------------|------------|----------------|
| ![](ss/ss7.jpeg) | ![](ss/ss8.jpeg) | ![](ss/ss9.jpeg) |

---

## 🧩 Tech Stack

| Technology         | Purpose                        |
|--------------------|--------------------------------|
| Java               | Core app logic                 |
| SQLite             | Local data storage             |
| SharedPreferences  | Config values & flags          |
| Material 3 (MDC)   | UI components & themes         |
| RecyclerView       | Dynamic lists                  |
| BottomSheetDialog  | Medicine selector              |
| Dialogs & Alerts   | Edits, confirmations           |
| CardView           | Visit entry display            |
| Lottie (Optional)  | Animations                     |

---

## 🔮 Upcoming Features

- 🗃 Export to **Excel**  
- ☁️ Cloud / Local DB backup & restore  
- 📊 Visit pattern analytics dashboard  
- 👥 Multi-user login (doctor/staff mode)  
- 🔄 Google Drive / Firebase sync  
- 📱 Tablet optimization + RTL support  

---

## 🏗 How to Build

1. Clone or download this repository.
2. Open in **Android Studio** or any Java-supported IDE.
3. Ensure **API 23+** (Android 6.0+) is used.
4. Build → Install APK → You're ready to go!

---

## 👨‍🔬 Developer Info

**👨‍💻 Author**: *Ramm Healthcare Dev Team*  
**📌 Purpose**: Provide secure, smart, and simple clinic software for Indian doctors and general practitioners.

> **Note:** Your clinic data stays on your device – no internet or cloud dependency. Maximum privacy and offline capability.

---