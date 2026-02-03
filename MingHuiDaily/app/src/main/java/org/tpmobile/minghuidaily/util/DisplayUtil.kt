package org.tpmobile.minghuidaily.util

import android.app.UiModeManager
import android.content.Context
import android.util.DisplayMetrics
import androidx.appcompat.app.AppCompatDelegate
import org.tpmobile.minghuidaily.MyApp


object DisplayUtil {
    /**
     * 获取 显示信息
     */
    fun getDisplayMetrics(context: Context): DisplayMetrics {
        return context.resources.displayMetrics
    }

    fun getScreenWidth(context: Context): Int {
        return getDisplayMetrics(context).widthPixels
    }

    fun getScreenWidth(): Int {
        return getDisplayMetrics(MyApp.appContext).widthPixels
    }

    /**
     * 打印 显示信息
     */
    fun printDisplayInfo(context: Context): DisplayMetrics {
        val dm = getDisplayMetrics(context)
        val sb = StringBuilder()
        sb.append("_______  显示信息:  ")
        sb.append("\ndensity         :").append(dm.density)
        sb.append("\ndensityDpi      :").append(dm.densityDpi)
        sb.append("\nheightPixels    :").append(dm.heightPixels)
        sb.append("\nwidthPixels     :").append(dm.widthPixels)
        //sb.append("\nscaledDensity   :").append(dm.scaledDensity)
        sb.append("\nxdpi            :").append(dm.xdpi)
        sb.append("\nydpi            :").append(dm.ydpi)
        //Log.sopln(sb.toString())
        return dm
    }

    ////////////
    fun dip2px(context: Context, dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    fun px2dip(context: Context, pxValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (pxValue / scale + 0.5f).toInt()
    }

    /////////////////

}
