package com.lunime.githubcollab.archanaberry.gachadesignstudio

import android.os.Process
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object GachaStudioLogger {

    private val logBuffer = mutableListOf<String>() // Buffer untuk menyimpan log sementara

    // Fungsi untuk mendeteksi direktori dinamis berdasarkan ID user
    private fun detectDynamicDirectory(): String {
        val userId = Process.myUserHandle().hashCode()
        return if (userId == 0) {
            "/storage/emulated/0"  // Direktori umum untuk user 0
        } else {
            "/storage/emulated/$userId"  // Direktori berdasarkan ID user
        }
    }

    // Fungsi untuk membuat/memilih file log
    private fun getLogFile(): File {
        val directory = File(detectDynamicDirectory() + "/Lunime/data/logging")
        if (!directory.exists()) {
            val created = directory.mkdirs()  // Membuat folder logging jika belum ada
            if (created) {
                println("Folder logging berhasil dibuat di: ${directory.absolutePath}")
            } else {
                println("Gagal membuat folder logging di: ${directory.absolutePath}")
            }
        }

        val dateFormat = SimpleDateFormat("ddMMMyyyy", Locale.getDefault())
        val date = dateFormat.format(Date())
        var logFile = File(directory, "log0-$date.txt")
        var logIndex = 0

        // Cek apakah file log sudah ada dan tentukan log berikutnya
        while (logFile.exists()) {
            logIndex++
            logFile = File(directory, "log$logIndex-$date.txt")
        }

        return logFile
    }

    // Fungsi untuk mencetak log dan menyimpannya ke buffer
    fun log(message: String, isError: Boolean = false) {
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val timeStamp = timeFormat.format(Date())
        val dateFormat = SimpleDateFormat("ddMMMyyyy", Locale.getDefault())
        val date = dateFormat.format(Date())

        // Tentukan kategori log berdasarkan jenis pesan
        val logType = if (isError) "Lunime Corrupted" else "Archana Berry Analyzer"

        // Format pesan log
        val logMessage = "$timeStamp/$date - $logType: $message"

        // Tampilkan di konsol
        println(logMessage)

        // Simpan log ke buffer
        logBuffer.add(logMessage)
    }

    // Fungsi untuk menyimpan semua log dari buffer ke file
    fun flushLogs() {
        try {
            val logFile = getLogFile()
            logBuffer.forEach { logMessage ->
                logFile.appendText("$logMessage\n")
            }
            logBuffer.clear() // Bersihkan buffer setelah disimpan
        } catch (e: Exception) {
            println("Gagal menyimpan log ke file: ${e.message}")
        }
    }

    // Tambahkan *shutdown hook* untuk menyimpan log saat aplikasi keluar
    init {
        Runtime.getRuntime().addShutdownHook(Thread {
            flushLogs() // Simpan semua log saat aplikasi dihentikan
        })
    }
}