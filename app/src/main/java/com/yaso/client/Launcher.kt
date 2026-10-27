package com.yaso.client

import android.content.Context
import android.content.Intent
import android.widget.Toast

object Launcher {
    private const val MC_PACKAGE = "com.mojang.minecraftpe"

    fun launchMinecraft(context: Context) {
        val intent = context.packageManager.getLaunchIntentForPackage(MC_PACKAGE)
        if (intent == null) {
            Toast.makeText(context, "Minecraft is not installed", Toast.LENGTH_LONG).show()
            return
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
