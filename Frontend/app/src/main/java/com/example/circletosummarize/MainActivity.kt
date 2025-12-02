package com.example.circletosummarize

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll


import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import com.example.circletosummarize.share.copyToClipboard
import com.example.circletosummarize.share.shareText
import com.example.circletosummarize.ui.SummaryUiState
import com.example.circletosummarize.ui.SummaryViewModel
import com.example.circletosummarize.ui.theme.CircleToSummarizeTheme

import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels

class MainActivity : ComponentActivity() {

    private val viewModel: SummaryViewModel by viewModels()

    // 갤러리 이미지 선택
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { viewModel.onImageSelected(it) }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CircleToSummarizeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SummaryScreen(
                        uiState = viewModel.uiState,
                        onPickImage = { pickImageLauncher.launch("image/*") },
                        onShare = { text -> shareText(this, text) },
                        onCopy = { text -> copyToClipboard(this, text) }
                    )
                }
            }
        }
    }
}

@Composable
fun SummaryScreen(
    uiState: SummaryUiState,
    onPickImage: () -> Unit,
    onShare: (String) -> Unit,
    onCopy: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Circle-to-Summarize (스크린샷 버전)",
            style = MaterialTheme.typography.headlineSmall
        )

        Button(onClick = onPickImage) {
            Text("스크린샷 선택하기")
        }

        if (uiState.isLoading) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                Text(text = uiState.statusMessage)
            }
        } else if (uiState.statusMessage.isNotBlank()) {
            Text(text = uiState.statusMessage)
        }

        if (uiState.ocrText.isNotBlank()) {
            Text(
                text = "OCR 결과:",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = uiState.ocrText,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (uiState.summary.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "요약:",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = uiState.summary,
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onCopy(uiState.summary) }) {
                    Text("클립보드 복사")
                }
                Button(onClick = { onShare(uiState.summary) }) {
                    Text("다른 앱으로 공유")
                }
            }
        }
    }
}
