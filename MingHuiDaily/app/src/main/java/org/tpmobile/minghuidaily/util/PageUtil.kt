package org.tpmobile.minghuidaily.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import org.jsoup.select.Elements
import org.tpmobile.minghuidaily.MyApp
import org.tpmobile.minghuidaily.data.TaskInfo
import org.tpmobile.minghuidaily.util.ktx.toFilePath
import java.io.File
import java.util.Date
import kotlin.time.TimeSource

object PageUtil {
    private const val TAG = "PageUtil"

    suspend fun generateStyleCss(
        zoomScale: Int = 100,
        nightTheme: Boolean = false
    ): TaskInfo = withContext(Dispatchers.IO) {
        val metrics = DisplayUtil.getDisplayMetrics(MyApp.appContext)
        val maxWidth = (metrics.widthPixels * 9 / 10 / metrics.density).toInt()
        Logger.i(TAG, "generateStyleCss: maxWidth = $maxWidth")

        var bgColor = "#FFFFFF"
        var textColor = "#000000"
        var aLinkColor = "#0000FF"
        var fontTextColor = "#0000FF"
        var aGoTopBorderColor = "#0000FF"
        var aGoTopBgR = 219
        var aGoTopBgG = 234
        var aGoTopBgB = 254

        if (nightTheme) {
            bgColor = "#000000"
            textColor = "#FFFFFF"
            aLinkColor = "#FFD700"
            fontTextColor = "#FFA500"
            aGoTopBorderColor = "#FFD700"
            aGoTopBgR = 254
            aGoTopBgG = 249
            aGoTopBgB = 194
        }

        val pattern = """
         :root {
            --zoom-scale:${zoomScale}%;
            --bg-color:$bgColor;
            --text-color:$textColor;
            --a-link-color:$aLinkColor;
            --a-visited-color:$aLinkColor;
            --a-hover-color:$aLinkColor;
            --a-active-color:$aLinkColor;
            --font-text-color:$fontTextColor;
            --a-go-top-border-color:$aGoTopBorderColor;
            --a-go-top-bg-color-r:$aGoTopBgR;
            --a-go-top-bg-color-g:$aGoTopBgG;
            --a-go-top-bg-color-b:$aGoTopBgB;
        }
        
        .text-color {
            color:var(--text-color);
        }
        .font-text-color {
            color:var(--font-text-color);
        }
        
        head{
            font-size:var(--zoom-scale);
        }
        body{
            background-color:var(--bg-color);
            color:var(--text-color);
            font-size:var(--zoom-scale);
        }
        table,td,th{
            max-width:${maxWidth}px;
            font-size:0.6rem;
        }
        img,video,iframe{
            max-width:${maxWidth}px;
            width:expression(this.width>${maxWidth}px:this.width);
        }
        a,p{
            word-wrap:break-word;
            word-break:break-all;
        }
        
        a:link {
          color:var(--a-link-color);
          background-color:transparent;
          text-decoration:underline;
        }
        a:visited {
          color:var(--a-visited-color);
          background-color:transparent;
          text-decoration:none;
        }        
        .a_go_top {
          float:left;
          position:fixed;
          right:0;
          bottom:0;
          border:1px solid var(--a-go-top-border-color);
          border-radius:6px;
          padding:5px;
          margin:16px 16px 36px;      
          background:rgba(var(--a-go-top-bg-color-r), var(--a-go-top-bg-color-g), var(--a-go-top-bg-color-b), 0.5);
        }
    """.trimIndent()

        try {
            val cssFile =
                File("${MyApp.appContext.filesDir.absolutePath + File.separator}style.css")
            cssFile.writeText(pattern)
            Logger.i(TAG, "写入css成功")
            return@withContext TaskInfo(
                TaskInfo.TASK_NAME_MODIFY_CSS_FILE,
                "修改样式文件成功",
                100,
                "",
                false
            )
        } catch (e: Exception) {
            Logger.i(TAG, "写入css失败，error=${e.message}")
            return@withContext TaskInfo(
                TaskInfo.TASK_NAME_MODIFY_CSS_FILE,
                "",
                -1,
                "修改样式文件失败，error=${e.message}",
                true
            )
        }
    }

    /**
     * @param date 如：2026-1-1
     * @param zipFilePath 实际为 cacheFile 的路径，即下载和存放zip文件的目录
     * @param scaledWidth 屏幕宽度的90%
     * @param zoomScale 放大倍数，如：120%
     * @param density 屏幕密度的100倍，如 屏幕密度位3.0，此数则为 300
     * @param withPic 处理 带图片的每日文章
     *
     * 例如：
     *    val scaledWidth = 972
     *     val density = 300
     *     val maxWidth = 324
     *     val zoomScale = 108
     */
    suspend fun modifyHtml(
        date: Date,
        withPic: Boolean,
        scaledWidth: Int,
        density: Int,
        onProgress: (Int, String) -> Unit,
    ): Result<Boolean> = withContext(Dispatchers.IO) {


        try {
            val timeSource = TimeSource.Monotonic
            val mark1 = timeSource.markNow()

            onProgress(0, "开始适配网页文件...")

            val file = MyApp.appContext.filesDir.absolutePath + File.separator + date.toFilePath(
                withPic,
                true
            )
            val newFile = MyApp.appContext.filesDir.absolutePath + File.separator + date.toFilePath(
                withPic,
                false
            )
            Logger.i(TAG, "file = $file")
            Logger.i(TAG, "newFile = $newFile")

            val maxWidth = scaledWidth * 100 / density //324

            //1.
            val htmlString = File(file).readText()
            val doc = Jsoup.parse(htmlString)

            //2.1 删除掉 <style>
            doc.select("style")?.remove()
            onProgress(1, "适配网页文件：完成第1步修改")

            //2.2 添加自定义 meta, style 以及 topAnchor
            //2.2.1
            var contentAppend = """
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <link rel="stylesheet" type="text/css" href="../style.css">
            """.trimIndent()
            doc.head().append(contentAppend)
            onProgress(2, "适配网页文件：完成第2步修改")
            //2.2.2 手机上会导致返回键无效
            //m1
            /*contentAppend = """
                <div class="a_go_top">
                    <a href="#topAnchor"><font size="3">回到顶部</font></a>
                </div>
            """.trimIndent()
            doc.body().apply {
                attr("id", "topAnchor")
                append(contentAppend)
            }*/
            //m2
            contentAppend = """
                <div class="a_go_top">
                    <a href="javascript:scrollTo(0,0)"><font size="3">回到顶部</font></a>
                </div>
            """.trimIndent()
            doc.body().apply {
                append(contentAppend)
            }
            onProgress(3, "适配网页文件：完成第3步修改")

            //2.3 全局处理
            //2.3.1 图片添加 loading = lazy,(2)font 的 color
            doc.select("img")?.forEach { img ->
                img.attr("loading", "lazy")
            }
            onProgress(8, "适配网页文件：完成第4步修改")
            //2.3.2 修正 font 的 color
            doc.select("font")?.forEach { font ->
                val fontColor = font.attr("color")
                if (fontColor != "#ff0000" && fontColor != "red") {
                    font.removeAttr("color")
                    font.attr("class", "font-text-color")
                }
            }
            Logger.i(TAG, "完成基本的修改")
            onProgress(10, "适配网页文件：完成第5步修改")

            //2.4 处理 table 里的图片 width
            var imgs: Elements
            var totalImgWidth = 0
            var imgWidth = 0
            var imgHeight = 0
            val tables = doc.select("table")
            val tableSize = tables.size
            Logger.i(TAG, "tables.size: ${tables.size}")
            tables?.forEachIndexed { index, table ->
                //调整img宽度，有两种情况，attr 有width，或者其 style里有width
                table.select("tr").forEach { tr ->
                    totalImgWidth = 0
                    imgs = tr.select("img")
                    var imgRatio = 0f
                    imgs.forEach { img ->
                        img.removeAttr("style")

                        val width = img.attr("width").toIntOrNull() ?: 0
                        val height = img.attr("height").toIntOrNull() ?: 0

                        imgWidth = width * density / 100
                        if (width > 0 && height > 0) {
                            imgRatio = height.toFloat() / width.toFloat()
                            if (imgWidth > scaledWidth) {
                                img.attr("width", "${maxWidth / imgs.size}")
                                img.attr("height", "${maxWidth / imgs.size * imgRatio}")
                                totalImgWidth += maxWidth / imgs.size
                            } else {
                                totalImgWidth += width
                            }
                        } else if (width > 0 && height == 0) {
                            if (imgWidth > scaledWidth) {
                                img.attr("width", "${maxWidth / imgs.size}")
                                img.attr("height", "auto")
                                totalImgWidth += maxWidth / imgs.size
                            } else {
                                totalImgWidth += width
                            }
                        } else {
                            if (imgs.size > 1) {
                                img.attr("width", "${maxWidth / imgs.size}")
                                img.attr("height", "auto")
                                totalImgWidth += maxWidth / imgs.size
                            }
                            //其它在 style 里有 max-width 来设定
                        }
                    }
                    if (totalImgWidth > scaledWidth) {
                        imgs.forEach { img ->
                            imgWidth = img.attr("width").toIntOrNull() ?: 0
                            imgHeight = img.attr("height").toIntOrNull() ?: 0
                            img.attr("width", "${imgWidth * scaledWidth / totalImgWidth}")
                            if (imgHeight > 0) {
                                img.attr("height", "${imgHeight * scaledWidth / totalImgWidth}")
                            }
                        }
                    }
                }
                //调整表宽
                val tableWidth = table.attr("width").toIntOrNull() ?: 0
                if (tableWidth * density / 100 > scaledWidth) {//imagewith=scaledWidth*100/density
                    table.attr("width", "${scaledWidth * 100 / density}px")
                }
                Logger.i(TAG, "正在处理table[${index + 1}/$tableSize] > img...")
                onProgress(10 + ((index + 1) * 70 / tableSize), "正在做第6步修改...")
            }
            Logger.i(TAG, "完成处理table>img")
            onProgress(90, "适配网页文件：完成第6步修改")

            //2.5 处理iframe,video
            val tags = listOf("iframe", "video")
            tags.forEach { tag ->
                var videoWidth = 0f
                var videoRatio = 0f
                doc.select(tag).forEach { video ->
                    val width = video.attr("width").toIntOrNull() ?: 0
                    val height = video.attr("height").toIntOrNull() ?: 0
                    videoWidth = width * density / 100f
                    if (width > 0 && height > 0) {
                        videoRatio = height / width.toFloat()
                        //newVideoWidth = (width*scaledWidth/videoWidth).toInt()
                        if (videoWidth > scaledWidth) {
                            video.attr("width", "$maxWidth")
                            video.attr("height", "${(maxWidth * videoRatio).toInt()}")
                        }
                    } else if (width > 0 && height == 0) {
                        if (videoWidth > scaledWidth) {
                            video.attr("width", "$maxWidth")
                            video.attr("height", "auto")
                        }
                    } else {
                        //其它在 style 里有 max-width 来设定
                        video.attr("height", "auto")
                    }
                    video.select("source").forEach { source ->
                        val src = source.attr("src")
                        println("source = $src")
                        if (src.startsWith("//")) {
                            source.attr("src", "https:$src")
                        } else if (src.indexOf("//") == -1) {
                            source.attr("src", "https://$src")
                        }
                    }
                }
            }
            Logger.i(TAG, "完成处理iframe,video")
            onProgress(95, "适配网页文件：完成第7步修改")

            //2.6 保存文件
            // 设置输出选项：禁用缩进，使用XHTML格式
            doc.outputSettings().prettyPrint(false)
            File(newFile).writeText(doc.html())
            Logger.i(TAG, "保存网页文件成功")
            onProgress(100, "保存网页文件成功")

            val mark2 = timeSource.markNow()
            Logger.i(TAG, "处理时长：${mark2 - mark1}")

            return@withContext Result.success(true)
        } catch (e: Exception) {
            Logger.e(TAG, "适配网页文件失败，详情：${e.message}")
            return@withContext Result.failure(e)
        }

    }

    /**
     * @return 2025-5-13 或 2025-5-13-t
     */
    fun getBaseUrl(dateString: String, withPic: Boolean = true): String {
        /*var dateString = "2025-5-13"
        dateString="2026-1-19"
        val withPic = true*/
        return dateString + (if (withPic) "-t" else "")
    }

    /**
     * @return 2025-5-13 或 2025-5-13-t
     */
    fun getBaseUrl(date: Date, withPic: Boolean = true): String {
        /*var dateString = "2025-5-13"
        dateString="2026-1-19"
        val withPic = true*/
        val dateString = DateUtils.date2String(date, DateUtils.SDF_DATE_ONLY_EN)
        return dateString + (if (withPic) "-t" else "")
    }

    /**
     * @return 2026/5/13/2025-5-13-t/2025-5-13-t.html
     */
    fun dateToFilePathPart(
        date: Date,
        withPic: Boolean = true,
        originalFile: Boolean = true
    ): String {
        /*var dateString = "2025-5-13"
        dateString="2026-1-19"
        val withPic = true*/

        val path1 = DateUtils.date2String(date, DateUtils.SDF_URL_LINK)
        val dateString = DateUtils.date2String(date, DateUtils.SDF_DATE_ONLY_EN)
        val path2 = dateString + (if (withPic) "-t" else "")
        return path1 + path2 + File.separator + path2 + (if (originalFile) ".html" else "-new.html")
    }

    fun dateToFileName(date: Date, withPic: Boolean = true): String {
        val dateString = DateUtils.date2String(date,DateUtils.SDF_DATE_ONLY_EN)
        return dateString + (if (withPic) "-t" else "")
    }

    fun clean(element: Element) {
        element.childNodes().forEach { node ->
            when (node) {
                is TextNode -> {
                    if (node.isBlank) {
                        node.remove()
                    }
                }

                is Element -> {
                    clean(node)
                }
            }
        }
    }

}
