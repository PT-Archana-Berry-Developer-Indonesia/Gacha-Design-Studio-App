package com.lunime.githubcollab.archanaberry.gachadesignstudio

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.RemoteViews
import android.widget.Toast

import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

import org.apache.commons.io.FileUtils
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.NodeList

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class GachaStudioMain : Activity() {

    private val PERMISSION_REQUEST_CODE = 1
    private val DOWNLOAD_REQUEST_CODE = 2
    private val APP_FOLDER = "/"

    private var CONFIG_FILE_PATH: String? = null
    private var TEMP_DIR_PATH: String? = null
    private var ZIP_FILE_PATH: String? = null
    private var EXTRACTED_DIR_PATH: String? = null
    private var SOURCE_DIR_PATH: String? = null
    private var DEST_DIR_PATH: String? = null

    private var progressDialog: AlertDialog? = null
    private var progressBar: ProgressBar? = null

    private var remoteViews: RemoteViews? = null
    private var builder: NotificationCompat.Builder? = null
    private var notificationManager: NotificationManagerCompat? = null

    private var webView: WebView? = null
    private var backPressedTime: Long = 0
    private val PRESS_BACK_INTERVAL = 2000 // 2 detik

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inisialisasi path
        CONFIG_FILE_PATH = getExternalFilesDir(null)?.absolutePath
                ?.plus(APP_FOLDER)?.plus("/data/data.xml")
        TEMP_DIR_PATH = getExternalFilesDir(null)?.absolutePath
                ?.plus(APP_FOLDER)?.plus("/.temp")
        ZIP_FILE_PATH = TEMP_DIR_PATH?.plus("/DL.zip")
        EXTRACTED_DIR_PATH = TEMP_DIR_PATH?.plus("/")
        SOURCE_DIR_PATH = EXTRACTED_DIR_PATH?.plus("Gacha-Design-Studio-DL/")
        DEST_DIR_PATH = getExternalFilesDir(null)?.absolutePath
                ?.plus(APP_FOLDER)?.plus("/")

        val agreementAccepted = isAgreementAccepted()
        if (agreementAccepted) {
            loadWebViewContent()
        } else {
            showWelcomeDialog()
        }
    }

    private fun isAgreementAccepted(): Boolean {
        return try {
            val configFile = File(CONFIG_FILE_PATH)
            if (!configFile.exists()) {
                false
            } else {
                val dbFactory = DocumentBuilderFactory.newInstance()
                val dBuilder = dbFactory.newDocumentBuilder()
                val doc = dBuilder.parse(configFile)
                doc.documentElement.normalize()

                val nList = doc.getElementsByTagName("agreement")
                val eElement = nList.item(0) as Element
                val accepted = eElement.getElementsByTagName("accepted").item(0).textContent
                java.lang.Boolean.parseBoolean(accepted)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun setAgreementAccepted(accepted: Boolean) {
        try {
            val configFile = File(CONFIG_FILE_PATH)
            if (!configFile.exists()) {
                configFile.parentFile.mkdirs()
                configFile.createNewFile()
            }

            val dbFactory = DocumentBuilderFactory.newInstance()
            val dBuilder = dbFactory.newDocumentBuilder()
            val doc = dBuilder.newDocument()

            val rootElement = doc.createElement("config")
            doc.appendChild(rootElement)

            val agreement = doc.createElement("agreement")
            rootElement.appendChild(agreement)

            val acceptedElement = doc.createElement("accepted")
            acceptedElement.appendChild(doc.createTextNode(accepted.toString()))
            agreement.appendChild(acceptedElement)

            // Write the content into XML file
            val transformerFactory = TransformerFactory.newInstance()
            val transformer = transformerFactory.newTransformer()
            transformer.setOutputProperty(OutputKeys.INDENT, "yes")
            val source = DOMSource(doc)
            val result = StreamResult(FileOutputStream(configFile))
            transformer.transform(source, result)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun loadWebViewContent() {

        // Menjadikan activity fullscreen
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        setContentView(R.layout.gachastudio_main)

        webView = findViewById(R.id.web)

        webView?.settings?.javaScriptEnabled = true
        webView?.settings?.allowContentAccess = true
        webView?.settings?.allowFileAccess = true

        // Autoplay untuk audio
        webView?.settings?.mediaPlaybackRequiresUserGesture = false

        webView?.webViewClient = object : WebViewClient() {
		override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
		return false
		}
		}

        // Mendapatkan path ke direktori files di external storage
        val externalFilesDir = getExternalFilesDir(null)
        externalFilesDir?.let { dir ->
            val htmlFilePath = dir.absolutePath + "/mainmenu.html"
            webView?.loadUrl("file:///$htmlFilePath")

            // Menampilkan pesan alert
            webView?.webChromeClient = object : android.webkit.WebChromeClient() {
                override fun onJsAlert(view: WebView?, url: String?, message: String?, result: android.webkit.JsResult?): Boolean {
                    val builder = AlertDialog.Builder(this@GachaStudioMain)
                    builder.setTitle("Gacha Design Studio Berpesan!")
                            .setMessage(message)
                            .setPositiveButton("Salin ke Papan Klip") { dialog, id ->
                                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Pesan", message)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(this@GachaStudioMain, "Pesan disalin ke papan klip", Toast.LENGTH_SHORT).show()
                                result?.confirm() // Ini perlu untuk mengkonfirmasi alert
                            }
                            .setNegativeButton("Oke") { dialog, id ->
                                dialog.dismiss()
                                result?.confirm() // Ini perlu untuk mengkonfirmasi alert
                            }
                    val dialog = builder.create()
                    dialog.setCancelable(false)
                    dialog.setCanceledOnTouchOutside(false)
                    dialog.show()
                    return true
                }
            }
        }

        // Sembunyikan tombol navigasi
        webView?.systemUiVisibility = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (event?.action == KeyEvent.ACTION_DOWN) {
                if (webView?.canGoBack() == true) {
                    webView?.goBack()
                } else {
                    exitApp()
                }
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun exitApp() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - backPressedTime > PRESS_BACK_INTERVAL) {
            backPressedTime = currentTime
            Toast.makeText(this, "Tekan dua kali lagi untuk keluar dari game!", Toast.LENGTH_SHORT).show()
            Thread {
                try {
                    Thread.sleep(3000) // Tunggu 3 detik
                    backPressedTime = 0
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }.start()
        } else {
            finish()
            // force close aplikasi
            System.exit(0)
        }
    }

    // Method untuk menampilkan dialog selamat datang
    private fun showWelcomeDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Selamat Datang di Gacha Design Studio!")
        builder.setMessage(
                "Gacha Design Studio hadir dengan bebas berkreasi membuat karakter Gacha sesuka mu dengan dukungan kustomisasi penuh.\n\nApakah kamu ingin memainkan nya?\nKalau iya tolong pilih setuju untuk mengizinkan akses penyimpanan dan unduh sumber daya tambahan sekitar 70Mb an.\n\n! Jika tidak setuju maka game ini keluar !\n\n\n\n\nGacha Design Studio ini dibuat olah para penggemar Lunime")
        builder.setPositiveButton("Setuju") { dialog, which ->
            dialog.dismiss()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestStoragePermission()
            } else {
                downloadResources()
            }
        }

        builder.setNegativeButton("Tidak Setuju") { dialog, which ->
            dialog.dismiss()
            finish()
        }
        builder.setCancelable(false)
        builder.show()
    }

    // Method untuk meminta izin akses penyimpanan
    private fun requestStoragePermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_CODE)
    }

    // Method untuk memulai unduhan sumber daya tambahan
    private fun downloadResources() {
        progressDialog = AlertDialog.Builder(this).create()
        val dialogView = layoutInflater.inflate(R.layout.dialog_progress, null)
        progressBar = dialogView.findViewById(R.id.progressBar)
        progressDialog?.setView(dialogView)
        progressDialog?.setTitle("Sedang mengunduh sumber daya tambahan...")
        progressDialog?.setCancelable(false)
        progressDialog?.setCanceledOnTouchOutside(false)
        progressDialog?.show()
        DownloadResourcesTask().execute()
    }

    // AsyncTask untuk mengunduh sumber daya tambahan
    private inner class DownloadResourcesTask : AsyncTask<Void, Int, Boolean>() {

        override fun doInBackground(vararg voids: Void): Boolean {
            return try {
                val url = URL("https://github.com/archanaberry/Gacha-Design-Studio/archive/refs/heads/DL.zip")
                val connection = url.openConnection() as HttpURLConnection
                connection.connect()

                val fileLength = connection.contentLength

                val tempDir = File(TEMP_DIR_PATH)
                if (!tempDir.exists()) {
                    tempDir.mkdirs()
                }

                val outputFile = File(ZIP_FILE_PATH)
                if (outputFile.exists()) {
                    outputFile.delete()
                }

                val input = BufferedInputStream(url.openStream())
                val output = FileOutputStream(outputFile)

                val data = ByteArray(1024)
                var total: Long = 0
                var count: Int
                while (input.read(data).also { count = it } != -1) {
                    total += count.toLong()
                    publishProgress((total * 100 / fileLength).toInt())
                    output.write(data, 0, count)
                }

                output.flush()
                output.close()
                input.close()

                unzip(ZIP_FILE_PATH, EXTRACTED_DIR_PATH!!)
                moveExtractedFiles(SOURCE_DIR_PATH!!, DEST_DIR_PATH!!)
                deleteTempDirectory()

                true
            } catch (e: IOException) {
                e.printStackTrace()
                false
            }
        }

        override fun onProgressUpdate(vararg values: Int?) {
            super.onProgressUpdate(*values)
            progressDialog?.setTitle("Sedang mengekstrak sumber daya tambahan...")
            values[0]?.let { progressBar?.progress = it }
        }

        override fun onPostExecute(success: Boolean) {
            super.onPostExecute(success)
            progressDialog?.dismiss()
            if (success) {
                val builder = AlertDialog.Builder(this@GachaStudioMain)
                builder.setTitle("Unduhan Berhasil")
                builder.setMessage("Sumber daya sudah berhasil diekstrak!")
                builder.setPositiveButton("OK") { dialog, which ->
                    dialog.dismiss()
                    setAgreementAccepted(true)
                    loadWebViewContent()
                }
                builder.setCancelable(false)
                builder.show()
            } else {
                val builder = AlertDialog.Builder(this@GachaStudioMain)
                builder.setTitle("Unduhan Gagal")
                builder.setMessage("Maaf, unduhan gagal. Silakan coba lagi nanti.")
                builder.setPositiveButton("OK") { dialog, which ->
                    dialog.dismiss()
                    finish()
                }
                builder.setCancelable(false)
                builder.show()
            }
        }
    }

    private fun unzip(zipFilePath: String?, destDirectory: String?) {
        try {
            val destDir = File(destDirectory)
            if (!destDir.exists()) {
                destDir.mkdirs()
            }
            val zipIn = ZipInputStream(FileInputStream(zipFilePath))
            var entry: ZipEntry? = zipIn.nextEntry
            while (entry != null) {
                val filePath = destDirectory + File.separator + entry.name
                if (!entry.isDirectory) {
                    extractFile(zipIn, filePath)
                } else {
                    val dir = File(filePath)
                    dir.mkdirs()
                }
                zipIn.closeEntry()
                entry = zipIn.nextEntry
            }
            zipIn.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun extractFile(zipIn: ZipInputStream, filePath: String) {
        try {
            val bos = BufferedOutputStream(FileOutputStream(filePath))
            val bytesIn = ByteArray(4096)
            var read: Int
            while (zipIn.read(bytesIn).also { read = it } != -1) {
                bos.write(bytesIn, 0, read)
            }
            bos.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun moveExtractedFiles(srcDirPath: String, destDirPath: String) {
        try {
            val srcDir = File(srcDirPath)
            val destDir = File(destDirPath)
            if (!destDir.exists()) {
                destDir.mkdirs()
            }

            FileUtils.copyDirectory(srcDir, destDir)

            FileUtils.deleteDirectory(srcDir)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun deleteTempDirectory() {
        try {
            val tempDir = File(TEMP_DIR_PATH)
            FileUtils.deleteDirectory(tempDir)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                downloadResources()
            } else {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Izin Diperlukan")
                builder.setMessage(
                    "Aplikasi memerlukan izin akses penyimpanan untuk melanjutkan. Izinkan akses penyimpanan melalui Pengaturan?")
                builder.setPositiveButton("OK") { dialogInterface, i ->
                    val intent = Intent()
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivityForResult(intent, DOWNLOAD_REQUEST_CODE)
                }
                builder.setNegativeButton("Tidak") { dialogInterface, i ->
                    finish()
                }
                builder.setCancelable(false)
                builder.show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == DOWNLOAD_REQUEST_CODE) {
            if (ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                downloadResources()
            } else {
                finish()
            }
        }
    }
}
