package com.setupnotebook.ui

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

object ShareUtil {
    /** Writes [content] to a cache file and opens the Android share sheet for it. */
    fun shareJson(context: Context, baseName: String, content: String) {
        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val safeName = baseName.replace(Regex("[^A-Za-z0-9-_ ]"), "").trim()
            .ifBlank { "setup" }.replace(' ', '_')
        val file = File(dir, "$safeName.json")
        file.writeText(content)
        val uri = FileProvider.getUriForFile(context, "com.setupnotebook.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, safeName)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Export setup"))
    }
}
