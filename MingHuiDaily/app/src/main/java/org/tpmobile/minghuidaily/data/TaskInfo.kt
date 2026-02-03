package org.tpmobile.minghuidaily.data

data class TaskInfo(
    val taskName: String,
    val taskInfo: String,
    val progress: Int,
    val errorInfo: String,
    val isError: Boolean
) {
    companion object {
        const val TASK_NAME_DOWNLOAD = "下载"
        const val TASK_NAME_UNZIP = "解压"
        const val TASK_NAME_MODIFY_HTML_FILE = "适配网页文件"
        const val TASK_NAME_MODIFY_CSS_FILE = "修改样式文件"
        const val TASK_NAME_LOAD_URL = "加载网页"
        const val TASK_NAME_CLEAN_HISTORY = "清理存档"
    }
}
