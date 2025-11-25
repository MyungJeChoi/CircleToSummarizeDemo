package com.example.circletosummarize.share

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast

fun shareText(context: Context, text: String) {
    if (text.isBlank()) return

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }

    val chooser = Intent.createChooser(intent, "요약 내용 공유")
    context.startActivity(chooser)
}

fun copyToClipboard(context: Context, text: String) {
    if (text.isBlank()) return

    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("summary", text)
    clipboard.setPrimaryClip(clip)

    Toast.makeText(context, "클립보드에 복사됨", Toast.LENGTH_SHORT).show()
}