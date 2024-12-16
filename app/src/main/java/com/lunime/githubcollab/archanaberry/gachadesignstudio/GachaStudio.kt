package com.lunime.githubcollab.archanaberry.gachadesignstudio

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.os.Process
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File

import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.os.Handler;
import android.os.Looper;

class GachaStudio : AppCompatActivity() {

    //private lateinit var timeout: TimerTimeout  // Deklarasi objek timeout
    private lateinit var webView: WebView
    private val appFolder = "/Lunime"
    private var backPressedTime: Long = 0
    private val pressBackInterval: Long = 2000 // Interval 2 detik untuk tombol "Back"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.gachastudio_main) // Pastikan layout sesuai

        // Inisialisasi WebView
        webView = findViewById(R.id.web)
        loadWebViewContent()

        // Start fullscreen mode and hide system UI
        //startFullscreenMode()

        // Inisialisasi timeout
        //timeout = TimerTimeout()  // Inisialisasi objek timeout
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setUpWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            allowContentAccess = true
            allowFileAccess = true
            mediaPlaybackRequiresUserGesture = false
            GachaStudioLogger.log("""
            Menggunakan setelan:
            JavaScript Eksekusi: diaktifkan
            Izinkan akses konten: diaktifkan
            Izinkan akses berkas: diaktifkan
            Membutuhkan gerakan gestur pengguna media pemutaran: dinonaktifkan // [berguna untuk BGM dan SFX permainan]
            """)
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                GachaStudioLogger.log("Memuat tampilan web klien...")
                return false // Biarkan WebView memuat URL
            }
        }
    }

    private fun loadWebViewContent() {
        GachaStudioLogger.log("Menyetel tampilan konten web...")
        setUpWebView()

        val baseDir = detectDynamicDirectory()
        val htmlFilePath = "$baseDir$appFolder/mainmenu.html"
        val file = File(htmlFilePath)

        if (file.exists()) {
            webView.loadUrl("file:///$htmlFilePath")
        } else {
            val errorMessage = "File tidak ditemukan di $htmlFilePath"
            GachaStudioLogger.log("File $htmlFilePath tidak ditemukan!", iserror = true)
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
            Log.e("GachaStudio", errorMessage)
        }
    }

    /**
     * Mengatur fullscreen untuk GachaStudio
    **/
     
    /*
    private fun startFullscreenMode() {
        val decor = window.decorView
        decor.setOnSystemUiVisibilityChangeListener { visibility ->
            if ((visibility and View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                timeout.startTimeout(5000) {
                    GachaStudioLogger.log("Layar penuh dimulai...")
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                        // API 30 ke atas: Gunakan WindowInsetsController
                        window.setDecorFitsSystemWindows(false)
                        val insetsController = window.insetsController
                        insetsController?.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                        insetsController?.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE)
                    } else {
                        // API 29 ke bawah: Gunakan setSystemUiVisibility
                        GachaStudioLogger.log("Layar penuh (LAWAS) diaktifkan... ")
                        decor.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
                    }
                }
            }
        }
    }
    */

    private fun detectDynamicDirectory(): String {
        val userId = Process.myUserHandle().hashCode()
        GachaStudioLogger.log("Menggunakan penyimpanan di pengguna $userId")
        return if (userId == 0) "/storage/emulated/0" else "/storage/emulated/$userId"
    }

    override fun onLowMemory() {
        super.onLowMemory()
        GachaStudioLogger.log("Memori rendah dan penuh!, dadah aku menghilang...", iserror = true)
        Log.e("GachaStudio", "Memori rendah, log disimpan.")
    }

    override fun onBackPressed() {
        val currentTime = SystemClock.elapsedRealtime()
        if (currentTime - backPressedTime > pressBackInterval) {
            backPressedTime = currentTime
            GachaStudioLogger.log("Ga jadi keluar, karena aku tahan untuk konfirmasi nya :3")
            Toast.makeText(this, "Tekan dua kali untuk keluar dari game!", Toast.LENGTH_SHORT).show()
        } else {
            super.onBackPressed()
            Log.i("GachaStudio", "Aplikasi keluar menggunakan tombol Back.")
            GachaStudioLogger.log("Bye bye, Keluar menggunakan tombol kembali/esc", iserror = true)
        }
    }
}

/*
// TimerTimeout class to manage timeouts for UI visibility
private class TimerTimeout {
    private val handler = Handler(Looper.getMainLooper()) // Handler with main looper
    private var timeoutRunnable: Runnable? = null

    fun startTimeout(delayMillis: Long, action: () -> Unit) {
        stopTimeout() // Ensure no other timeout is running
        timeoutRunnable = Runnable {
            action()
        }
        timeoutRunnable?.let {
            handler.postDelayed(it, delayMillis)
        }
    }

    fun stopTimeout() {
        timeoutRunnable?.let { handler.removeCallbacks(it) }
    }
}
*/