# 🌐 tagihWIfi - Sistem Manajemen Penagihan Internet

Aplikasi Android berbasis Jetpack Compose untuk mengelola penagihan internet pelanggan dengan integrasi Google Sheets sebagai database (Tanpa Server Berbayar).

## 🚀 Fitur Utama

### 1. 👥 Multi-Level User
- **Petugas (User):** Melakukan input tagihan di lapangan.
- **Admin/Superadmin:** Mengelola user, data pelanggan, dan melihat rekapitulasi setoran.

### 2. ⚡ Input Tagihan Pintar
- **Smart Search:** Nama pelanggan akan hilang secara global jika sudah ditagih oleh petugas manapun dalam periode yang sama (Mencegah input ganda).
- **Auto-Fill Lokasi:** Lokasi pelanggan otomatis terisi saat nama dipilih.
- **Update Nominal:** Petugas dapat mengedit nominal tagihan jika terjadi kesalahan input.

### 3. 📊 Manajemen Admin
- **Bottom Navigation:** Akses cepat ke Riwayat, Data Pelanggan, dan Data Petugas.
- **Filter Periode:** Rekapitulasi per Bulan, Tahun, dan per Petugas.
- **Tutup Buku:** Admin dapat menghapus riwayat bulanan untuk memulai periode tagihan baru secara otomatis.

### 4. 🔔 Notifikasi & Keamanan
- **Notifikasi Mandiri:** Aplikasi mengecek data baru di latar belakang setiap 15 menit dan memberikan notifikasi meskipun aplikasi ditutup.
- **Biometric Login:** Mendukung login dengan sidik jari.
- **LockService:** Mencegah data tertimpa (overwrite) saat input bersamaan.

### 5. 🔄 Auto-Update GitHub
- Terintegrasi dengan GitHub Releases untuk pembaruan aplikasi otomatis tanpa melalui Play Store.

## 🛠️ Arsitektur Teknis
- **Bahasa:** Kotlin
- **UI:** Jetpack Compose (Material 3)
- **Database:** Google Sheets via Google Apps Script (GAS)
- **Networking:** Retrofit & OkHttp
- **Background Task:** WorkManager (untuk monitoring data)

## 📥 Cara Instalasi

1. **Backend Setup:**
   - Copy kode dari `google_apps_script.js` ke Editor Apps Script di Google Sheets Anda.
   - Deploy sebagai Web App dan setel akses ke "Anyone".
   - Salin URL Web App yang dihasilkan ke file `NetworkConfig.kt` di proyek Android.

2. **GitHub Update Setup:**
   - Letakkan file `version.json` di root repository Anda.
   - Unggah APK di bagian **Releases** GitHub.

3. **Build Aplikasi:**
   - Buka proyek di Android Studio.
   - Build APK (Build > Build APKs).
   - Install di perangkat Android (Minimal Android 7.0).

## 📄 File version.json
Struktur file untuk sistem update otomatis:
```json
{
  "versionCode": 2,
  "versionName": "1.0",
  "downloadUrl": "https://github.com/fahmy009/tagihWIfi/releases/latest",
  "releaseNotes": "Catatan rilis Anda..."
}
```

## 👨‍💻 Developer
Dibuat untuk kebutuhan manajemen penagihan internet mandiri yang efisien, aman, dan gratis selamanya.
