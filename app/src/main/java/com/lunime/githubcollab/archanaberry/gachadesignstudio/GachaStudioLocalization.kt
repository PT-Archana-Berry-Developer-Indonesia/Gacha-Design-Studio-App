package com.lunime.githubcollab.archanaberry.gachadesignstudio

import android.content.Context
import android.widget.Toast
import java.io.IOException
import java.util.*

object ParsingString {

    fun computeLpsArray(pattern: String): IntArray {
        val m = pattern.length
        val lps = IntArray(m)
        var len = 0
        var i = 1

        while (i < m) {
            if (pattern[i] == pattern[len]) {
                len++
                lps[i] = len
                i++
            } else {
                if (len != 0) {
                    len = lps[len - 1]
                } else {
                    lps[i] = 0
                    i++
                }
            }
        }
        return lps
    }

    fun matchPattern(input: String, pattern: String): List<String> {
        val matches = mutableListOf<String>()
        val delimiter = "\u2B80"
        var start = input.indexOf(delimiter)

        while (start != -1) {
            var end = input.indexOf(delimiter, start + 1)
            if (end == -1) break

            val match = input.substring(start + 1, end)
            matches.add(match)

            start = input.indexOf(delimiter, end + 1)
        }
        return matches
    }
}

class GachaStudioLocalization private constructor() {
    private val translations: MutableMap<String, String> = mutableMapOf()

    companion object {
        val instance: GachaStudioLocalization by lazy { GachaStudioLocalization() }
    }

    private fun loadFileLangFromAssets(context: Context, filename: String): Boolean {
        try {
            val inputStream = context.assets.open("localization/$filename")
            inputStream.bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    val matches = ParsingString.matchPattern(line, "\u2B80")
                    if (matches.size >= 2) {
                        translations[matches[0]] = matches[1]
                    }
                }
            }
            return true
        } catch (e: IOException) {
            e.printStackTrace()
            GachaStudioLogger.log("Failed to open file from assets: $filename", true)
            return false
        }
    }

    private fun getSystemLanguage(): String {
        val currentLocale = Locale.getDefault()
        return currentLocale.language
    }

    private fun isLanguageFileAvailable(context: Context, filename: String): Boolean {
        return try {
            val inputStream = context.assets.open("localization/$filename")
            inputStream.close()
            true
        } catch (e: IOException) {
            false
        }
    }

    private fun listLanguageFiles(context: Context) {
        try {
            val files = context.assets.list("localization")
            files?.forEach {
                GachaStudioLogger.log("File ditemukan: $it")
            }
        } catch (e: IOException) {
            GachaStudioLogger.log("Gagal membaca direktori localization: ${e.message}", true)
        }
    }

    fun loadLanguageFile(context: Context, languageCode: String): Boolean {
        val filename = "GachaStudio.$languageCode"
        GachaStudioLogger.log("Memuat file bahasa: $filename")

        listLanguageFiles(context)

        return if (isLanguageFileAvailable(context, filename)) {
            GachaStudioLogger.log("File bahasa ditemukan: $filename")
            loadFileLangFromAssets(context, filename)
        } else {
            GachaStudioLogger.log("File untuk bahasa $languageCode tidak ditemukan, fallback ke bahasa Inggris.")
            loadFileLangFromAssets(context, "GachaStudio.en")
        }
    }

    operator fun get(variable: String): String {
        return translations[variable] ?: "Translation not found"
    }

    fun showToast(context: Context, key: String, duration: Int = Toast.LENGTH_SHORT) {
        val message = this[key]
        Toast.makeText(context, message, duration).show()
    }

    fun log(key: String) {
        val message = this[key]
        GachaStudioLogger.log(message)
    }

    fun showDialog(context: Context, key: String) {
        val message = this[key]
        android.app.AlertDialog.Builder(context)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}