package org.tpmobile.minghuidaily.util


import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * G: Era 标志符
 * w: 年中的周数
 * W: 月份中的周数
 * d: 月份中的天数
 * D: 年中的天数
 * F: 月份中的星期
 * E: 星期中的天数
 * a: Am/pm 标记
 * k: 一天中的小时数（0-23）
 * K: am/pm 中的小时数（0-11）
 * h: am/pm 中的小时数（1-12）
 * H: 一天中的小时数（1-24）
 * z: 时区
 * Z: RFC 822 时区
 * s: 秒
 * S: 毫秒
 * HH:mm    15:44
 * h:mm a    3:44 下午
 * HH:mm z    15:44 CST
 * HH:mm Z    15:44 +0800
 * HH:mm zzzz    15:44 中国标准时间
 * HH:mm:ss    15:44:40
 * yyyy-MM-dd    2016-08-12
 * yyyy-MM-dd HH:mm    2016-08-12 15:44
 * yyyy-MM-dd HH:mm:ss    2016-08-12 15:44:40
 * yyyy-MM-dd HH:mm:ss zzzz    2016-08-12 15:44:40 中国标准时间
 * EEEE yyyy-MM-dd HH:mm:ss zzzz    星期五 2016-08-12 15:44:40 中国标准时间
 * yyyy-MM-dd HH:mm:ss.SSSZ    2016-08-12 15:44:40.461+0800
 * yyyy-MM-dd'T'HH:mm:ss.SSSZ    2016-08-12T15:44:40.461+0800
 * yyyy.MM.dd G 'at' HH:mm:ss z    2016.08.12 公元 at 15:44:40 CST
 * K:mm a    3:44 下午
 * EEE, MMM d, ''yy    星期五, 八月 12, '16
 * hh 'o''clock' a, zzzz    03 o'clock 下午, 中国标准时间
 * yyyyy.MMMMM.dd GGG hh:mm aaa    02016.八月.12 公元 03:44 下午
 * EEE, d MMM yyyy HH:mm:ss Z    星期五, 12 八月 2016 15:44:40 +0800
 * yyMMddHHmmssZ    160812154440+0800
 * yyyy-MM-dd'T'HH:mm:ss.SSSZ    2016-08-12T15:44:40.461+0800
 * EEEE 'DATE('yyyy-MM-dd')' 'TIME('HH:mm:ss')' zzzz    星期五 DATE(2016-08-12) TIME(15:44:40) 中国标准时间
 *
 */
object DateUtils {

    val SDF_DATE_ONLY_CN = SimpleDateFormat("yyyy年MM月dd日", Locale.US)
    val SDF_DATE_FOR_HISTORY = SimpleDateFormat("yyyy年MM月dd", Locale.US)
    val SDF_DATE_ONLY_EN = SimpleDateFormat("yyyy-M-d", Locale.US)
    val SDF_URL_LINK = SimpleDateFormat("yyyy/M/d/", Locale.US)


    fun parse(date: String, sdfString: String): Date? {
        val dateFormat = SimpleDateFormat(sdfString, Locale.US)
        try {
            return dateFormat.parse(date)
        } catch (e: ParseException) {
            e.printStackTrace()
            return null
        }
    }

    fun parse(date: String, sdf: SimpleDateFormat = SDF_DATE_ONLY_EN): Date? {
        if (date.isEmpty())
            return null
        return try {
            sdf.parse(date)
        } catch (e: ParseException) {
            e.printStackTrace()
            null
        }

    }

    fun format(date: Date, sdfString: String): String {
        val dateFormat = SimpleDateFormat(sdfString, Locale.US)
        return dateFormat.format(date)
    }

    fun format(date: Date, sdf: SimpleDateFormat = SDF_DATE_ONLY_EN): String {
        return sdf.format(date)
    }

    fun dateStringToDate(dateString: String, sdf: SimpleDateFormat = SDF_DATE_ONLY_EN): Date {
        return if (dateString.isEmpty())
            Date()
        else
            parse(dateString, sdf) ?: Date()
    }

    fun string2Milliseconds(dateString: String, sdf: SimpleDateFormat = SDF_DATE_ONLY_EN): Long {
        try {
            return sdf.parse(dateString)?.time ?: 0
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return -1
    }

    fun date2String(date: Date, sdf: SimpleDateFormat = SDF_DATE_ONLY_EN): String {
        return sdf.format(date)
    }

    fun currentDate2String(sdf: SimpleDateFormat = SDF_DATE_ONLY_EN): String {
        return sdf.format(Date())
    }

    /***
     * 使用 "yyyy年MM月dd日"
     *
     * @return
     */
    fun currentDate2String(): String {
        return SDF_DATE_ONLY_CN.format(Date())
    }

    private fun milliseconds2Unit(milliseconds: Long, unit: TimeUnit): Long {
        return when (unit) {
            TimeUnit.MSEC -> milliseconds / MSEC
            TimeUnit.SEC -> milliseconds / SEC
            TimeUnit.MIN -> milliseconds / MIN
            TimeUnit.HOUR -> milliseconds / HOUR
            TimeUnit.DAY -> milliseconds / DAY
        }
    }

    fun convertDateStringOfTodayNews(dateString: String): Date? {
        if (dateString.isEmpty())
            return null
        val d1 = parse(dateString, SDF_DATE_ONLY_CN) ?: return null
        return addNDay(d1, 1)
    }

    /**
     *
     * @param date
     * @param amount >0 增加;<0 减去
     * @return
     */
    fun addNDay(date: Date?, amount: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date ?: Date()
        calendar.add(Calendar.DATE, amount)     // 加一天
        return calendar.time
    }

    /**
     *
     */
    fun subNDayFromCurrent(amount: Int): Date {
        val calendar = Calendar.getInstance()
        //calendar.time = date
        calendar.add(Calendar.DATE, amount)
        return calendar.time
    }

    fun getNDayStringFromCurrent(timeSpan: Int, sdf: SimpleDateFormat = SDF_DATE_ONLY_EN): String {
        return format(subNDayFromCurrent(timeSpan), SDF_DATE_ONLY_CN)
    }

    fun isValid(dateString: String?, sdf: SimpleDateFormat = SDF_DATE_ONLY_EN): Boolean {
        if (dateString.isNullOrEmpty())
            return false
        return parse(dateString, sdf) != null
    }

}
