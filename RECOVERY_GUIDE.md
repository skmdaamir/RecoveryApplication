# 📸 Memory Card Recovery Guide (Quick Reference)

This guide walks you through recovering lost photos and files from your large memory card.

---

## 🎯 Option 1: When you get a Card Reader / USB Adapter

1. **Insert the memory card** into your USB card reader and plug it into your computer.
2. **Double-click [run-recoveryx.bat](file:///d:/Recovery/RecoveryApplication/run-recoveryx.bat)** to open the RecoveryX Pro GUI.
3. Select your card from the detected drives list.
4. Choose **Deep Scan**.
5. Select the destination folder on your **C:\** drive (e.g. `C:\RecoveredPhotos`).
   > ⚠️ **Warning:** Never select the memory card itself as the recovery destination!
6. Click **Recover**.

---

## 💾 Option 2: No Card Reader? Create an Image Dump on another device

If you have a laptop with a built-in SD slot, a camera connected via USB, or a friend's PC:

1. Use a free tool like **Win32 Disk Imager** or **DD** to create a single `.img` backup file of the card (e.g. `card_backup.img`).
2. Copy `card_backup.img` to your computer.
3. Run the following command in PowerShell:
   ```powershell
   d:\Recovery\RecoveryApplication\run-recoveryx.bat --image-file "C:\path\to\card_backup.img" --output "C:\RecoveredPhotos"
   ```
4. The scanner will carve and extract all images directly from the `.img` file!

---

## ⚡ Command-Line Quick Reference

### Deep Scan a Physical Drive (e.g. `E:\`):
```powershell
d:\Recovery\RecoveryApplication\run-recoveryx.bat --device E:\ --mode DEEP --output C:\RecoveredPhotos
```

### Deep Scan an Image File:
```powershell
d:\Recovery\RecoveryApplication\run-recoveryx.bat --image-file C:\card.img --output C:\RecoveredPhotos
```

---

## 🛡️ Supported Photo Formats

- **DSLR/Mirrorless RAW:** Canon (`.CR2`, `.CR3`), Nikon (`.NEF`), Sony (`.ARW`), Adobe (`.DNG`), Fujifilm (`.RAF`), Olympus (`.ORF`), Panasonic (`.RW2`)
- **Phone Photos:** Apple (`.HEIC`), Google (`.WebP`), standard (`.JPG`, `.PNG`, `.BMP`, `.TIFF`, `.GIF`)
- **Videos & Audio:** `.MP4`, `.MOV`, `.MP3`, `.WAV`
