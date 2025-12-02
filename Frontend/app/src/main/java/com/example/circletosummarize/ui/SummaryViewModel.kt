package com.example.circletosummarize.ui

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.circletosummarize.llm.LlmClient
import com.example.circletosummarize.llm.OnnxLlmClient
import com.example.circletosummarize.ocr.MlKitOcrClient
import com.example.circletosummarize.ocr.LangIdClient
import kotlinx.coroutines.launch

data class SummaryUiState(
    val selectedImageUri: Uri? = null,
    val ocrText: String = "",
    val summary: String = "",
    val isLoading: Boolean = false,
    val statusMessage: String = ""
)

class SummaryViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val ocrClient = MlKitOcrClient(application)
    private val llmClient: LlmClient = OnnxLlmClient(application)

    var uiState by mutableStateOf(SummaryUiState())
        private set

    fun onImageSelected(uri: Uri) {
        uiState = uiState.copy(
            selectedImageUri = uri,
            isLoading = true,
            statusMessage = "OCR 진행 중...",
            ocrText = "",
            summary = ""
        )

        viewModelScope.launch {
            try {
                val text = ocrClient.recognizeText(uri)
                val client = LangIdClient()
                val langCode = client.detectPrimaryLanguage(text)

                uiState = uiState.copy(
                    ocrText = text,
                    statusMessage = "요약 중..."
                )

                val summary = if (text.isNotBlank()) {
                    llmClient.summarize(text, lang = langCode)
                } else {
                    "(OCR 결과 없음)"
                }

                uiState = uiState.copy(
                    summary = summary,
                    isLoading = false,
                    statusMessage = "완료"
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    statusMessage = "에러: ${e.message ?: "알 수 없는 오류"}"
                )
            }
        }
    }
}