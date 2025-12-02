package com.example.circletosummarize.llm

import android.content.Context
import java.io.File

/**
 * assets/models/phi3-mini-4k/**/* 를
 * context.filesDir/models/phi3-mini-4k 로 복사해둔다.
 * (이미 있으면 스킵)
 */
object ModelAssetManager {

    private const val ASSETS_MODEL_ROOT = "models"
    private const val MODEL_DIR_NAME = "phi3-mini-4k" // assets/models/phi3-mini-4k

    fun ensureModelOnDisk(context: Context): File {
        val targetRoot = File(context.filesDir, ASSETS_MODEL_ROOT)
        val targetModelDir = File(targetRoot, MODEL_DIR_NAME)

        if (!targetModelDir.exists()) {
            targetModelDir.mkdirs()
            copyAssetDir(context, "$ASSETS_MODEL_ROOT/$MODEL_DIR_NAME", targetModelDir)
        }

        return targetModelDir
    }

    private fun copyAssetDir(context: Context, assetDirPath: String, targetDir: File) {
        val assetManager = context.assets
        val fileList = assetManager.list(assetDirPath) ?: return

        for (name in fileList) {
            val assetPath = if (assetDirPath.isEmpty()) name else "$assetDirPath/$name"
            val outFile = File(targetDir, name)
            val children = assetManager.list(assetPath)

            if (children.isNullOrEmpty()) {
                // 파일
                assetManager.open(assetPath).use { input ->
                    outFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            } else {
                // 디렉터리
                if (!outFile.exists()) outFile.mkdirs()
                copyAssetDir(context, assetPath, outFile)
            }
        }
    }
}
