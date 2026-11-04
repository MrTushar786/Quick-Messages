# True Hands-Free Auto-Send Implementation Plan

A solution delivering true zero-tap automatic dispatch so users can tap once on their phone and the message is sent automatically without having to manually press Send.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> The following user preferences were confirmed during Phase 1:

- **Confirmed Decision 1 (Direct SMS Auto-Send)**: Provide a **Direct SMS** toggle that uses Android `SmsManager` to send the text message silently in the background with zero screens opening.
- **Confirmed Decision 2 (WhatsApp Auto-Clicker via Accessibility Service)**: Provide an integrated Android **Accessibility Service** (`WhatsAppAutoSendService`). When WhatsApp mode is used, the accessibility service detects the opened chat and automatically clicks WhatsApp's Send button within milliseconds, requiring zero manual taps.
- **Confirmed Decision 3 (Permission & Setup)**: Include a simple 1-tap shortcut inside the app to open Android's Accessibility Settings page directly so the user can easily enable the service once.

---

## 1. Overview & Core Concept

Due to WhatsApp's security restrictions, third-party apps cannot force WhatsApp to send a message via standard Intent URLs without user interaction. To fulfill the user's requirement ("hamko dabana na pade bas app par dabao or vaha chala jaae apne aap"), we implement two complementary modes:

1. **WhatsApp Auto-Clicker Mode (Accessibility Service)**:
   - When the user taps a message in the app or widget, the app flags `isAutoSendActive = true` and launches WhatsApp.
   - The lightweight `WhatsAppAutoSendService` intercepts WhatsApp's chat window, searches for the Send button node (`com.whatsapp:id/send` or ContentDescription `"Send"`), and executes `performAction(ACTION_CLICK)`.
   - The message sends automatically within ~200ms without the user touching the screen.
2. **Direct Background SMS Mode**:
   - Uses Android `SmsManager` to send the text message directly to the recipient's phone number (+91 8864846795) in the background.
   - Completely invisible, instant, and requires no app switching.

---

## 2. Technical Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                 User Action: Tap Message                    │
│                 (From App OR Home Screen Widget)            │
└──────────────────────────────┬──────────────────────────────┘
                               │
               ┌───────────────┴───────────────┐
               ▼                               ▼
    [Mode: Direct SMS]             [Mode: WhatsApp Auto-Send]
               │                               │
               ▼                               ▼
    SmsManager.sendTextMessage()   WhatsAppHelper.launchWhatsApp()
    - Silent background delivery   - Flags auto-send pending
    - 0 taps, instant              - Opens WhatsApp chat
                                               │
                                               ▼
                                   WhatsAppAutoSendService
                                   - Intercepts WhatsApp window
                                   - Finds Send button node
                                   - performAction(ACTION_CLICK)
                                   - Auto-sends hands-free!
```

---

## 3. Implementation Steps

1. **Accessibility Service Configuration**:
   - Create `res/xml/accessibility_service_config.xml` targeting `com.whatsapp` and `com.whatsapp.w4b`.
   - Implement `WhatsAppAutoSendService.kt` extending `AccessibilityService`.
   - Declare the service with `android.permission.BIND_ACCESSIBILITY_SERVICE` in `AndroidManifest.xml`.
2. **Direct SMS Integration**:
   - Add `android.permission.SEND_SMS` to `AndroidManifest.xml`.
   - Implement `SmsHelper.kt` to send multi-part/single text messages via `SmsManager`.
3. **Dispatch Mode Switcher in UI (`HomeScreen.kt`)**:
   - Add a sleek dispatch mode selector (WhatsApp Auto-Send vs Direct SMS).
   - If Accessibility Service is disabled, show a banner with an easy button: **"Enable Auto-Send in Settings"** that opens `Settings.ACTION_ACCESSIBILITY_SETTINGS`.
4. **Widget Integration**:
   - Widget respects the configured auto-send mode and triggers hands-free dispatch.
5. **Testing & Verification**:
   - Run unit tests and `compile_applet` to verify compilation and stability.
