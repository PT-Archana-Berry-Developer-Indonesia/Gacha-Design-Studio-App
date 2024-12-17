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
import android.view.Window
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
import org.json.JSONObject

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

import org.w3c.dom.Element

import android.webkit.MimeTypeMap

import androidx.core.content.ContextCompat

import android.view.WindowInsets
import android.view.WindowInsetsController

import android.os.Handler
import android.os.Looper

class GachaStudioMain : Activity() {

    // Constants
    private val DOWNLOAD_REQUEST_CODE = 2
    private val APP_FOLDER = "/Lunime"

    // File paths and other variables
    private var CONFIG_FILE_PATH: String? = null
    private var TEMP_DIR_PATH: String? = null
    private var ZIP_FILE_PATH: String? = null
    private var EXTRACTED_DIR_PATH: String? = null
    private var SOURCE_DIR_PATH: String? = null
    private var DEST_DIR_PATH: String? = null
    private var MANIFEST_RESOURCE: String? = null

    // UI components
    private var progressDialog: AlertDialog? = null
    private var progressBar: ProgressBar? = null
    private var remoteViews: RemoteViews? = null
    private var builder: NotificationCompat.Builder? = null
    private var notificationManager: NotificationManagerCompat? = null

    // URLs
    private val PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=com.lunime.githubcollab.archanaberry.gachadesignstudio"
    private val MANIFEST_URL = "https://raw.githubusercontent.com/archanaberry/Gacha-Design-Studio/DL/manifest.json"
    private val RESOURCE_URL = "https://github.com/archanaberry/Gacha-Design-Studio/archive/refs/heads/DL.zip"

    // WebView and back press logic
    private var backPressedTime: Long = 0
    private val PRESS_BACK_INTERVAL = 2000 // 2 seconds
    
    private val REQUEST_CODE_FILE_PICK = 100
    private val PERMISSION_REQUEST_CODE = 1
    
    override fun onCreate(savedInstanceState: Bundle?) {
        
        // GachaStudioMain.kt
        // Deteksi direktori dinamis
        val baseDir = detectDynamicDirectory()

        // Inisialisasi path
        CONFIG_FILE_PATH = baseDir.plus(APP_FOLDER).plus("/data/data.xml")
        TEMP_DIR_PATH = baseDir.plus(APP_FOLDER).plus("/.temp")
        ZIP_FILE_PATH = TEMP_DIR_PATH?.plus("/DL.zip")
        EXTRACTED_DIR_PATH = TEMP_DIR_PATH?.plus("/")
        SOURCE_DIR_PATH = EXTRACTED_DIR_PATH?.plus("Gacha-Design-Studio-DL/")
        MANIFEST_RESOURCE = baseDir.plus(APP_FOLDER).plus("/manifest.json")
        DEST_DIR_PATH = baseDir.plus(APP_FOLDER).plus("/")
        
        super.onCreate(savedInstanceState)
        
        setContentView(R.layout.gachastudio_welcome)
        GachaStudioLogger.log("Sedang menginisialisasi aktivitas utama Gacha Design Studio...")
        
        val agreementAccepted = isAgreementAccepted()

if (agreementAccepted) {
    // Periksa apakah pembaruan diperlukan sebelum masuk ke GachaStudio
    if (checkForUpdates()) {
        // Jika pembaruan diperlukan, tampilkan dialog pembaruan
        GachaStudioLogger.log("Memeriksa pembaruan...")
        if (showUpdateDialog()) {
            GachaStudioLogger.log("Perbaruan di temukan!")
        }
    } else {
        // Jika tidak ada pembaruan, lanjutkan ke GachaStudio
        openGachaDesignStudio()
        GachaStudioLogger.log("Membuka Gacha Design Studio...")
    }
} else {
    // Jika perjanjian belum diterima, tampilkan dialog sambutan
    showWelcomeDialog()
    GachaStudioLogger.log("Aku ingin menyapa :3")
	}
}
    
    private fun openGachaDesignStudio() {
    val intent = Intent(this, GachaStudio::class.java)
    startActivity(intent)
    if (finish()) {
        GachaStudioLogger.log("Aku hindarkan aktivitas utama karena ga ada apa apa, KOSONG!")
    } // Tutup aktivitas saat ini
}

        // Fungsi untuk mendeteksi direktori dinamis
    private fun detectDynamicDirectory(): String {
    // Deteksi apakah pengguna adalah user pertama (0) atau lainnya
    val userId = android.os.Process.myUserHandle().hashCode() // Unik untuk tiap pengguna
    GachaStudioLogger.log("Menggunakan penyimpanan di pengguna $userId")
    return if (userId == 0) {
        "/storage/emulated/0"
    } else {
        "/storage/emulated/$userId"
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
                GachaStudioLogger.log("Yey, persetujuan nya diterima :3")
            }
        } catch (e: Exception) {
                // Mengubah stacktrace menjadi String
            val stackTraceWriter = StringWriter()
            e.printStackTrace(PrintWriter(stackTraceWriter))
            val stackTraceAsString = stackTraceWriter.toString()

            e.printStackTrace()
            GachaStudioLogger.log("Yah persetujuan nya ditolak T wT")
            GachaStudioLogger.log("Yah kok aku ditolak sih? T wT, Dengan pesan ditolak $stackTraceAsString", isError = true)
            false
        }
    }

    private fun setAgreementAccepted(accepted: Boolean) {
        try {
            val configFile = File(CONFIG_FILE_PATH)
            if (!configFile.exists()) {
                configFile.parentFile.mkdirs()
                GachaStudioLogger.log("Membuat folder konfigurasi...")
                configFile.createNewFile()
                GachaStudioLogger.log("Menulis file konfigurasi...")
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
            GachaStudioLogger.log("Berhasil membuat file konfigurasi")
        } catch (e: Exception) {
            GachaStudioLogger.log("Gagal membuat file konfigurasi!", isError = true)
            e.printStackTrace()
        }
    }

        private fun showWelcomeDialog() {
        val builder = AlertDialog.Builder(this)
        GachaStudioLogger.log("Memanggil dialog sambutan selamat datang :3 ...")
        builder.setTitle("Selamat Datang di Gacha Design Studio!")
        builder.setMessage(
            "Gacha Design Studio hadir dengan bebas berkreasi membuat karakter Gacha sesuka mu dengan dukungan kustomisasi penuh.\n\nApakah kamu ingin memainkan nya?\nKalau iya tolong pilih setuju untuk mengizinkan akses penyimpanan dan unduh sumber daya tambahan sekitar 70Mb an.\n\n! Jika tidak setuju maka game ini keluar !\n\n\n\n\nGacha Design Studio ini dibuat olah para penggemar Lunime")
        builder.setPositiveButton("Setuju") {
            dialog,
            which ->
            dialog.dismiss()
            GachaStudioLogger.log("Menghapus dialog sambutan :> ...")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestStoragePermission()
                GachaStudioLogger.log("Archana Berry meminta akses nya kawan Un U ...")
            } else {
                downloadResources()
                GachaStudioLogger.log("Mengunduh sumber daya Gacha Design Studio...")
            }
        }

        builder.setNegativeButton("Tidak Setuju") {
            dialog,
            which ->
            dialog.dismiss()
            GachaStudioLogger.log("Kamu ga setuju yasudah aku pergi :v")
            finish()
            GachaStudioLogger.log("Bye...")
        }
        builder.setCancelable(false)
        builder.show()
        GachaStudioLogger.log("Membangun dialog...")
    }

    // Method untuk meminta izin akses penyimpanan
    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (!Environment.isExternalStorageManager()) {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.data = Uri.parse("package:$packageName")
                    startActivityForResult(intent, PERMISSION_REQUEST_CODE)
                    GachaStudioLogger.log("Mengarahkan ke pengaturan untuk diminta izinkan AKU!")
                } else {
                    downloadResources()
                    GachaStudioLogger.log("Otw download yagesya...")
                }
            } else {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSION_REQUEST_CODE)
                    GachaStudioLogger.log("Aku ingin mengecek sekali lagi :3")
                } else {
                    downloadResources()
                    GachaStudioLogger.log("Otw download yagesya...")
                }
            }
        } else {
            downloadResources()
            GachaStudioLogger.log("Otw download yagesya...")
        }
    }

    // Method untuk memulai unduhan sumber daya tambahan
    private fun downloadResources() {
        progressDialog = AlertDialog.Builder(this).create()
        val dialogView = layoutInflater.inflate(R.layout.dialog_progress, null)
        progressBar = dialogView.findViewById(R.id.progressBar)
        progressDialog?.setView(dialogView)
        progressDialog?.setTitle("Sedang mengunduh sumber daya tambahan... :>")
        GachaStudioLogger.log("Kawan aku sedang mengunduh sumber daya tambahan... :3")
        progressDialog?.setCancelable(false)
        progressDialog?.setCanceledOnTouchOutside(false)
        progressDialog?.show()
        DownloadResourcesTask().execute()
        GachaStudioLogger.log("Mengeksekusi tugas mengunduh...")
    }

    // AsyncTask untuk mengunduh sumber daya tambahan
    private inner class DownloadResourcesTask: AsyncTask < Void, Int, Boolean > () {

        override fun doInBackground(vararg voids: Void): Boolean {
            return try {
                val url = URL(RESOURCE_URL)
                val connection = url.openConnection() as HttpURLConnection
                connection.connect()
                GachaStudioLogger.log("Mengkoneksikan kembali... dari $url ke $connection")

                val fileLength = connection.contentLength

                val tempDir = File(TEMP_DIR_PATH)
                if (!tempDir.exists()) {
                    tempDir.mkdirs()
                    GachaStudioLogger.log("Membuat folder $tempDir")
                }

                val outputFile = File(ZIP_FILE_PATH)
                if (outputFile.exists()) {
                    outputFile.delete()
                    GachaStudioLogger.log("Menghapus zip...")
                }

                val input = BufferedInputStream(url.openStream())
                val output = FileOutputStream(outputFile)

                val data = ByteArray(1024)
                var total: Long = 0
                var count: Int
                while (input.read(data).also {
                        count = it
                    } != -1) {
                    total += count.toLong()
                    publishProgress((total * 100 / fileLength).toInt())
                    output.write(data, 0, count)
                    GachaStudioLogger.log("Progres pengunduhan...")
                }

                output.flush()
                output.close()
                input.close()

                if (unzip(ZIP_FILE_PATH, EXTRACTED_DIR_PATH!!)) {
                    GachaStudioLogger.log("Mengekstrak sumber daya...")
                }
                if (moveExtractedFiles(SOURCE_DIR_PATH!!, DEST_DIR_PATH!!)) {
                GachaStudioLogger.log("Memindahkan ke folder Lunime...")
                }
                if (deleteTempDirectory()) {
                    GachaStudioLogger.log("Menghapus folder sementara beserta isi nya...")
                }

                true
            } catch (e: IOException) {
                GachaStudioLogger.log("Yah kak aku mengalami kegagalan Tw T!")
                e.printStackTrace()
                false
            }
        }

        override fun onProgressUpdate(vararg values: Int ? ) {
            super.onProgressUpdate( * values)
            progressDialog?.setTitle("Sedang mengekstrak sumber daya tambahan... :>")
            values[0]?.let {
                progressBar?.progress = it
                GachaStudioLogger.log("Progres pembaruan...")
            }
        }

        override fun onPostExecute(success: Boolean) {
            super.onPostExecute(success)
            progressDialog?.dismiss()
            if (success) {
                val builder = AlertDialog.Builder(this@GachaStudioMain)
                builder.setTitle("Unduhan Berhasil")
                GachaStudioLogger.log("Yey, dah berhasil kawan didownload Uw U")
                builder.setMessage("Sumber daya sudah berhasil diekstrak! O wO")
                GachaStudioLogger.log("Yey, sumber daya nya sudah berhasil di ekstrak Uw U")
                builder.setPositiveButton("OK") {
                    dialog,
                    which ->
                    dialog.dismiss()
                    setAgreementAccepted(true)
                    openGachaDesignStudio()
                    GachaStudioLogger.log("Saat nya ditunggu tunggu, membuka Gacha Studio Design...")
                }
                builder.setCancelable(false)
                builder.show()
                GachaStudioLogger.log("Memunculkan bangun dialog")
            } else {
                val builder = AlertDialog.Builder(this@GachaStudioMain)
                builder.setTitle("Unduhan Gagal")
                builder.setMessage("Maaf, unduhan gagal. Silakan coba lagi nanti T vT.")
                GachaStudioLogger.log("Yah kak aku mengalami kegagalan mengunduh Tw T!")
                builder.setPositiveButton("OK") {
                    dialog,
                    which ->
                    dialog.dismiss()
                    finish()
                }
                builder.setCancelable(false)
                builder.show()
                GachaStudioLogger.log("Memunculkan bangun dialog")
            }
        }
    }

    private fun unzip(zipFilePath: String ? , destDirectory : String ? ) {
        try {
            val destDir = File(destDirectory)
            if (!destDir.exists()) {
                destDir.mkdirs()
                GachaStudioLogger.log("Membuat folder di: $destDir")
            }
            val zipIn = ZipInputStream(FileInputStream(zipFilePath))
            var entry: ZipEntry ? = zipIn.nextEntry
            GachaStudioLogger.log("Memasang antrian unzip $entry")
            while (entry != null) {
                val filePath = destDirectory + File.separator + entry.name
                if (!entry.isDirectory) {
                    extractFile(zipIn, filePath)
                    GachaStudioLogger.log("Mengekstrak file sumber daya $zipIn ke $filePath")
                } else {
                    val dir = File(filePath)
                    dir.mkdirs()
                    GachaStudioLogger.log("Membuat folder di $filePath")
                }
                zipIn.closeEntry()
                entry = zipIn.nextEntry
                GachaStudioLogger.log("Menutup antrian unzip $entry")
            }
            zipIn.close()
            GachaStudioLogger.log("Menutup ekstrak sumber daya file")
        } catch (e: IOException) {
            e.printStackTrace()
            GachaStudioLogger.log("Waduh kak, terjadi error  pas lagi ekstrak nih?", isError = true)
        }
    }

    private fun extractFile(zipIn: ZipInputStream, filePath: String) {
        try {
            val bos = BufferedOutputStream(FileOutputStream(filePath))
            val bytesIn = ByteArray(4096)
            var read: Int
            while (zipIn.read(bytesIn).also {
                    read = it
                } != -1) {
                bos.write(bytesIn, 0, read)
            }
            bos.close()
            GachaStudioLogger.log("Menutup pengekstrakan (BOS)...")
        } catch (e: IOException) {
            e.printStackTrace()
            GachaStudioLogger.log("Yah kak error nih? (BOS)", isError = true)
        }
    }

    private fun moveExtractedFiles(srcDirPath: String, destDirPath: String) {
        try {
            val srcDir = File(srcDirPath)
            val destDir = File(destDirPath)

            // Pastikan direktori tujuan sudah ada
            if (!destDir.exists()) {
                destDir.mkdirs()
                GachaStudioLogger.log("Memindahkan file hasil ekstrak dari $srcDir ke $destDir")
            }

            // Salin semua file dan direktori dari srcDir ke destDir
            srcDir.walkTopDown().forEach {
                file ->
                    val relativePath = file.relativeTo(srcDir)
                val destFile = File(destDir, relativePath.path)
                if (file.isDirectory) {
                    destFile.mkdirs()
                    GachaStudioLogger.log("Menyalin semua file dari $relativePath ke $destFile")
                } else {
                    file.copyTo(destFile, overwrite = true)
                    GachaStudioLogger.log("Mwehehe, file lama akan ku update!, KEMUNGKINAN BESAR DATA KEGANTI DENGAN BARU!", isError = true)
                }
            }

            // Hapus direktori sumber setelah selesai menyalin
            if (srcDir.exists()) {
                FileUtils.deleteDirectory(srcDir)
                GachaStudioLogger.log("Aku menghapus folder ini $srcDir ...", isError = true)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            GachaStudioLogger.log("Yah kak terjadi kesalahan!", isError = true)
        }
    }


    private fun deleteTempDirectory() {
        try {
            val tempDir = File(TEMP_DIR_PATH)
            FileUtils.deleteDirectory(tempDir)
            GachaStudioLogger.log("Menghapus folder sementara $tempDir agar luas memori nya", isError = true)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    ///
    private fun checkForUpdates(): Boolean {
    // Membaca manifest lokal
    val localManifestFile = File(MANIFEST_RESOURCE)
    if (localManifestFile.exists()) {
        val localManifest = JSONObject(localManifestFile.readText())
        val remoteManifest = getRemoteManifest()

        // Cek jika manifest remote berhasil diambil
        if (remoteManifest != null) {
            val localAppVer = localManifest.getString("appver")
            val localVersion = localManifest.getString("version")
            val remoteAppVer = remoteManifest.getString("appver")
            val remoteVersion = remoteManifest.getString("version")

            // Bandingkan versi aplikasi (appver)
            if (compareVersions(remoteAppVer, localAppVer) > 0) {
                showUpdateDialog()
                return true  // Pembaruan aplikasi diperlukan
            } else if (compareVersions(remoteAppVer, localAppVer) < 0) {
                showDowngradeDialog()
                return false  // Tidak perlu pembaruan, tetapi downgrade diperlukan
            }

            // Bandingkan versi sumber daya (version)
            if (compareVersions(remoteVersion, localVersion) > 0) {
                showResourceUpdateDialog()
                return true  // Pembaruan sumber daya diperlukan
            }
        }
    }
    return false  // Tidak ada pembaruan
}

private fun getRemoteManifest(): JSONObject? {
    return try {
        val url = URL(MANIFEST_URL)
        val connection = url.openConnection() as HttpURLConnection
        connection.connect()

        val inputStream = connection.inputStream
        val content = inputStream.readBytes().toString(Charsets.UTF_8)
        JSONObject(content)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun compareVersions(version1: String, version2: String): Int {
    val order = mapOf("alpha" to 1, "beta" to 2, "stable" to 3, "unstable" to 4)
    val parts1 = version1.split("_")
    val parts2 = version2.split("_")
    val num1 = parts1[0].toDouble()
    val num2 = parts2[0].toDouble()
    val type1 = order[parts1[1]] ?: 0  // Pastikan type1 tidak null
    val type2 = order[parts2[1]] ?: 0  // Pastikan type2 tidak null

    return when {
        num1 > num2 -> 1
        num1 < num2 -> -1
        else -> type1.compareTo(type2)  // type1 dan type2 sekarang tidak nullable
    }
}

private fun showUpdateDialog() {
    AlertDialog.Builder(this)
        .setTitle("Gacha Design Studio")
        .setMessage("Gacha Design Studio butuh update aplikasi. Apakah Anda ingin mengupdate nya di Play Store? ｡⁠◕⁠‿⁠◕⁠｡")
        .setPositiveButton("Sekarang!") { _, _ ->
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_STORE_URL))
            startActivity(intent)
            finish()
        }
        .setNegativeButton("Nanti", null)
        .show()
}

private fun showDowngradeDialog() {
    AlertDialog.Builder(this)
        .setTitle("Gacha Design Studio")
        .setMessage("Versi aplikasi lebih tinggi dari versi yang diunduh. Apakah Anda ingin mengunduh versi lama? T' nT")
        .setPositiveButton("Sekarang!") { _, _ ->
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_STORE_URL))
            startActivity(intent)
            finish()
        }
        .setNegativeButton("Nanti", null)
        .show()
}

private fun showResourceUpdateDialog() {
    AlertDialog.Builder(this)
        .setTitle("Gacha Design Studio")
        .setMessage("Sumber daya Gacha Design Studio telah diperbarui. Apakah Anda ingin mengunduhnya? :3")
        .setPositiveButton("Sekarang!") { _, _ ->
            downloadResources()
        }
        .setNegativeButton("Nanti", null)
        .show()
}
    
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array < String > ,
        grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                downloadResources()
                GachaStudioLogger.log("Otw mengunduh ygy...")
            } else {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Izin Diperlukan")
                builder.setMessage(
                    "Game ini memerlukan izin akses penyimpanan untuk melanjutkan. Izinkan dong akses penyimpanan nya melalui Pengaturan nya?, agar game ini bisa berjalan :v")
                    GachaStudioLogger.log("Izinkan dulu atuh akses mengolah file nya?!")
                builder.setPositiveButton("OK") {
                    dialogInterface,
                    i ->
                    val intent = Intent()
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivityForResult(intent, DOWNLOAD_REQUEST_CODE)
                }
                builder.setNegativeButton("Tidak") {
                    dialogInterface,
                    i ->
                    finish()
                }
                builder.setCancelable(false)
                builder.show()
                GachaStudioLogger.log("Membangun dialog...")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent ? ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    downloadResources()
                    GachaStudioLogger.log("Otw mengunduh...")
                } else {
                    Toast.makeText(this, "Kok gak diizinin?, jika ditolak game nya tidak dapat melanjutkan nya TwT.", Toast.LENGTH_SHORT).show()
                    GachaStudioLogger.log("//Menendang anda keluar...", isError = true)
                    finish()
                }
            }
        }
    }
}