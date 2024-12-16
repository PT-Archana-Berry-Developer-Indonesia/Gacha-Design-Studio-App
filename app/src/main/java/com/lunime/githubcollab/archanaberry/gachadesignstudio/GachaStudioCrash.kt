package com.lunime.githubcollab.archanaberry.gachadesignstudio

import android.app.Application
import android.content.Context
import android.os.Process
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class CrashHandler(private val context: Context) : Thread.UncaughtExceptionHandler {

    private val defaultHandler: Thread.UncaughtExceptionHandler =
        Thread.getDefaultUncaughtExceptionHandler()

    override fun uncaughtException(t: Thread, e: Throwable) {
        // Menangani crash
        handleCrash(e)

        // Menggunakan default handler setelah penanganan crash
        defaultHandler.uncaughtException(t, e)
    }

    private fun handleCrash(e: Throwable) {
        // Membuat laporan crash dengan format yang diminta
        val crashReport = generateCrashReport(e)

        // Menyimpan laporan ke file
        val crashFile = saveCrashReport(crashReport)

        // Menampilkan dialog error
        showErrorDialog(crashFile, e)
    }

    private fun generateCrashReport(e: Throwable): String {
        val timeStamp = SimpleDateFormat("ddMMMyyyy", Locale.getDefault()).format(Date())
        val stackTrace = e.stackTraceToString()
        
        // Contoh error yang dihasilkan
        
        /*
        Gacha Design Studio - Error Report
        ----------------------------------
		Waktu: 17Desember2024
		Error: java.lang.NullPointerException: Attempt to invoke virtual method 'void com.lunime.gachadesignstudio.MyActivity.someMethod()' on a null object reference
		Stack Trace:
		java.lang.NullPointerException: Attempt to invoke virtual method 'void com.lunime.gachadesignstudio.MyActivity.someMethod()' on a null object reference
	at com.lunime.gachadesignstudio.MyActivity.onCreate(MyActivity.java:50)
	at android.app.Activity.performCreate(Activity.java:7393)
	at android.app.Activity.performCreate(Activity.java:7380)
	at android.app.Instrumentation.callActivityOnCreate(Instrumentation.java:1217)
	at android.app.ActivityThread.performLaunchActivity(ActivityThread.java:3265)
	at android.app.ActivityThread.handleLaunchActivity(ActivityThread.java:3402)
	at android.app.ActivityThread.handleRelaunchActivity(ActivityThread.java:5323)
	at android.app.ActivityThread$H.handleMessage(ActivityThread.java:1996)
	at android.os.Handler.dispatchMessage(Handler.java:107)
	at android.os.Looper.loop(Looper.java:214)
	at android.app.ActivityThread.main(ActivityThread.java:7356)
	at java.lang.reflect.Method.invoke(Method.java)
	at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:1296)
		30 More...
        */

        return """
            ${GachaStudioLocalization.instance["gachastudio_crashreport"]}
            ----------------------------------
            ${GachaStudioLocalization.instance["crash_time"]}: $timeStamp
            ${GachaStudioLocalization.instance["crash_error"]}: ${e.localizedMessage}
            ${GachaStudioLocalization.instance["crash_stacktrace"]}:
            $stackTrace
        """.trimIndent()
    }

    private fun saveCrashReport(crashReport: String): File {
    // Mengambil prefix untuk direktori dari konfigurasi lokal
    val crashDirectoryName = GachaStudioLocalization.instance["crash_directory_name"] ?: "mogok"

    // Menentukan direktori penyimpanan laporan crash
    val dir = detectDynamicDirectory() + "/Lunime/data/$crashDirectoryName"
    val directory = File(dir)

    // Membuat direktori jika belum ada
    if (!directory.exists()) {
        directory.mkdirs()
    }

    // Mendapatkan format tanggal sesuai dengan lokal
    val date = SimpleDateFormat("ddMMMyyyy", Locale.getDefault()).format(Date())

    // Mendapatkan prefix untuk nama file dari pengaturan lokal
    val filePrefix = GachaStudioLocalization.instance["file_prefix_crash"]

    // Menentukan nama file menggunakan nomor urut dan tanggal
    val fileName = "$filePrefix-${getNextCrashFileNumber(dir)}-$date.txt"
    val crashFile = File(directory, fileName)

    // Menyimpan laporan crash ke dalam file
    FileOutputStream(crashFile).use {
        it.write(crashReport.toByteArray())
    }

    // Mengembalikan file yang sudah dibuat
    return crashFile
}


    private fun getNextCrashFileNumber(directory: String): Int {
    // Mendapatkan prefix file dari lokal (misalnya "mogok" bisa diganti dengan terjemahan lokal)
    val filePrefix = GachaStudioLocalization.instance["file_prefix_crash"]

    // Mendapatkan daftar file yang ada di direktori
    val files = File(directory).listFiles { _, name ->
        name.startsWith(filePrefix) && name.endsWith(".txt")
    }

    // Mengambil nomor urut file dari nama file
    val numbers = files?.mapNotNull {
        // Mengambil bagian nomor dari nama file yang dipisah berdasarkan "-"
        val parts = it.name.split("-")
        parts.getOrNull(1)?.toIntOrNull()
    }

    // Menentukan nomor file berikutnya
    val nextNumber = (numbers?.maxOrNull() ?: -1) + 1
    return nextNumber
}

    private fun showErrorDialog(crashFile: File, e: Throwable) {
        val title = GachaStudioLocalization.instance["crash_dialog_title"] // Gacha Design Studio - Mengalami Mogok (Crash))
        val message = GachaStudioLocalization.instance["crash_dialog_message"]
            .replace("{filePath}", crashFile.absolutePath)

        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(
                GachaStudioLocalization.instance["button_ok"]
            ) { dialog, _ ->
                dialog.dismiss()
                // Tampilkan Toast setelah tombol OK ditekan
                Toast.makeText(
                    context,
                    GachaStudioLocalization.instance["toast_file_saved"]
                        .replace("{filePath}", crashFile.absolutePath),
                    Toast.LENGTH_LONG
                ).show()
            }
            .setCancelable(false)
            .show()
    }

    private fun detectDynamicDirectory(): String {
        val userId = Process.myUserHandle().hashCode()
        return if (userId == 0) {
            "/storage/emulated/0"
        } else {
            "/storage/emulated/$userId"
        }
    }
}