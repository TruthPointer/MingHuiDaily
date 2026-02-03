package org.tpmobile.minghuidaily.util


import androidx.webkit.ProxyConfig
import androidx.webkit.ProxyController
import androidx.webkit.WebViewFeature
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import org.tpmobile.minghuidaily.MyApp
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.Proxy
import java.net.URL
import java.util.Date

object HttpUtil {
    private val TAG = "HttpUtil"

    /////////////////////////////////////
    //For WebView
    fun isWebViewSupportProxyOverrideFeature() =
        WebViewFeature.isFeatureSupported(WebViewFeature.PROXY_OVERRIDE)

    fun setProxyForWebView(
        proxyHost: String = MyApp.proxyHost,
        proxyPort: Int = MyApp.proxyPort
    ) {
        if (!MyApp.USE_PROXY) return
        if (WebViewFeature.isFeatureSupported(WebViewFeature.PROXY_OVERRIDE)) {
            Logger.i("[NET]", "可以设置代理……");
            val proxyConfig = ProxyConfig.Builder()
                .addProxyRule("http://$proxyHost:$proxyPort")
                .addProxyRule("https://$proxyHost:$proxyPort")
                .addProxyRule("socks://$proxyHost:$proxyPort")
                //.addDirect() //直连
                .build();
            /*网站直连
            这种方式是应用内的 WebView 全局代理，如果需要绕过某些网站，可以指定，或者使用通配符进行绕过:
            val proxyConfig = ProxyConfig.Builder()
                .addProxyRule("http://192.168.0.26:8100")
                .addBypassRule("www.google.com")
                .addBypassRule("www.bing.com")
                .addBypassRule("*.bing.com")
                .addDirect().build()*/
            ProxyController.getInstance().setProxyOverride(
                proxyConfig, { executor ->
                    {
                        //Logger.i("[NET]", "代理设置完成");
                    }
                },
                {
                    Logger.i("[NET]", "WebView代理已设置");
                })

        }
    }

    fun clearProxyForWebView() {
        if (WebViewFeature.isFeatureSupported(WebViewFeature.PROXY_OVERRIDE)) {
            Logger.i("[NET]", "可以取消代理……")
            ProxyController.getInstance().clearProxyOverride(
                { executor ->
                    {
                        //Logger.i("[NET]", "代理取消完成");
                    }
                },
                {
                    Logger.i("[NET]", "WebView代理已取消");
                })
        }
    }

    fun clearWebViewCache() {

    }

    //////////////////////////

    /**
     * https://m.minghui.org/mh/articles/2026/1/19/2026-1-19.zip
     * https://m.minghui.org/mh/articles/2026/1/19/2026-1-19-t.zip
     * https://m.minghui.org/mh/articles/2026/1/19/2026-1-19.txt
     */
    fun getDownloadUrl(date: Date, withPic: Boolean): String {
        val urlPart = DateUtils.format(date, DateUtils.SDF_URL_LINK) // yyyy/M/d
        var fileName = DateUtils.format(date, DateUtils.SDF_DATE_ONLY_EN) // yyyy-M-d
        if (withPic)
            fileName += "-t"
        return "https://m.minghui.org/mh/articles/$urlPart$fileName.zip"
    }

    suspend fun downloadFile(
        urlString: String,
        proxy: Proxy = MyApp.proxy,
        onProgress: ((Int, String) -> Unit)? = null
    ): String = withContext(Dispatchers.IO) {
        // 打开连接
        try {
            onProgress?.invoke(0, "开始下载...")

            val connection = URL(urlString).openConnection(proxy) as HttpURLConnection
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.requestMethod = "GET"
            connection.connect()

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                // 转到 catch{} 部分处理了，onProgress?.invoke(-1, "下载失败，详情：服务器响应异常")
                throw Exception("服务器响应错误: ${connection.responseCode} ${connection.responseMessage}")
            }

            // 准备文件路径
            val fileLength = connection.contentLength
            val fileName = File(urlString).name//"download_${System.currentTimeMillis()}.dat"
            val file = File(MyApp.appContext.filesDir, fileName)

            // 流操作
            connection.inputStream.use { input ->
                FileOutputStream(file).use { output ->
                    val data = ByteArray(8192) // 8KB buffer
                    var total: Long = 0
                    var count: Int

                    while (input.read(data).also { count = it } != -1 && isActive) {
                        total += count
                        output.write(data, 0, count)

                        // 计算进度并回调
                        if (fileLength > 0) {
                            val progress = (total * 100 / fileLength).toInt()
                            val detail = "${total / 1024} KB / ${fileLength / 1024} KB"
                            onProgress?.invoke(
                                progress,
                                if (progress < 100) "下载进度：$detail" else "下载成功"
                            )
                        } else {
                            // 如果服务器没返回长度，只显示已下载大小
                            onProgress?.invoke(-2, "已下载：${total / 1024} KB")
                        }
                    }
                }
            }
            return@withContext file.absolutePath
        } catch (e: Exception) {
            Logger.e(TAG, e.message ?: "错误原因不详")
            onProgress?.invoke(-1, "下载失败，详情：${e.message ?: "原因不详"}")
            return@withContext ""
        }

    }


}