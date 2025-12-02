package com.example.circletosummarize.llm

/* interface 분리 선언
 * 모델 교체 유연화
 */
interface LlmClient {
    suspend fun summarize(text: String, lang: String = "ko"): String
}