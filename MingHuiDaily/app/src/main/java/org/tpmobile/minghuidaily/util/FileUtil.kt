package org.tpmobile.minghuidaily.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tpmobile.minghuidaily.MyApp
import java.io.File

object FileUtil {
    private val TAG = "FileUtil"

    suspend fun clearFiles(
        files: List<File>,
        onProgress: ((Int, String) -> Unit)? = null
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        onProgress?.invoke(0, "开始清理历史存档...")
        var index = 0
        try {
            files.filter { it.exists() && it.isDirectory }.forEach { file ->
                file.deleteRecursively()
                index++
                onProgress?.invoke(index * 100 / files.size, "正在清理历史存档...")
            }
            onProgress?.invoke(100, "成功清理历史存档")
            return@withContext Result.success(true)
        } catch (e: Exception) {
            Logger.e(TAG, "clearFiles error: ${e.message}")
            onProgress?.invoke(-1, "清理历史存档失败，详情${e.message ?: "失败原因不详"}")
            return@withContext Result.failure(e)
        }
    }

    suspend fun clearAllFiles(): Boolean = withContext(Dispatchers.IO) {
        try {
            MyApp.appContext.filesDir.listFiles { it.isDirectory }?.forEach { file ->
                file.deleteRecursively()
            }
            return@withContext true
        } catch (e: Exception) {
            Logger.e(TAG, "clearFiles error: ${e.message}")
            return@withContext false
        }
    }

}
