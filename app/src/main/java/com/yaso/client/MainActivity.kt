package com.yaso.client

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private var pendingType: ImportType? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestAllFilesAccess()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setBackgroundColor(Color.parseColor("#121212"))
            setPadding(48, 96, 48, 48)
        }

        root.addView(TextView(this).apply {
            text = "Yaso Client"
            textSize = 32f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        })

        root.addView(makeButton("Launch Minecraft") {
            Launcher.launchMinecraft(this)
        })

        root.addView(TextView(this).apply {
            text = "Importers"
            textSize = 18f
            setTextColor(Color.LTGRAY)
            setPadding(0, 64, 0, 16)
        })

        ImportType.values().forEach { type ->
            root.addView(makeButton("Import ${type.label}") {
                pendingType = type
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    this.type = "*/*"
                }
                startActivityForResult(intent, 1)
            })
        }

        setContentView(root)
    }

    private fun makeButton(label: String, onClick: () -> Unit): Button {
        return Button(this).apply {
            text = label
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 24 }
            setOnClickListener { onClick() }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val uri: Uri? = data?.data
        val type = pendingType
        if (requestCode == 1 && resultCode == RESULT_OK && uri != null && type != null) {
            val result = Importer.import(this, uri, type)
            Toast.makeText(
                this,
                result.getOrElse { "Import failed: ${it.message}" },
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun requestAllFilesAccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
            !Environment.isExternalStorageManager()
        ) {
            startActivity(
                Intent(
                    Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                    Uri.parse("package:$packageName")
                )
            )
        }
    }
}
