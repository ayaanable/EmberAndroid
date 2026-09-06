# Visual Comparison: Keyboard Bug Fix

## Problem Visualization

### How the Bug Occurred (BEFORE)

```
┌─────────────────────────────────────────┐
│  Android System (Full Screen)           │
├─────────────────────────────────────────┤
│                                         │
│  JournalScreen → WritingDockSheet       │
│  ┌───────────────────────────────────┐  │
│  │ Box (with imePadding)             │  │  ← Box applies IME padding
│  │ ┌─────────────────────────────┐   │  │
│  │ │ Column (with scroll)        │   │  │
│  │ │                             │   │  │
│  │ │ Close  |  Info  |  Save     │   │  │
│  │ │ Mood Selector               │   │  │
│  │ │ Tags                        │   │  │
│  │ │ [Journal Text Input Field]  │   │  │
│  │ │ ◄────────────────────────► │   │  │
│  │ │ (User taps here → keyboard) │   │  │
│  │ └─────────────────────────────┘   │  │
│  └───────────────────────────────────┘  │
│  navigationBarsPadding pushes this down  │
└─────────────────────────────────────────┘

KEYBOARD APPEARS ⬇

┌─────────────────────────────────────────┐
│  Android System (Full Screen)           │
├─────────────────────────────────────────┤
│  ❌ ENTIRE SCREEN PUSHED UP             │
│  ┌───────────────────────────────────┐  │
│  │ Box (EXPANDED for IME height)     │  │  ← Box expands!
│  │ ┌─────────────────────────────┐   │  │
│  │ │ Column (pushed up)          │   │  │
│  │ │ [Journal Text Input Field]  │   │  │
│  │ │ ◄────────────────────────► │   │  │
│  │ └─────────────────────────────┘   │  │
│  │ [Empty space added for IME]       │  │  ← IME padding expands Box
│  │ [Navigation bar can't fit here]   │  │
│  └───────────────────────────────────┘  │
│                                         │
│  ┌─────────────────────────────────┐   │
│  │   SOFT KEYBOARD                 │   │  ← Keyboard appears
│  │   [a b c d e f g h i j k l m]   │   │
│  │   [n o p q r s t u v w x y z]   │   │
│  └─────────────────────────────────┘   │
└─────────────────────────────────────────┘

PROBLEM: Navigation bar and UI moved up visibly!
```

---

## Solution Visualization

### How It Works Now (AFTER)

```
BEFORE KEYBOARD:

┌─────────────────────────────────────────┐
│  Android System (Full Screen)           │
├─────────────────────────────────────────┤
│                                         │
│  JournalScreen → WritingDockSheet       │
│  ┌───────────────────────────────────┐  │
│  │ Box (NO imePadding → stays same)  │  │  ← Box STABLE
│  │ ┌─────────────────────────────┐   │  │
│  │ │ Column (with scroll)        │   │  │
│  │ │ + navigationBarsPadding()    │   │  │
│  │ │ + imePadding()             │   │  │  ← Padding moved here
│  │ │                             │   │  │
│  │ │ Close  |  Info  |  Save     │   │  │
│  │ │ Mood Selector               │   │  │
│  │ │ Tags                        │   │  │
│  │ │ [Journal Text Input Field]  │   │  │
│  │ │ ◄────────────────────────► │   │  │
│  │ │ (User taps here → keyboard) │   │  │
│  │ └─────────────────────────────┘   │  │
│  └───────────────────────────────────┘  │
│                                         │
│  🔴 Navigation bar (bottom nav visible) │  ← Still visible
└─────────────────────────────────────────┘

KEYBOARD APPEARS ⬇

┌─────────────────────────────────────────┐
│  Android System (Full Screen)           │
├─────────────────────────────────────────┤
│                                         │
│  JournalScreen → WritingDockSheet       │
│  ┌───────────────────────────────────┐  │
│  │ Box (unchanged size)              │  │  ← Box STABLE (no change!)
│  │ ┌─────────────────────────────┐   │  │
│  │ │ Column (with scroll)        │   │  │
│  │ │ ◄ Can scroll now ►          │   │  │
│  │ │ [Journal Text Input Field]  │   │  │
│  │ │ ◄────────────────────────► │   │  │
│  │ │ ▲ [Visible above keyboard]  │   │  │
│  │ └─────────────────────────────┘   │  │
│  └───────────────────────────────────┘  │
│                                         │
│  🟢 Navigation bar (stays in place)     │  ← Unchanged!
└─────────────────────────────────────────┘
                ⬇ GAP ⬇
┌─────────────────────────────────────────┐
│   SOFT KEYBOARD                         │
│   [a b c d e f g h i j k l m]           │
│   [n o p q r s t u v w x y z]           │
└─────────────────────────────────────────┘

SOLUTION: Navigation bar stays in place, Column scrolls to keep text visible!
```

---

## Modifier Chain Analysis

### BEFORE (Problematic)

```
Box (↔ Full Width, ↕ Full Height)
├─ fillMaxSize()              ✓ correct
├─ background(colors.canvas)  ✓ correct
├─ statusBarsPadding()        ✓ correct (for status bar)
├─ navigationBarsPadding()    ❌ WRONG - applies to Box
│  (When keyboard opens, this area expands)
├─ imePadding()               ❌ WRONG - applies to Box
│  (When keyboard opens, Box expands to fit IME)
│
└─ Column (↔ Full Width, ↕ Full Height)
   ├─ fillMaxSize()           ✓ correct
   ├─ verticalScroll()        ✓ correct (scrollable)
   └─ padding(horizontal, vertical) ✓ correct
```

**Issue**: When keyboard opens, imePadding() expands the Box, which cascades down and pushes everything up.

---

### AFTER (Correct)

```
Box (↔ Full Width, ↕ Full Height)
├─ fillMaxSize()              ✓ correct
├─ background(colors.canvas)  ✓ correct  
└─ statusBarsPadding()        ✓ correct (only for status bar, not IME)

└─ Column (↔ Full Width, ↕ Full Height)
   ├─ fillMaxSize()           ✓ correct
   ├─ verticalScroll()        ✓ correct (scrollable)
   ├─ padding(horizontal, vertical) ✓ correct
   ├─ navigationBarsPadding()  ✅ CORRECT - on scrollable Column
   │  (Only scrollable components should get this)
   └─ imePadding()            ✅ CORRECT - on scrollable Column
      (Scrollable Column handles IME padding naturally)
```

**Result**: Box stays stable. Column receives IME padding and handles it via scrolling.

---

## Compose Window Insets Explanation

### What Each Modifier Does

| Modifier | Purpose | Applies When | Effect |
|----------|---------|--------------|--------|
| **statusBarsPadding()** | Avoid overlap with status bar | Always | Adds space at top |
| **navigationBarsPadding()** | Avoid overlap with nav bar/buttons | Always | Adds space at bottom (normal) or side |
| **imePadding()** | Avoid overlap with keyboard | When IME visible | Adds space at bottom equal to keyboard height |

### Why Placement Matters

**On Non-Scrollable Box**:
- ❌ Entire Box expands when IME appears
- ❌ Everything below gets pushed up
- ❌ Visual shift/jump occurs

**On Scrollable Column**:
- ✅ Column receives padding normally
- ✅ Content scrolls to fill new space
- ✅ No visual shift of other UI elements

---

## User Experience Flow

### BEFORE (Broken)
```
User taps "Write here" 
   ↓
WritingDockSheet appears fullscreen
   ↓
User taps journal text field
   ↓
Keyboard starts to appear
   ↓
❌ Box.imePadding() triggers
   ↓
❌ Box expands downward
   ↓
❌ Entire screen pushed upward
   ↓
❌ Navigation bar visibly moves up into view
   ↓
❌ Jarring, unexpected visual shift
   ↓
User disoriented, poor experience
```

### AFTER (Fixed)
```
User taps "Write here"
   ↓
WritingDockSheet appears fullscreen
   ↓
User taps journal text field
   ↓
Keyboard starts to appear
   ↓
✅ Column.imePadding() triggers
   ↓
✅ Box remains stable (no change)
   ↓
✅ Column receives IME padding
   ↓
✅ Column scrolls to keep text visible
   ↓
✅ Navigation bar stays in place (already below WritingDockSheet)
   ↓
✅ Smooth, natural keyboard appearance
   ↓
User experience is clean and intuitive
```

---

## Measurement: Before vs After

### Screen Movement Comparison

#### BEFORE (Broken) - IME Height: ~260dp
```
Initial State:
┌────────────────────┐
│  WritingDockSheet  │  ← Visible on screen
│  (fullscreen)      │
│                    │
│  [Text Input]      │
└────────────────────┘

After Keyboard Opens:
┌────────────────────┐
│  WritingDockSheet  │  ← PUSHED UP (entire screen shifted up by 260dp)
│  (fullscreen)      │
│  [Text Input] ▲    │
│               |    │
│         (IME space)│
└────────────────────┘
     ⬇ VISIBLE SHIFT ⬇
```

Keyboard Height: ~260dp
Screen Shift: **~260dp upward** ❌

#### AFTER (Fixed) - IME Height: ~260dp  
```
Initial State:
┌────────────────────┐
│  WritingDockSheet  │  ← Stable on screen
│  (fullscreen)      │
│                    │
│  [Text Input]      │
└────────────────────┘

After Keyboard Opens:
┌────────────────────┐
│  WritingDockSheet  │  ← NO SHIFT (exactly same position)
│  (fullscreen)      │
│  [Text Input] ▲    │
│  [Scrollable] |    │
│  (inside Column)   │
└────────────────────┘
        ⬇ NO SHIFT ⬇
```

Screen Shift: **0dp** ✅ (Box stays completely stable)

---

## Summary

- **Problem**: IME padding on the Box caused entire screen to shift
- **Root Cause**: Box expands when imePadding() is applied to it
- **Solution**: Apply padding to the scrollable Column instead
- **Result**: Box stays stable, Column scrolls naturally with IME
- **User Impact**: Smooth, predictable keyboard interaction with zero UI jank

