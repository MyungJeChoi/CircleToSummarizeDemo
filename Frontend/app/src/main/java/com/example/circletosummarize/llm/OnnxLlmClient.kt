package com.example.circletosummarize.llm

import android.app.Application
import android.util.Log
import ai.onnxruntime.genai.Generator
import ai.onnxruntime.genai.GeneratorParams
import ai.onnxruntime.genai.Model
import ai.onnxruntime.genai.Tokenizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class OnnxLlmClient(
    private val app: Application
) : LlmClient {

    companion object {
        private const val TAG = "OnnxLlmClient"
        private const val MODEL_SUBDIR = "cpu_and_mobile/cpu-int4-rtn-block-32-acc-level-4"
        private const val MAX_LENGTH_TOKENS = 768.0
    }

    @Volatile
    private var model: Model? = null

    @Volatile
    private var tokenizer: Tokenizer? = null

    override suspend fun summarize(text: String, lang: String): String =
        withContext(Dispatchers.Default) {
            if (text.isBlank()) return@withContext ""

            val prompt = buildPrompt(text, lang)

            try {
                val (m, t) = getOrCreateModelAndTokenizer()

                val params = GeneratorParams(m)
                params.setSearchOption("max_length", MAX_LENGTH_TOKENS)
                params.setSearchOption("do_sample", false)
                params.setSearchOption("top_p", 0.9)
                params.setSearchOption("temperature", 0.3)

                val generator = Generator(m, params)

                val sequences = t.encode(prompt)
                generator.appendTokenSequences(sequences)
                while (!generator.isDone) {
                    generator.generateNextToken()
                }
                val outputIds = generator.getSequence(0L)
                val fullText = t.decode(outputIds)

                postProcessSummary(prompt, fullText)
            } catch (e: Exception) {
                Log.e(TAG, "LLM summarize error: ${e.message}", e)
                fallbackSummary(text)
            }
        }

    private fun getOrCreateModelAndTokenizer(): Pair<Model, Tokenizer> {
        val existingModel = model
        val existingTokenizer = tokenizer
        if (existingModel != null && existingTokenizer != null) {
            return existingModel to existingTokenizer
        }

        synchronized(this) {
            val againModel = model
            val againTokenizer = tokenizer
            if (againModel != null && againTokenizer != null) {
                return againModel to againTokenizer
            }

            // assets → filesDir 복사
            val modelRoot: File = ModelAssetManager.ensureModelOnDisk(app)
            val modelDir = File(modelRoot, MODEL_SUBDIR)
            if (!modelDir.exists()) {
                throw IllegalStateException("Model directory not found: ${modelDir.absolutePath}")
            }

            val m = Model(modelDir.absolutePath)
            val t = Tokenizer(m)

            model = m
            tokenizer = t
            return m to t
        }
    }

    private fun buildPrompt(text: String, lang: String): String {
        return if (lang.lowercase().startsWith("ko")) {
            "다음 한국어 문서를 한 줄로 간단히 요약해줘.\n\n$text\n\n요약:"
        } else {
            "Summarize the following document into a single concise sentence.\n\n$text\n\nSummary:"
        }
    }

    private fun postProcessSummary(prompt: String, fullOutput: String): String {
        // 프롬프트까지 함께 생성되었을 가능성이 있으므로 제거
        val cleaned = fullOutput.replace(prompt, "")
        return cleaned
            .replace("\r", " ")
            .replace("\n", " ")
            .replace("\\s+".toRegex(), " ")
            .trim()
    }

    private fun fallbackSummary(text: String, maxChars: Int = 150): String {
        val flat = text
            .replace("\n", " ")
            .replace("\\s+".toRegex(), " ")
            .trim()
        return if (flat.length <= maxChars) flat else flat.take(maxChars) + "…"
    }
}
