# Gacha-Design-Studio-App
Browser game semi-online khusus android untuk Gacha Design Studio dibuat oleh Lunime Fanmade (Penggemar Lunime)

![Banner](archanaberry/Banner.png)

![Archana Berry Developer Game](archanaberry/archanaberry.png)

# Ikon Aplikasi
![Gacha Desing Studio](archanaberry/icon.png)

# Fitur Gacha Design Studio webview
* Library write/read file (no download/upload file method)
* Download automation resource from (https://github.com/archanaberry/Gacha-Design-Studio)
* Fullscreen for phone tablet and PC
* Migration SWF (Adobe Flash Player) to HTML5 (Custom Browser)
* Server-Client protocol for sharing studiospaces enjoying friend.
* Use Vanila JS, Pure CSS, HTML5 for natively resource Gacha Game.

Game webview ini masih menimbulkan banyak bugs...
* Sudah oke ✅
* Belum difix ❌

| Fitur                     | Deskripsi                                                                                                        | Status |
|---------------------------|------------------------------------------------------------------------------------------------------------------|--------|
| Tampilan WebView          | Memuat game Gacha Design Studio menggunakan WebView di dalam aplikasi Android.                                  | ✅ |
| Main Activity dan Webview terpisah              | GachaStudioMain (berguna sebagai loader sekaligus managing update resource) dengan GachaStudio (webview) dibuat terpisah agar lebih mudah dimaintain/mengurus jika terjadi suatu bug dan memungkinkan fullscreen activity.           | ✅ |
| Pengecekan Update sumber daya melalui manifest         | Memuat resource game Gacha Design Studio menggunakan pembanding file manifest lokal dengan di rawgithub (membuat seolah olah menjadi API) untuk melakukan update baik aplikasi nya atau resource nya.                                | ❌ |
| Alert Custom              | Menampilkan pesan alert khusus dengan judul besar dan pesan kecil, serta opsi untuk menyalin teks ke papan klip. | ✅ |
| Navigasi Mundur           | Memungkinkan pengguna untuk mundur ke halaman sebelumnya saat menekan tombol kembali di perangkat Android.       | ✅ |
| Mundurkan halaman bingkai (frame content) pakai tombol kembali        | Memungkinkan pengguna untuk mundur frame atau keluarkan window di game GDS pakai tombol back atau esc       | ❌ |
| Mengunduh resource game    | Mengunduh sumber daya dengan indikator di dialog box dan juga di notifikasi.             | ❌ |
| Keluar dengan konfirmasi dua kali klik      | Ketuk dua kali agar aplikasi dapat dikonfirmasi agar aman untuk keluar dari gane agar tidak mereset sesi game.         | ✅ |
| FullScreen                 | Merespons layar penuh untuk semua device baik ponsel atau tablet atau komputer PC.                   | ❌ |
| Berbagi server studio atau Menerima klien studio dari saya ke teman dan ke teman lainnya via wlan0 (hotspot/wifi rumah (server kecil kecilan))               | Merespons server dan klien untuk melalukan berbagi studiospaces kepada teman melalui wlan0 dengan IPv4 (IP versi 4) menggunakan websocket untuk menjamin real time tiap pergerakan.              | ❌ |
| Responsif                 | Merespons perubahan orientasi dan ukuran layar perangkat Android untuk tampilan yang optimal.                   | ✅ |
| Indikator pengunduhan, pengekstrakan sumber daya         | [BUG!!!] Untuk mengetahui progres pemasangan sumber daya game GDS agar tahu berapa lama dan berapa persen.       | ❌ |
| Terjemahkan secara dinamis dengan bahasa sistem (Menggunakan library ( ![TransVar - Translator Variable](https://github.com/archanaberry/transvar) )                | Merespons perubahan bahasa menyesuaikan dengan bahasa lokalisasi sistem yang sedang digunakan berlangsung secara menyeluruh baik MainActivity dan Webview beserta html5 nya.              | ❌ |
| Archana Berry dan Lunime Logger Report (GachaStudioLogger.kt)                | Merespons melaporkan menjalankan game sekaligus mendebug dan melaporkan kode kesalahan dengan mudah ke pengembang ku, berfungsi baik kode inspeksi html5 web ataupun log kode aplikasi tiap berjalan (MainActivity), Peran tipe log dari Archana Berry sebagai Analisis dan Lunime sebagai Error Kerusakan !.               | ✅ |

## Bahasa pemrograman yang dipakai
* Tolong diperbaiki bug dari kotlin nya!
<img src="archanaberry/Kotlin.png" alt="Menggunakan kotlin sebagai program utama" width="192" height="108">

## Informasi 
Kami akan segera membuat ulang project ini dengan rancangan yang benar.

## Terimakasih untuk:
![lunime credits arts (open source but don't forget to remember her :>)](archanaberry/lunime.svg)
