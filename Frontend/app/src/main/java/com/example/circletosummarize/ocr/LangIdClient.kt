package com.example.circletosummarize.ocr

import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.languageid.LanguageIdentificationOptions
import kotlinx.coroutines.tasks.await

class LangIdClient {

    private val identifier = LanguageIdentification.getClient(
        LanguageIdentificationOptions.Builder()
            .setConfidenceThreshold(0.5f)
            .build()
    )

    suspend fun detectPrimaryLanguage(text: String): String {
        if (text.isBlank()) return "und"

        val langCode = identifier.identifyLanguage(text).await()
        return langCode ?: "und"
    }
}
