<div align="center">

#  Ember

### A quiet place to write, reflect, and understand yourself.

<br>


<br><br>

**Offline-first journaling · Mood tracking · Personal reflections**

<br>

[![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white)](https://developer.android.com/compose)
[![Room](https://img.shields.io/badge/Room-Local%20Storage-FF6F00?style=flat-square)](https://developer.android.com/training/data-storage/room)

</div>

---

## About

Ember is a simple Android journal built around one idea:

> **Journaling shouldn't feel like another task to manage.**

Write about your day, record how you're feeling, and look back at your reflections over time.

Everything works **offline**, so your journal stays on your device.

---

## ✦ What you can do

| | Feature | |
|---|---|---|
| 📝 | **Journal** | Write, edit and revisit your daily thoughts |
| 🌤️ | **Mood** | Record how you're feeling alongside your entries |
| 🔥 | **Streaks** | Build a consistent journaling habit |
| 📖 | **Reflections** | Look back at what you've written |
| 📊 | **Insights** | See patterns in your journaling activity |
| 🔒 | **Offline** | Your core journal data stays on your device |

---

## 📱 Preview

<div align="center">

<img src="screenshots/today.png" width="220">
<img src="screenshots/journal.png" width="220">
<img src="screenshots/history.png" width="220">

</div>

> Screenshots will be added as the UI evolves.

---

## 🛠 Built with

**Kotlin**  
**Jetpack Compose**  
**Room**  
**Coroutines**  
**StateFlow**  
**Material Design**

The app follows a simple flow:

```text
Compose UI
    ↓
ViewModel
    ↓
Repository
    ↓
Room
    ↓
Local device storage
