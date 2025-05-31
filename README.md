# Ramm Healthcare – Clinic Management App

Ramm Healthcare is a modern and efficient Android application designed to simplify clinic management and patient billing for small- to mid-sized healthcare practices. This app streamlines patient data entry, medicine billing, payment tracking, and visit history filtering — all tailored with an intuitive Material 3 design.

## 📱 Features

### 🧾 Patient Registration & Visit Entry
- Add new patient details with full name, age (in years/months/days), and phone number.
- Specify visit payment and record visit date.
- Optional Lottie animation enhances UI experience.

### 💊 Medicine Billing
- Select medicines via a bottom sheet with a search-enabled RecyclerView.
- Set quantity, discount %, and dynamically calculate total.
- Items added to cart with live preview and deletion support.
- Payment options: Cash / Online (QR Code auto-generated).

### 🔍 Visit History & Search
- View all patient visits in a clean, scrollable ListView.
- Filter history by:
  - Date Range (via DatePicker)
  - Keyword-based search (Name, Age, Phone, or Amount Range)
- Edit or delete past records via long-press dialogs.

### 📦 Medicine Picker
- Beautiful MaterialCardView styled medicine list.
- Full Material You support with medium/high contrast themes.
- Auto-scrolls to top when searching.
- Supports color schemes for light/dark mode.

### 🧠 Intelligent UI Behavior
- Automatic total refresh when discount % changes.
- Dynamically updates search hints based on selected filter type.

### 🔐 Privacy & Permissions
- Local data storage using SQLite.
- No internet access required.
- No sensitive permissions requested — ensures complete patient privacy.

## 📂 Technologies Used
- Java (Android SDK)
- SQLite (local database)
- Material 3 (M3) Design Components
- Lottie Animation
- BottomSheetBehavior
- SharedPreferences for lightweight config

## 📸 Screenshots
_Coming soon_

## 🚀 Getting Started
To run this project:
1. Clone the repository or copy the source into Android Studio.
2. Ensure your Android device/emulator is set to API 23+.
3. Build and run the app.

## 🧩 TODO (Upcoming Enhancements)
- Export visit history to PDF/Excel.
- Backup/restore local database.
- Advanced analytics dashboard (optional).
- Login system for doctors/staff.
- Cloud sync integration (optional).

## 🏥 App Info
**Developer**: Ramm Healthcare  
**Purpose**: Lightweight, offline-first clinic management tool tailored for Indian clinics, especially useful for general practitioners and small healthcare setups.

---

**Note**: This application is built with a privacy-first approach. All data is stored locally, and no patient data leaves the device.