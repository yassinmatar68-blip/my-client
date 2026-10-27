package com.yaso.client

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import java.io.File
import java.util.zip.ZipInputStream

enum class ImportType(val folder: String, val label: String) {
    WORLD("minecraftWorlds", "World"),
    RESOURCE_PACK("resource_packs", "Resource Pack"),
    BEHAVIOR_PACK("behavior_packs", "Behavior Pack"),
    SKIN_PACK("skin_packs", "Skin Pack")
}

object Importer {

    private val base = File(
        Environment.getExternalStorageDirectory(),
        "Android/data/com.mojang.minecraftpe/files/games/com.mojang"
    )

    fun import(context: Context, uri: Uri, type: ImportType): Result<String> {
        return try {
            val targetRoot = File(base, type.folder)
            targetRoot.mkdirs()

            val name = queryName(context, uri).substringBeforeLast('.')
            val dest = File(targetRoot, name)
            dest.mkdirs()

            context.contentResolver.openInputStream(uri)!!.use { input ->
                ZipInputStream(input).use { zip ->
                    var entry = zip.nextEntry
                    while (entry != null) {
                        val out = File(dest, entry.name)
                        if (!out.canonicalPath.startsWith(dest.canonicalPath)) {
                            throw SecurityException("Bad zip entry")
                        }
                        if (entry.isDirectory) {
                            out.mkdirs()
                        } else {
                            out.parentFile?.mkdirs()
                            out.outputStream().use { zip.copyTo(it) }
                        }
                        zip.closeEntry()
                        entry = zip.nextEntry
                    }
                }
            }
            Result.success("${type.label} imported: $name")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun queryName(context: Context, uri: Uri): String {
        context.contentResolver.query(uri, null, null, null, null)?.use { c ->
            val i = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (i >= 0 && c.moveToFirst()) return c.getString(i)
        }
        return "imported_${System.currentTimeMillis()}"
    }
}
