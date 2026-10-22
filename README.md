# 💸 MoneyMatters — Android Expense Tracker

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Room DB](https://img.shields.io/badge/Database-Room-4285F4?style=for-the-badge&logo=google&logoColor=white)](https://developer.android.com/training/data-storage/room)
[![Architecture](https://img.shields.io/badge/Architecture-MVVM-FF6F00?style=for-the-badge)](#)

> A native Android financial tracking application built with Kotlin, Room Database, and MVVM architecture to track, manage, and visualize personal expenses seamlessly.

---

## 🏗️ Architectural Overview

This project follows Google's recommended **MVVM (Model-View-ViewModel)** architectural pattern with clean separation of concerns[cite: 3]:

```text
com.example.moneymatters
├── data/
│   ├── dao/           # ExpenseDao (Room Database queries)
│   ├── database/      # ExpenseDataBase (Room Database configuration)
│   └── model/         # ExpenseModel (Entity definition)
├── repository/        # ExpenseRepository (Data source abstraction)
└── ui/
    ├── adapter/       # ExpenseAdapter (RecyclerView list rendering)
    ├── view/          # MainActivity (UI Controller)
    └── viewModel/     # ExpenseViewModel (UI State Management)
```[cite: 3]

---

## 📂 Repository Layout

| Component | Description |
| :--- | :--- |
| 🗄️ **`data/`** | Data persistence layer powered by Room DB (`ExpenseDao`, `ExpenseDataBase`)[cite: 3] |
| 🔄 **`repository/`** | Repository pattern handling single source of truth for financial data[cite: 3] |
| 📱 **`ui/view/`** | UI Activity hosting the interactive dashboard (`MainActivity`)[cite: 3] |
| 🧠 **`ui/viewModel/`** | ViewModels handling reactive business logic and LiveData/Flow states[cite: 3] |
| 🎨 **`res/layout/`** | XML layouts for dashboard screens, dialogs (`dialog_add_expense`), and list items[cite: 3] |

---

## ✨ Features

- **Expense Tracking:** Record, modify, and monitor daily financial transactions[cite: 3].
- **Local Persistence:** Offline-first architecture powered by Android Room DB[cite: 3].
- **Interactive UI:** Dynamic list views built with `RecyclerView` (`ExpenseAdapter`)[cite: 3].
- **Custom Input Dialogs:** Quick-add financial entry interface (`dialog_add_expense.xml`)[cite: 3].

---

## ⚙️ Requirements & Tech Stack

### 🛠️ Core Technologies
* **Language:** Kotlin[cite: 3]
* **Build System:** Gradle (Kotlin DSL / `build.gradle.kts`)[cite: 3]
* **Database:** Room Persistence Library[cite: 3]
* **UI Framework:** Android Material Components & XML Layouts[cite: 3]

### 🖥️ Software
* **[Android Studio](https://developer.android.com/studio)** (Hedgehog | Iguana or newer recommended)
* **JDK:** 17 or higher
* **Android SDK:** Target API level configured via Gradle[cite: 3]

---

## 🚀 Quick Start Guide

1. **Clone the Repository:**
   ```bash
   git clone [https://github.com/kban123421412/MoneyMatters.git](https://github.com/kban123421412/MoneyMatters.git)
   cd MoneyMatters
