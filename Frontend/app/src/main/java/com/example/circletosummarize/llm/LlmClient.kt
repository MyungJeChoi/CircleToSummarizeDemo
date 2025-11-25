package com.example.circletosummarize.llm

interface LlmClient {
    /**
     * OCR로 얻은 텍스트를 요약.
     * 나중에 phi3 / llama / gemma on-device LLM으로 교체할 수 있음.
     */
    suspend fun summarize(text: String, lang: String = "korean"): String
}