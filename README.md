# CC Cuan — Aplikasi Tracker Kartu Kredit Android

**CC Cuan** adalah aplikasi Android offline (Kotlin + Jetpack Compose + SQLite) untuk memantau kartu kredit dengan rapi.

## Fitur Utama
- **Manajemen Multi-Kartu Kredit**: Catat nama kartu, bank, 4 digit terakhir, **limit kartu kredit**, sisa limit otomatis, dan persentase pemakaian limit.
- **Siklus Tagihan & Jatuh Tempo**: Catat **tanggal cetak tagihan** dan **tanggal jatuh tempo** (1–31) dengan hitung mundur otomatis (`H-X`).
- **Pencatatan Transaksi**: Catat nama transaksi, tanggal transaksi (DatePicker), kategori, catatan, dan jumlah transaksi.
- **Pencatatan Pembayaran / Pelunasan**: Catat pembayaran sebagian atau tombol cepat **Lunas Penuh** yang otomatis mengurangi tagihan & memulihkan sisa limit.
- **Ekspor Laporan PDF**: Buat laporan A4 multi-halaman (ringkasan kartu, tabel transaksi, tabel pembayaran) yang tersimpan di `Downloads/CC_Cuan` dan bisa langsung dibagikan/dibuka.
- **Alarm Pengingat H-7 Jatuh Tempo**: Penjadwalan otomatis via `AlarmManager` 7 hari sebelum jatuh tempo pukul 08:00 (lengkap dengan suara alarm, getar, auto-reschedule setelah HP restart, dan tombol **Tes Alarm**).

## Cara Build APK Otomatis via GitHub Actions
1. Buat repository baru di GitHub (misal `cc-cuan`).
2. Ekstrak seluruh isi file ZIP ini langsung ke root repository (pastikan folder `.github/`, `app/`, `build.gradle.kts`, dan `settings.gradle.kts` berada di tingkat paling atas).
3. Push ke branch `main` (atau `master`).
4. Buka tab **Actions** di repository GitHub kamu → pilih workflow **Build CC Cuan Android APK**.
5. Setelah status hijau (selesai ±2–4 menit), scroll ke bagian **Artifacts** dan unduh **CC-Cuan-APK** (`CC-Cuan-v1.0.0.apk`).
