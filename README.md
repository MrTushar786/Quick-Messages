# Custom WhatsApp Quick Message Widget for Nothing Phone (1)

A native Android application and interactive home screen widget designed specifically for the **Nothing Phone (1)** running Nothing OS. It lets you send frequently used WhatsApp messages (such as *"Nikal raha"*, *"Poch Gaya"*) to your designated recipient with a single tap from your home screen.

---

## Key Highlights

- **WhatsApp Auto-Send (Accessibility Service)**: Automatically taps the Send button inside WhatsApp within milliseconds using an integrated Android Accessibility Service, providing true zero-tap hands-free delivery.
- **Direct SMS Mode**: Sends text messages silently in the background via Android's `SmsManager` with 0 screens opening.
- **1-Click Send Recent Media**: Automatically queries the latest photo or video from your gallery and dispatches it directly.
- **Google Play Fallback**: If WhatsApp is not installed, it gracefully prompts to download it from the Google Play Store.

---

## Default Configuration

- **Default Recipient**:
  - Name: Default
  - Country code: `+91`
  - Phone number: `8864846795`
- **Default Message Presets**:
  1. `Leaving`: *"Nikal raha"* (Emoji: 🚗)
  2. `Reached`: *"Poch Gaya"* (Emoji: 📍)
  3. `On My Way`: *"Raste me hu"* (Emoji: ⏱️)
  4. `Call Later`: *"Abhi busy hu, thodi der me call karta hu"* (Emoji: 📞)

---

## Instructions: Build Debug APK

To build the APK on your workstation or in Android Studio:

### Via Terminal / Command Line:
```bash
gradle :app:assembleDebug
```
The output APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`

### Via Android Studio:
1. Open this project in **Android Studio Hedgehog or newer**.
2. Select **Build** > **Build Bundle(s) / APK(s)** > **Build APK(s)**.
3. Once completed, click **locate** to get `app-debug.apk`.

---

## Instructions: Install on Nothing Phone (1)

1. **Enable Developer Options on Nothing Phone (1)**:
   - Go to **Settings** > **About Phone**.
   - Tap **Build Number** 7 times until you see *"You are now a developer!"*.
2. **Enable USB Debugging**:
   - Go to **Settings** > **System** > **Developer options**.
   - Toggle on **USB debugging**.
3. **Connect and Install via ADB**:
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```
   *(Or transfer the `.apk` file directly to your Nothing Phone (1) via USB/Files and tap to install).*

---

## Instructions: Add the Widget to Your Home Screen

1. On your Nothing Phone (1) home screen, **press and hold** any empty space.
2. Tap **Widgets** in the Nothing OS pop-up menu.
3. Scroll down to find **Quick Message** (or **WhatsApp Quick Messages**).
4. Drag the **WhatsApp Quick Messages** widget onto your home screen.
5. Resize the widget horizontally or vertically:
   - **2x2 format**: Displays the top 4 quick messages as a clean tactile grid.
   - **4x2 format**: Expands across your Nothing OS grid layout.
6. Tap any message tile: WhatsApp will launch directly to your recipient's chat with the pre-filled text!

---

## Instructions: Configure Custom Messages & Recipients

1. Open the **Quick Message** companion app from your app drawer.
2. **Change Recipient**:
   - Tap the **Recipient Card** or the **EDIT** button at the top.
   - Enter contact name, country code (e.g. `+91`), and phone number (e.g. `8864846795`).
   - Tap **SAVE RECIPIENT**.
3. **Add New Message**:
   - Tap the **+** (Floating Action Button) at the bottom-right.
   - Enter **Title** (shown on widget button, e.g. *"Leaving"*).
   - Enter **Message Body** (prefilled into WhatsApp chat, e.g. *"Nikal raha"*).
   - Pick an optional emoji badge (e.g. 🚗).
   - Inspect the live **WhatsApp Deep Link Preview** to see the exact URL.
   - Tap **SAVE MESSAGE**.
4. **Reorder Messages**:
   - Use the **Up** / **Down** arrows on any message card to prioritize which messages appear on the 2x2 widget.
5. **Test Launch**:
   - Tap the **TEST LAUNCH** button on any card to test the WhatsApp intent directly without leaving the app.
