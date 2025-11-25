package com.example.circletosummarize.ocr

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

class MlKitOcrClient(
    private val context: Context
) {

    // 기본 라틴 인식기 (한글+영문 혼합이면 MULTI 스펙 고려 가능)
    private val recognizer = TextRecognition.getClient(
        TextRecognizerOptions.Builder().build()
    )

    suspend fun recognizeText(uri: Uri): String {
        val image = InputImage.fromFilePath(context, uri)
        val result = recognizer.process(image).await()
        return result.text.trim()
    }
}