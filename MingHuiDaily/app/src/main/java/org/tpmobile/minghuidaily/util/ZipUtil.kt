package org.tpmobile.minghuidaily.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.progress.ProgressMonitor
import java.io.File

object ZipUtil {
    private const val TAG = "ZipUtil"

    /**
     * 安全解压加密ZIP文件
     * @param zipFilePath ZIP文件路径
     * @param destDirectory 解压目标目录
     * @param password 解密密码
     */
    fun extractZipFile(
        zipFilePath: String,
        destDirectory: String,
        password: String = ""
    ) {
        val zipFile = ZipFile(zipFilePath)//, password.toCharArray())
        zipFile.extractAll(destDirectory)
    }

    suspend fun unZipFileWithProgress(
        zipFile: File,
        destFilePath: String,
        password: String = "",
        onProgress: ((progress: Int, info: String) -> Unit)? = null
    ): Boolean = withContext(Dispatchers.IO) {
        var job: Job? = null
        try {
            onProgress?.invoke(0, "开始解压...")

            val zFile = ZipFile(zipFile)
            //zFile.charset = Charset.forName(("GBK"))
            if (!zFile.isValidZipFile()) { //
                onProgress?.invoke(-1, "压缩文件无效，解压失败！")
                return@withContext false
            }
            val destDir = File(destFilePath)
            if (destDir.isDirectory() && !destDir.exists()) {
                destDir.mkdir()
            }
            if (zFile.isEncrypted() && password.isNotEmpty()) {
                zFile.setPassword(password.toCharArray()) // 设置解压密码
            }

            job = launch(Dispatchers.IO) {
                monitorUnzipProgress(zFile, onProgress)
            }
            zFile.isRunInThread = true //true 在子线程中进行解压 , false主线程中解压
            zFile.extractAll(destFilePath)

            return@withContext true
        } catch (e: Exception) {
            Logger.e(TAG, "Error: ${e.message}")
            job?.cancel()
            onProgress?.invoke(-1, "解压失败，详情：${e.message ?: "原因不详"}")
            e.printStackTrace()
            return@withContext false
        }
    }

    suspend fun monitorUnzipProgress(
        zFile: ZipFile,
        onProgress: ((progress: Int, info: String) -> Unit)? = null
    ) = withContext(Dispatchers.IO) {
        val progressMonitor: ProgressMonitor = zFile.progressMonitor
        var percentDone = 0
        onProgress?.invoke(0, "开始解压...")
        while (isActive) {
            // 每隔50ms,发送一个解压进度出去
            delay(50)
            percentDone = progressMonitor.percentDone
            onProgress?.invoke(percentDone, if (percentDone < 100) "正在解压中..." else "解压成功")
            println("PERCENT: $percentDone")
            if (percentDone >= 100) {
                break
            }
        }
        //onProgress?.invoke(100, "解压成功")
    }
}