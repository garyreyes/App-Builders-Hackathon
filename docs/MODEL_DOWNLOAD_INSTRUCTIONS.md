# Model Download and Device Installation Guide

This document explains where to find the trained Waray language model file, how to host it, and how to transfer it onto an Android device for offline inference.

## 1. Model Details

* **File Name:** `waray-chat-v3-q8_0.gguf`
* **Model Architecture:** Sailor2 1B Chat with Waray LoRA adaptation
* **Quantization:** Q8_0
* **Exact Size:** 1,056,199,072 bytes (approximately 1.05 GB)
* **SHA256 Hash:** `97E54275B4814FDE219D224D2FFE1CDEAFC04339564036DBCFF31E42F371D6B0`

## 2. Where the File is Stored on this Computer

The model file is already saved locally on this machine at:

```text
C:\Users\Denienz\Documents\Personal Projects\AppBuilders\App-Builders-Hackathon\models\waray-chat-v3-q8_0.gguf
```

Relative repository path:
```text
models/waray-chat-v3-q8_0.gguf
```

## 3. Where to Upload the Model File

Because the model file exceeds GitHub standard commit limits, upload it to one of these public download locations:

1. **GitHub Releases (Recommended for Judges)**
   * Go to your repository Releases page:
     `https://github.com/garyreyes/Buh.ai/releases`
   * Edit or create a release (example: tag `android-judge-demo-v1`).
   * Attach `waray-chat-v3-q8_0.gguf` alongside `app-debug.apk`.

2. **Hugging Face Model Hub**
   * Create a free model repository on Hugging Face (example: `your-username/buhai-waray-sailor2-gguf`).
   * Upload `waray-chat-v3-q8_0.gguf`.
   * Fast, reliable direct download link for judges and developers.

3. **Google Drive or Cloud Storage**
   * Upload the file and set sharing to anyone with the link.

## 4. How to Install and Run on Android (For Judges and Testers)

Connect your Android phone or start the Android emulator, then run these steps:

### Step 1. Install the application

From the folder where `app-debug.apk` is saved:

```powershell
adb install -r app-debug.apk
```

### Step 2. Create the destination directory on the phone

```powershell
adb shell mkdir -p /sdcard/Android/data/ph.appbuilders.offlinehealth/files
```

### Step 3. Push the model file to the phone

```powershell
adb push waray-chat-v3-q8_0.gguf /sdcard/Android/data/ph.appbuilders.offlinehealth/files/
```

### Step 4. Launch the application

```powershell
adb shell am start -n ph.appbuilders.offlinehealth/.MainActivity
```

### Step 5. Test fully offline

1. Turn on Airplane Mode on the phone or emulator.
2. Select **Winaray** on the opening screen.
3. Type a health question in Waray (for example: *"Tulo na ka adlaw nga nagkakalibang an akon anak"*).
4. The application reads the model directly from local phone storage and generates the Waray reply through llama.cpp with zero internet.

## 5. Fallback Behavior

If the model file is not transferred to the device, the app safely defaults to basic local mode:
* Danger sign detection and red emergency banners still trigger immediately.
* Reviewed first aid cards remain accessible.
