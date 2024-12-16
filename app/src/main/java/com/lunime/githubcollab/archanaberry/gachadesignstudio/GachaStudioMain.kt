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
        
        val agreementAccepted = isAgreementAccepted()

if (agreementAccepted) {
    // Periksa apakah pembaruan diperlukan sebelum masuk ke GachaStudio
    if (checkForUpdates()) {
        // Jika pembaruan diperlukan, tampilkan dialog pembaruan
        showUpdateDialog()
    } else {
        // Jika tidak ada pembaruan, lanjutkan ke GachaStudio
        openGachaDesignStudio()
    }
} else {
    // Jika perjanjian belum diterima, tampilkan dialog sambutan
    showWelcomeDialog()
	}
}
    
    private fun openGachaDesignStudio() {
    val intent = Intent(this, GachaStudio::class.java)
    startActivity(intent)
    finish() // Tutup aktivitas saat ini
}

        // Fungsi untuk mendeteksi direktori dinamis
    private fun detectDynamicDirectory(): String {
    // Deteksi apakah pengguna adalah user pertama (0) atau lainnya
    val userId = android.os.Process.myUserHandle().hashCode() // Unik untuk tiap pengguna
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

        private fun showWelcomeDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Selamat Datang di Gacha Design Studio!")
        builder.setMessage(
            "Gacha Design Studio hadir dengan bebas berkreasi membuat karakter Gacha sesuka mu dengan dukungan kustomisasi penuh.\n\nApakah kamu ingin memainkan nya?\nKalau iya tolong pilih setuju untuk mengizinkan akses penyimpanan dan unduh sumber daya tambahan sekitar 70Mb an.\n\n! Jika tidak setuju maka game ini keluar !\n\n\n\n\nGacha Design Studio ini dibuat olah para penggemar Lunime")
        builder.setPositiveButton("Setuju") {
            dialog,
            which ->
            dialog.dismiss()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestStoragePermission()
            } else {
                downloadResources()
            }
        }

        builder.setNegativeButton("Tidak Setuju") {
            dialog,
            which ->
            dialog.dismiss()
            finish()
        }
        builder.setCancelable(false)
        builder.show()
    }

    // Method untuk meminta izin akses penyimpanan
    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (!Environment.isExternalStorageManager()) {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.data = Uri.parse("package:$packageName")
                    startActivityForResult(intent, PERMISSION_REQUEST_CODE)
                } else {
                    downloadResources()
                }
            } else {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSION_REQUEST_CODE)
                } else {
                    downloadResources()
                }
            }
        } else {
            downloadResources()
        }
    }

    // Method untuk memulai unduhan sumber daya tambahan
    private fun downloadResources() {
        progressDialog = AlertDialog.Builder(this).create()
        val dialogView = layoutInflater.inflate(R.layout.dialog_progress, null)
        progressBar = dialogView.findViewById(R.id.progressBar)
        progressDialog?.setView(dialogView)
        progressDialog?.setTitle("Sedang mengunduh sumber daya tambahan... :>")
        progressDialog?.setCancelable(false)
        progressDialog?.setCanceledOnTouchOutside(false)
        progressDialog?.show()
        DownloadResourcesTask().execute()
    }

    // AsyncTask untuk mengunduh sumber daya tambahan
    private inner class DownloadResourcesTask: AsyncTask < Void, Int, Boolean > () {

        override fun doInBackground(vararg voids: Void): Boolean {
            return try {
                val url = URL(RESOURCE_URL)
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
                while (input.read(data).also {
                        count = it
                    } != -1) {
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

        override fun onProgressUpdate(vararg values: Int ? ) {
            super.onProgressUpdate( * values)
            progressDialog?.setTitle("Sedang mengekstrak sumber daya tambahan... :>")
            values[0]?.let {
                progressBar?.progress = it
            }
        }

        override fun onPostExecute(success: Boolean) {
            super.onPostExecute(success)
            progressDialog?.dismiss()
            if (success) {
                val builder = AlertDialog.Builder(this@GachaStudioMain)
                builder.setTitle("Unduhan Berhasil")
                builder.setMessage("Sumber daya sudah berhasil diekstrak! O wO")
                builder.setPositiveButton("OK") {
                    dialog,
                    which ->
                    dialog.dismiss()
                    setAgreementAccepted(true)
                    openGachaDesignStudio()
                }
                builder.setCancelable(false)
                builder.show()
            } else {
                val builder = AlertDialog.Builder(this@GachaStudioMain)
                builder.setTitle("Unduhan Gagal")
                builder.setMessage("Maaf, unduhan gagal. Silakan coba lagi nanti T vT.")
                builder.setPositiveButton("OK") {
                    dialog,
                    which ->
                    dialog.dismiss()
                    finish()
                }
                builder.setCancelable(false)
                builder.show()
            }
        }
    }

    private fun unzip(zipFilePath: String ? , destDirectory : String ? ) {
        try {
            val destDir = File(destDirectory)
            if (!destDir.exists()) {
                destDir.mkdirs()
            }
            val zipIn = ZipInputStream(FileInputStream(zipFilePath))
            var entry: ZipEntry ? = zipIn.nextEntry
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
            while (zipIn.read(bytesIn).also {
                    read = it
                } != -1) {
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

            // Pastikan direktori tujuan sudah ada
            if (!destDir.exists()) {
                destDir.mkdirs()
            }

            // Salin semua file dan direktori dari srcDir ke destDir
            srcDir.walkTopDown().forEach {
                file ->
                    val relativePath = file.relativeTo(srcDir)
                val destFile = File(destDir, relativePath.path)
                if (file.isDirectory) {
                    destFile.mkdirs()
                } else {
                    file.copyTo(destFile, overwrite = true)
                }
            }

            // Hapus direktori sumber setelah selesai menyalin
            if (srcDir.exists()) {
                FileUtils.deleteDirectory(srcDir)
            }
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
    ///
    
    //3
    
    //3
    
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array < String > ,
        grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                downloadResources()
            } else {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Izin Diperlukan")
                builder.setMessage(
                    "Game ini memerlukan izin akses penyimpanan untuk melanjutkan. Izinkan dong akses penyimpanan nya melalui Pengaturan nya?, agar game ini bisa berjalan :v")
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
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent ? ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    downloadResources()
                } else {
                    Toast.makeText(this, "Kok gak diizinin?, jika ditolak game nya tidak dapat melanjutkan nya TwT.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }
}