package org.tpmobile.minghuidaily.util.ktx

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.Spannable
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.BackgroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.text.style.TypefaceSpan
import android.text.style.URLSpan
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import org.tpmobile.minghuidaily.MyApp
import org.tpmobile.minghuidaily.R
import org.tpmobile.minghuidaily.util.DateUtils
import java.io.File
import java.text.DecimalFormat
import java.util.Date

////////////////////////////////////////
//Int, Long
////////////////////////////////////////
fun Int.toColor(context: Context, theme: Resources.Theme): Int =
    ResourcesCompat.getColor(context.resources, this, theme)

fun Int.toDrawable(context: Context, theme: Resources.Theme): Drawable? =
    ResourcesCompat.getDrawable(context.resources, this, theme)

//-----------------------------------------------------------------------------------------------
fun resIdToRealString(@StringRes resId: Int): String {
    return MyApp.appContext.getString(resId)
}

fun resIdToRealString(@StringRes resId: Int, formatArgs: Array<String>): String {
    return MyApp.appContext.getString(resId, *formatArgs)
}
//-----------------------------------------------------------------------------------------------

//
fun Int.toHumanReadableSize(): String {
    return this.toLong().toHumanReadableSize()
}

fun Int.toHumanReadableSpeed(): String {
    return this.toLong().toHumanReadableSpeed()
}

fun Int.dp2Px(): Int {
    val scale = MyApp.appContext.resources.displayMetrics.density
    return (this * scale + 0.5f).toInt()
}

fun Int.px2Dp(): Int {
    val scale = MyApp.appContext.resources.displayMetrics.density
    return (this / scale + 0.5f).toInt()
}

fun Long.toHumanReadableSize(): String {
    val kb = this / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    val tb = gb / 1024.0

    val df1 = DecimalFormat("0.00")
    val df2 = DecimalFormat("0")
    return when {
        tb > 1 -> df1.format(tb).plus("TB")
        gb > 1 -> df1.format(gb).plus("GB")
        mb > 1 -> df1.format(mb).plus("MB")
        kb > 1 -> df2.format(kb).plus("KB")
        else -> this.toString().plus("B")
    }
}

fun Long.toHumanReadableSpeed(): String {
    val kb = this / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    val tb = gb / 1024.0

    val df1 = DecimalFormat("0.00")
    val df2 = DecimalFormat("0")
    return when {
        tb > 1 -> df1.format(tb).plus("TB/S")
        gb > 1 -> df1.format(gb).plus("GB/S")
        mb > 1 -> df1.format(mb).plus("MB/S")
        else -> df2.format(kb).plus("KB/S")
    }
}

////////////////////////////////////////
//ByteArray
////////////////////////////////////////
fun ByteArray.toHex(): String {
    return joinToString("") { "%02x".format(it) }
}

////////////////////////////////////////
//Boolean
////////////////////////////////////////
fun Boolean.toInt01(): Int = if (this) 1 else 0

////////////////////////////////////////
//View
////////////////////////////////////////
fun View.toggleVisibility() {
    if (this.visibility == View.GONE)
        this.visibility = View.VISIBLE
    else
        this.visibility = View.GONE
}

////////////////////////////////////////
//TextView
////////////////////////////////////////
//1.
fun TextView.htmlText(value: String) {
    text = HtmlCompat.fromHtml(value, HtmlCompat.FROM_HTML_MODE_COMPACT)
}

fun TextView.setUnderlineSpan(setOrClearUnderline: Boolean) {
    paint.flags = if (setOrClearUnderline)
        Paint.UNDERLINE_TEXT_FLAG
    else
        0
}

////////////////////////////////////////
//Context <<<基礎部分>>>
////////////////////////////////////////
//1.
fun Context?.resIdToString(@StringRes resId: Int): String = this?.getString(resId) ?: ""

//20220420
fun Context?.resIdToString(@StringRes resId: Int, formatArgs: Array<String>): String =
    this?.getString(resId, *formatArgs) ?: ""

fun Context?.resIdToSpannableString(@StringRes resId: Int): SpannableString =
    SpannableString(this?.getString(resId) ?: "")

//2.Preference
fun Context.toSharedPreferenceByString(key: String, value: String) {
    PreferenceManager.getDefaultSharedPreferences(this).edit {
        putString(key, value)
    }
}

fun Context.fromSharedPreferenceByString(key: String, defaultValue: String) =
    PreferenceManager.getDefaultSharedPreferences(this)
        .getString(key, defaultValue)

fun Context.toSharedPreferenceByInt(key: String, value: Int) {
    PreferenceManager.getDefaultSharedPreferences(this).edit {
        putInt(key, value)
    }
}

fun Context.fromSharedPreferenceByInt(key: String, defaultValue: Int) =
    //使用 this.applicationContext 在Applicaton的attachBaseContext获取语言会为null，引发异常
    PreferenceManager.getDefaultSharedPreferences(this).getInt(key, defaultValue)

//3.
//4. Toast
//4.1 通用
fun Context?.toast(message: CharSequence, long: Boolean = true) {
    this?.let {
        Toast.makeText(
            this.applicationContext,
            message,
            if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
        ).apply { show() }
    }
}

fun Context?.toast(
    @StringRes messageId: Int,
    long: Boolean = false,
) {
    this?.toast(getString(messageId), long)
}

fun Context?.toast(
    @StringRes messageId: Int,
    formatArgs: Array<String>,
    long: Boolean = false,
) {
    this?.toast(getString(messageId, *formatArgs), long)
}

//
fun Context?.toastHtml(
    html: String,
    htmlMode: Int = HtmlCompat.FROM_HTML_MODE_COMPACT,
    long: Boolean = false
) {
    this?.let {
        Toast.makeText(
            this.applicationContext,
            HtmlCompat.fromHtml(html, htmlMode),
            if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
        ).apply { show() }
    }
}

fun Context?.toastHtml(
    @StringRes messageId: Int,
    htmlMode: Int = HtmlCompat.FROM_HTML_MODE_COMPACT,
    long: Boolean = false
) {
    this?.toastHtml(getString(messageId), htmlMode, long)
}


fun Context?.toastHtml(
    @StringRes messageId: Int,
    formatArgs: Array<String>,
    htmlMode: Int = HtmlCompat.FROM_HTML_MODE_COMPACT,
    long: Boolean = false
) {
    this?.toastHtml(getString(messageId, *formatArgs), htmlMode, long)
}

////////////////////////////////////////
//Activity 的 dialog
////////////////////////////////////////
//2. dialog的Show的判斷
fun Activity.canShow(): Boolean = !(isDestroyed || isFinishing)

fun Fragment.canShow(): Boolean =
    activity?.let {
        !(it.isDestroyed || it.isFinishing)
    } ?: false

//3. 对话框
//3.1
//3.1.1
fun Activity.dialogWith3Btn(
    titleId: CharSequence = getString(R.string.dialog_tip),
    message: CharSequence,
    povButtonName: CharSequence = getString(R.string.dialog_result_ok),
    povAction: () -> Unit,
    neuButtonName: CharSequence? = null,
    neuAction: () -> Unit,
    negButtonName: CharSequence? = getString(R.string.dialog_result_cancel),
    negAction: () -> Unit = {},
    cancellable: Boolean = false
) {
    if (!canShow())
        return
    val builder = AlertDialog.Builder(this/*, getThemeId()*/)
        .setTitle(titleId)
        .setMessage(message)
        .setPositiveButton(povButtonName) { _, _ ->
            povAction()
        }
    if (neuButtonName != null) {
        builder.setNeutralButton(neuButtonName) { _, _ ->
            neuAction()
        }
    }
    if (negButtonName != null) {
        builder.setNegativeButton(negButtonName) { _, _ ->
            negAction()
        }
    }
    builder.show().setCanceledOnTouchOutside(cancellable)
}

fun Activity.dialogWith3Btn(
    @StringRes titleId: Int = R.string.dialog_tip,
    @StringRes messageId: Int,
    @StringRes povButtonNameId: Int = R.string.dialog_result_ok,
    povAction: () -> Unit,
    @StringRes neuButtonNameId: Int? = null,
    neuAction: () -> Unit,
    @StringRes negButtonNameId: Int? = R.string.dialog_result_cancel,
    negAction: () -> Unit = {},
    cancellable: Boolean = false
) {
    dialogWith3Btn(
        getString(titleId), getString(messageId),
        getString(povButtonNameId), povAction,
        if (neuButtonNameId != null) getString(neuButtonNameId) else null, neuAction,
        if (negButtonNameId != null) getString(negButtonNameId) else null, negAction,
        cancellable
    )
}

fun Activity.dialogWith3Btn(
    @StringRes titleId: Int = R.string.dialog_tip,
    @StringRes messageId: Int, formatArgs: Array<String>,
    @StringRes povButtonNameId: Int = R.string.dialog_result_ok,
    povAction: () -> Unit,
    @StringRes neuButtonNameId: Int? = null,
    neuAction: () -> Unit,
    @StringRes negButtonNameId: Int? = R.string.dialog_result_cancel,
    negAction: () -> Unit = {},
    cancellable: Boolean = false
) {
    dialogWith3Btn(
        getString(titleId), getString(messageId, *formatArgs),
        getString(povButtonNameId), povAction,
        if (neuButtonNameId != null) getString(neuButtonNameId) else null, neuAction,
        if (negButtonNameId != null) getString(negButtonNameId) else null, negAction,
        cancellable
    )
}

//3.1.2
fun Activity.customDialogWith3Btn(
    title: String, message: String,
    povButtonName: CharSequence, povAction: () -> Unit,
    neuButtonName: CharSequence? = null, neuAction: (() -> Unit)? = null,
    negButtonName: CharSequence? = null, negAction: (() -> Unit)? = null,
    cancellable: Boolean = false
) {
    if (!canShow())
        return
    val view = layoutInflater.inflate(R.layout.dialog_show_text_info, null).apply {
        with(findViewById<TextView>(R.id.infoTxt)) {
            text = HtmlCompat.fromHtml(message, HtmlCompat.FROM_HTML_MODE_COMPACT)
            movementMethod = LinkMovementMethod.getInstance()
        }
    }
    val builder = AlertDialog.Builder(this/*, getThemeId()*/)
        .setTitle(title)
        .setView(view)
        .setPositiveButton(povButtonName) { _, _ ->
            povAction()
        }
    if (neuButtonName != null) {
        builder.setNeutralButton(neuButtonName) { _, _ ->
            neuAction?.invoke()
        }
    }
    if (negButtonName != null) {
        builder.setNegativeButton(negButtonName) { _, _ ->
            negAction?.invoke()
        }
    }
    builder.show().setCanceledOnTouchOutside(cancellable)
}

fun Activity.customDialogWith3Btn(
    @StringRes titleId: Int = R.string.dialog_tip,
    @StringRes messageId: Int,
    @StringRes povButtonNameId: Int = R.string.dialog_result_ok,
    povAction: () -> Unit,
    @StringRes neuButtonNameId: Int? = null,
    neuAction: (() -> Unit)? = null,
    @StringRes negButtonNameId: Int? = R.string.dialog_result_cancel,
    negAction: (() -> Unit)? = null,
    cancellable: Boolean = false
) {
    customDialogWith3Btn(
        getString(titleId), getString(messageId),
        getString(povButtonNameId), povAction,
        if (neuButtonNameId != null) getString(neuButtonNameId) else null, neuAction,
        if (negButtonNameId != null) getString(negButtonNameId) else null, negAction,
        cancellable
    )
}

fun Activity.customDialogWith3Btn(
    @StringRes titleId: Int = R.string.dialog_tip,
    @StringRes messageId: Int, formatArgs: Array<String>,
    @StringRes povButtonNameId: Int = R.string.dialog_result_ok, povAction: () -> Unit,
    @StringRes neuButtonNameId: Int? = null, neuAction: () -> Unit,
    @StringRes negButtonNameId: Int? = R.string.dialog_result_cancel, negAction: () -> Unit = {},
    cancellable: Boolean = false
) {
    customDialogWith3Btn(
        getString(titleId), getString(messageId, *formatArgs),
        getString(povButtonNameId), povAction,
        if (neuButtonNameId != null) getString(neuButtonNameId) else null, neuAction,
        if (negButtonNameId != null) getString(negButtonNameId) else null, negAction,
        cancellable
    )
}

//3.1.3
fun Activity.dialogWithSelection(
    title: String, items: Array<out CharSequence>,
    selectAction: (which: Int) -> Unit,
    negButtonName: CharSequence,
    cancellable: Boolean = false
) {
    if (!canShow())
        return
    AlertDialog.Builder(this/*, getThemeId()*/)
        .setTitle(title)
        .setItems(items) { _, which ->
            selectAction(which)
        }
        .setNegativeButton(negButtonName) { _, _ -> }
        .show().setCanceledOnTouchOutside(cancellable)
}

fun Activity.dialogWithSelection(
    @StringRes titleId: Int = R.string.dialog_tip, items: Array<out CharSequence>,
    selectAction: (which: Int) -> Unit,
    @StringRes negButtonNameId: Int = R.string.dialog_result_cancel,
    cancellable: Boolean = false
) {
    dialogWithSelection(
        getString(titleId),
        items,
        selectAction,
        getString(negButtonNameId),
        cancellable
    )
}

//3.2 普通的Dialog
//3.2.1
fun Activity.dialogWith1Btn(
    title: String,
    message: CharSequence,
    povButtonName: CharSequence = resIdToString(R.string.dialog_result_ok),
    povAction: () -> Unit = {},
    cancellable: Boolean = false
) {
    dialogWith3Btn(
        title, message,
        povButtonName, povAction,
        null, {},
        null, {},
        cancellable
    )
}

fun Activity.dialogWith2Btn(
    title: String, message: CharSequence,
    povButtonName: CharSequence, povAction: () -> Unit,
    negButtonName: CharSequence, negAction: () -> Unit = {},
    cancellable: Boolean = false
) {
    dialogWith3Btn(
        title, message,
        povButtonName, povAction,
        null, {},
        negButtonName, negAction,
        cancellable
    )
}

//3.2.2
fun Activity.dialogWith1Btn(
    @StringRes titleId: Int = R.string.dialog_tip, @StringRes messageId: Int,
    @StringRes povButtonNameId: Int = R.string.dialog_result_ok, povAction: () -> Unit = {},
    cancellable: Boolean = false
) {
    dialogWith3Btn(
        titleId, messageId,
        povButtonNameId, povAction,
        null, {},
        null, {},
        cancellable
    )
}

fun Activity.dialogWith2Btn(
    @StringRes titleId: Int = R.string.dialog_tip, @StringRes messageId: Int,
    @StringRes povButtonNameId: Int = R.string.dialog_result_ok, povAction: () -> Unit,
    @StringRes negButtonNameId: Int = R.string.dialog_result_cancel, negAction: () -> Unit = {},
    cancellable: Boolean = false
) {
    dialogWith3Btn(
        titleId, messageId,
        povButtonNameId, povAction,
        null, {},
        negButtonNameId, negAction,
        cancellable
    )
}

//3.2.3
fun Activity.dialogWith1Btn(
    @StringRes titleId: Int = R.string.dialog_tip,
    @StringRes messageId: Int, formatArgs: Array<String>,
    @StringRes povButtonNameId: Int = R.string.dialog_result_ok, povAction: () -> Unit = {},
    cancellable: Boolean = false
) {
    dialogWith1Btn(
        getString(titleId),
        getString(messageId, *formatArgs),
        getString(povButtonNameId), povAction,
        cancellable
    )
}

fun Activity.dialogWith2Btn(
    @StringRes titleId: Int = R.string.dialog_tip,
    @StringRes messageId: Int, formatArgs: Array<String>,
    @StringRes povButtonNameId: Int = R.string.dialog_result_ok, povAction: () -> Unit,
    @StringRes negButtonNameId: Int = R.string.dialog_result_cancel, negAction: () -> Unit = {},
    cancellable: Boolean = false
) {
    dialogWith2Btn(
        getString(titleId),
        getString(messageId, *formatArgs),
        getString(povButtonNameId), povAction,
        getString(negButtonNameId), negAction,
        cancellable
    )
}

//////////////////////
/**
 * 用当前日期返回格式化路径
 * @return 2025-5-13 或者 2025-5-13-t
 */
fun Date.toFileNamePart(withPic: Boolean = true): String {
    return DateUtils.date2String(this, DateUtils.SDF_DATE_ONLY_EN) + (if (withPic) "-t" else "")
}

/**
 * 用当前日期返回格式化路径
 * @return
 * 2025-5-13.html 或者 2025-5-13-new.html
 * 2025-5-13-t.html 或者 2025-5-13-t-new.html
 */
fun Date.toFileName(withPic: Boolean = true, originalFile: Boolean = true): String {
    return DateUtils.date2String(this, DateUtils.SDF_DATE_ONLY_EN) +
            (if (withPic) "-t" else "") + (if (originalFile) "" else "-new") + ".html"
}

/**
 * 用当前日期返回格式化路径
 * @return
 * 2025-5-13/2025-5-13.html 或者 2025-5-13/2025-5-13-new.html
 * 2025-5-13-t/2025-5-13-t.html 或者 2025-5-13-t/2025-5-13-t-new.html
 */
fun Date.toFilePath(withPic: Boolean = true, originalFile: Boolean = true): String {
    val path2 = DateUtils.date2String(this, DateUtils.SDF_DATE_ONLY_EN) +
            (if (withPic) "-t" else "")
    return path2 + File.separator + path2 + (if (originalFile) "" else "-new") + ".html"
}

/**
 * 用当前日期返回格式化路径
 * @return
 * 2025/5/13/2025-5-13.zip 或者 2025/5/13/2025-5-13-t.zip
 */
fun Date.toZipUrl(withPic: Boolean = true, originalFile: Boolean = true): String {
    val path1 = DateUtils.date2String(this, DateUtils.SDF_URL_LINK)
    val path2 = DateUtils.date2String(this, DateUtils.SDF_DATE_ONLY_EN) +
            (if (withPic) "-t" else "")
    return "$path1$path2.zip"
}

//////////////////////////
fun SpannableString.setForegroundColorSpan(
    color: Color,
    start: Int = 0,
    spannableStyle: Int = Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
) {
    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
}

/**
 * @param color eg: Color.YELLOW
 */
fun SpannableString.setBackgroundColorSpan(
    color: Int,
    start: Int = 0,
    spannableStyle: Int = Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
) {
    this.setSpan(BackgroundColorSpan(color), start, start + this.length, spannableStyle)
}

/**
 * @param style eg: Typeface.BOLD
 */
fun SpannableString.setStyleSpan(
    style: Int,
    start: Int = 0,
    spannableStyle: Int = Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
) {
    this.setSpan(StyleSpan(style), start, start + this.length, spannableStyle)
}

/**
 * @param relativeSize eg: 2.5f
 */
fun SpannableString.setRelativeFontSpan(
    relativeSize: Float,
    start: Int = 0,
    spannableStyle: Int = Spannable.SPAN_INCLUSIVE_EXCLUSIVE
) {
    this.setSpan(RelativeSizeSpan(relativeSize), start, start + this.length, spannableStyle)
}

/**
 * @param typeface eg: monospace
 */
fun SpannableString.setTypeFaceSpan(
    typeface: String,
    start: Int = 0,
    spannableStyle: Int = Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
) {
    this.setSpan(TypefaceSpan(typeface), start, start + this.length, spannableStyle)
}

/**
 * 文字注解为url
 * @param url eg: "https://m.minghui.org"
 */
fun SpannableString.setUrlSpan(
    url: String,
    start: Int = 0,
    spannableStyle: Int = Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
) {
    this.setSpan(URLSpan(url), start, start + this.length, spannableStyle)
}