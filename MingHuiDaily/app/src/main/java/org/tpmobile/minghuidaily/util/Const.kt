package org.tpmobile.minghuidaily.util


const val DEBUG_APP = true

const val SHOW_DETAILED_RUNNING_INFO = false
const val PREF_PROXY_PORT = "pref_proxy_port"
const val PROXY_PORT_HOST = "127.0.0.1"
const val PROXY_PORT_FREEGATE = 8590
const val PROXY_PORT_WUJIE = 19966

const val PREF_CURRENT_BROWSING_DATE = "pref_current_browsing_date"
const val PREF_THEME_NIGHT = "pref_theme_night"
const val ENABLE_SWITCH_THEME = true

const val PREF_FONT_ZOOM_SCALE = "pref_font_zoom_scale"
const val PREF_ZIP_WITH_PIC = "pref_zip_with_pic"

const val MAX_FILE_SIZE_FOR_TIP = 800 * 1024 //800KB

val LIST_TYPE_OF_PICTURE = arrayListOf("png", "jpg", "bmp", "gif")

const val ANDROID_ASSETS = "file:///android_asset/"

/////////////////////////////////////////////////
const val CONNECT_TIMEOUT = 30L //秒
const val READ_TIMEOUT = 30L //秒
const val WRITE_TIMEOUT = 30L //秒
const val DOWNLOAD_TIMEOUT = 10L

/////////////////////////////////////////////////
val COLOR_GOLDEN = "#FFD700"
val COLOR_ORANGE = "#FFA500"
val COLOR_BLUE = "#0000FF"
val COLOR_DARKBLUE = "#FFA500"

/////////////////////////////////////////////////
enum class TimeUnit {
    MSEC,
    SEC,
    MIN,
    HOUR,
    DAY
}

/////////////////////////////////////////////////
/******************** 时间相关常量 (与毫秒的倍数) */
val MSEC = 1
val SEC = 1000
val MIN = 60000
val HOUR = 3600000
val DAY = 86400000

/******************** 存储相关常量(与Byte的倍数)  */
val BYTE = 1
val KB = 1024
val MB = 1048576
val GB = 1073741824
