package com.example.circletosummarize.llm

import android.app.Application
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OnnxLlmClient(
    private val app: Application
) : LlmClient {

    // ONNX Runtime 환경
    private val env: OrtEnvironment by lazy {
        OrtEnvironment.getEnvironment()
    }

    // 실제 모델은 assets/ 에 넣고 이름 맞춰야 함.
    // 예: app/src/main/assets/phi3-mini-4k-int4.onnx
    private val session: OrtSession by lazy {
        val modelBytes = app.assets.open("phi3-mini-4k-int4.onnx").readBytes()
        env.createSession(modelBytes)
    }

    override suspend fun summarize(text: String, lang: String): String =
        withContext(Dispatchers.Default) {
            // 1) 프롬프트 구성
            val prompt = buildPrompt(text, lang)

            // 2) TODO: 실제 토크나이저 + ONNX 호출
            // 현재는 구조만 잡고, 임시로 앞부분 잘라서 "요약처럼" 보여줌.
            // 나중에 phi3/llama/gemma의 tokenizer & session.run()을 여기에 구현.

            // ===== 임시 로직 (LLM 없이 동작 확인용) =====
            promptToNaiveSummary(text)
        }

    private fun buildPrompt(text: String, lang: String): String {
        return if (lang == "korean") {
            "다음 한국어 문서를 한 줄로 간단히 요약해줘.\n\n$text\n\n요약 (한 줄로):"
        } else {
            "Summarize the following text in one concise line.\n\n$text\n\nSummary (one line):"
        }
    }

    /**
     * LLM을 붙이기 전까지 쓸 naïve 요약:
     * - 줄바꿈 제거 + 앞부분 N자만 자르기.
     */
    private fun promptToNaiveSummary(text: String, maxChars: Int = 150): String {
        val flat = text
            .replace("\n", " ")
            .replace("\\s+".toRegex(), " ")
            .trim()
        return if (flat.length <= maxChars) flat else flat.take(maxChars) + "…"
    }

    // === 참고용 (나중에 구현시) ===
    @Suppress("unused")
    private suspend fun callOnnxModel(prompt: String): String =
        withContext(Dispatchers.Default) {
            // 예시 구조 (실제 구현시 교체)
            val fakeIds = longArrayOf(1L, 2L, 3L)
            val shape = longArrayOf(1L, fakeIds.size.toLong())

            val inputTensor = OnnxTensor.createTensor(env, fakeIds, shape)

            val outputs = session.run(
                mapOf("input_ids" to inputTensor) // 실제 모델 I/O 이름에 맞춰야 함
            )

            // 출력 해석도 모델 구조에 맞게 구현 필요
            // 여기서는 TODO
            "TODO: ONNX LLM 호출 결과"
        }
}