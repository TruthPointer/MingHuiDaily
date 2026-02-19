package org.tpmobile.minghuidaily.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebChromeClient.CustomViewCallback
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.CalendarView
import android.widget.ProgressBar
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.tpmobile.minghuidaily.MyApp
import org.tpmobile.minghuidaily.R
import org.tpmobile.minghuidaily.adapter.HistoryAdapter
import org.tpmobile.minghuidaily.data.HistoryItem
import org.tpmobile.minghuidaily.data.SelectedStateOfHistoryItem
import org.tpmobile.minghuidaily.data.TaskInfo
import org.tpmobile.minghuidaily.data.YearMonthAndDay
import org.tpmobile.minghuidaily.databinding.ActivityMainBinding
import org.tpmobile.minghuidaily.util.DateUtils
import org.tpmobile.minghuidaily.util.FileUtil
import org.tpmobile.minghuidaily.util.HttpUtil
import org.tpmobile.minghuidaily.util.LIST_TYPE_OF_PICTURE
import org.tpmobile.minghuidaily.util.Logger
import org.tpmobile.minghuidaily.util.MAX_FILE_SIZE_FOR_TIP
import org.tpmobile.minghuidaily.util.PREF_CURRENT_BROWSING_DATE
import org.tpmobile.minghuidaily.util.PREF_FONT_ZOOM_SCALE
import org.tpmobile.minghuidaily.util.PREF_THEME_NIGHT
import org.tpmobile.minghuidaily.util.PREF_ZIP_WITH_PIC
import org.tpmobile.minghuidaily.util.PROXY_PORT_FREEGATE
import org.tpmobile.minghuidaily.util.PROXY_PORT_WUJIE
import org.tpmobile.minghuidaily.util.PageUtil
import org.tpmobile.minghuidaily.util.SHOW_DETAILED_RUNNING_INFO
import org.tpmobile.minghuidaily.util.ZipUtil
import org.tpmobile.minghuidaily.util.ktx.dialogWith1Btn
import org.tpmobile.minghuidaily.util.ktx.dialogWith2Btn
import org.tpmobile.minghuidaily.util.ktx.getPref
import org.tpmobile.minghuidaily.util.ktx.setPref
import org.tpmobile.minghuidaily.util.ktx.toFileNamePart
import org.tpmobile.minghuidaily.util.ktx.toFilePath
import org.tpmobile.minghuidaily.util.ktx.toast
import java.io.File
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG = "MainActivity"

    }

    private lateinit var binding: ActivityMainBinding
    private val viewModel: ActivityMainViewModel by viewModels()

    private var dateString: String = ""
    private var selectedDate: Date = Date()
    private var fontZoomScale = 0
    private var zipWithPic = true
    private var exitTime = 0L
    private var isLargeHtmlFile: Boolean = false
    private var isFirstPageStarted: Boolean = false

    private var lastScrollY: Int = 0

    private var lastOrientation: Int = 0
    private var loadingView: View? = null
    private var fullScreenView: View? = null
    private var fullScreenViewCallback: CustomViewCallback? = null
    private var tvLoadingInfo: TextView? = null
    private var tvAdditionalInfo: TextView? = null
    private var pbLoading: ProgressBar? = null
    private var taskDialog: AlertDialog? = null

    private var taskJob: Job? = null
    private var taskIndex: Int = 0
    private var taskHistory: String = ""
    val test = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 添加返回按钮回调
        dispatchOnBackEvent()
        initView()
        initViewModel()
        createPageCss()

        initialCheck(selectedDate)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        if (MyApp.currentNightMode == AppCompatDelegate.MODE_NIGHT_YES)
            menu.findItem(R.id.action_theme_night).isChecked = true
        else
            menu.findItem(R.id.action_theme_light).isChecked = true
        if (MyApp.proxyPort == PROXY_PORT_FREEGATE)
            menu.findItem(R.id.action_proxy_freegate).isChecked = true
        else
            menu.findItem(R.id.action_proxy_wujie).isChecked = true
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_open_calendar -> {
                showCalendarDialog()
                true
            }

            R.id.action_zoom -> {
                showZoomWebViewDialog()
                true
            }

            R.id.action_browse_history -> {
                showBrowseHistoryDialog()
                true
            }

            R.id.action_clean_history -> {
                showCleanHistoryDialog()
                true
            }

            R.id.action_theme_light -> {
                item.isChecked = !item.isChecked
                switchNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                true
            }

            R.id.action_theme_night -> {
                item.isChecked = !item.isChecked
                switchNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                true
            }
            /*R.id.action_theme_system -> {
                //toast("system theme")
                switchNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                true
            }*/
            R.id.action_proxy_freegate -> {
                MyApp.USE_PROXY = true
                item.isChecked = !item.isChecked
                MyApp.setProxy(PROXY_PORT_FREEGATE)
                true
            }

            R.id.action_proxy_wujie -> {
                MyApp.USE_PROXY = true
                item.isChecked = !item.isChecked
                MyApp.setProxy(PROXY_PORT_WUJIE)
                true
            }

            R.id.action_proxy_none -> {
                MyApp.USE_PROXY = false
                item.isChecked = !item.isChecked
                true
            }

            R.id.action_about -> {
                showAboutDialog()
                true
            }

            R.id.action_exit_app -> {
                finish()
                true
            }

            R.id.action_test -> {
                modifyHtml(DateUtils.parse("2025-5-13")!!, true)
                //showBrowseHistoryDialog()
                true
            }
            //R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * <activity
     *             android:name=".MainActivity"
     *             android:exported="true"
     *             android:configChanges="uiMode|"
     *  则和下面的 onConfigurationChanged 配合，自己做切换逻辑
     */
    /*override fun onConfigurationChanged(newConfig: Configuration) {
        val currentNightMode = newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK
        when(currentNightMode){
            Configuration.UI_MODE_NIGHT_NO -> {}
            Configuration.UI_MODE_NIGHT_YES -> {}
        }
        super.onConfigurationChanged(newConfig)
    }*/

    override fun onResume() {
        Logger.i("onResume()")
        super.onResume()
        binding.webView.apply {
            onResume()
            resumeTimers()
        }

    }

    override fun onPause() {
        Logger.i("onPause()")
        super.onPause()
        binding.webView.apply {
            onPause()
            pauseTimers()
        }
    }

    override fun onDestroy() {
        binding.webView.apply {
            loadUrl("about:blank")
            stopLoading()
            //webViewClient = null
            settings.javaScriptEnabled = false
            webChromeClient = null
            clearHistory()
            removeAllViews()
            destroy()
        }
        super.onDestroy()
    }

    /////////////////////////////////////
    fun switchNightMode(nightModState: Int) {
        MyApp.currentNightMode = nightModState
        delegate.localNightMode = nightModState
        setPref(PREF_THEME_NIGHT, nightModState)
        //修改网页模式
        changePageCss()
    }

    fun dispatchOnBackEvent() {
        onBackPressedDispatcher.addCallback(
            this,
            onBackPressedCallback = object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (inFullScreenState()) {
                        hideFullScreenView()
                    } else if (binding.webView.canGoBack()) {
                        Logger.i(TAG, "webView.canGoBack() = true")
                        binding.webView.goBack()
                    } else {
                        if ((System.currentTimeMillis() - exitTime) > 2000) {
                            toast(R.string.quit_the_app_after_pressing_back_key_once_again)
                            exitTime = System.currentTimeMillis()
                        } else {
                            finish()
                        }
                    }
                }
            })
    }

    fun initView() {
        dateString = getPref(PREF_CURRENT_BROWSING_DATE, "")
        selectedDate = DateUtils.dateStringToDate(dateString)

        fontZoomScale = getPref(PREF_FONT_ZOOM_SCALE, 0)
        zipWithPic = getPref(PREF_ZIP_WITH_PIC, true)

        setSupportActionBar(binding.toolbar)
        val isWebViewSupportProxyOverrideFeature = HttpUtil.isWebViewSupportProxyOverrideFeature()
        Logger.i(TAG, "SEC=$isWebViewSupportProxyOverrideFeature")
        binding.toolbar.setLogo(
            if (isWebViewSupportProxyOverrideFeature)
                R.drawable.mh_daily_logo_sec_ok
            else
                R.drawable.mh_daily_logo
        )
        supportActionBar?.title = ""

        with(binding.webView.settings) {
            javaScriptEnabled = true
            javaScriptCanOpenWindowsAutomatically = true
            useWideViewPort = true
            loadWithOverviewMode = true
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
            allowFileAccess = true
            allowContentAccess = true
            //allowUniversalAccessFromFileURLs = true //此会导致无法顺利加载 css 或无法完全实现
            domStorageEnabled = true //20200406 防止下载时，先出现空白页面，才开始下载
            cacheMode = WebSettings.LOAD_NO_CACHE
            //layoutAlgorithm = WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        }
        binding.webView.webViewClient = object : WebViewClient() {

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                binding.webView.clearHistory()
                super.onPageStarted(view, url, favicon)
                Logger.i(TAG, "onPageStarted")
                isFirstPageStarted = true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                Logger.i(TAG, "onPageFinished")
            }

            override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler?,
                error: SslError?
            ) {
                super.onReceivedSslError(view, handler, error)
                //handler?.proceed()
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                Logger.i("shouldOverrideUrlLoading", view?.url ?: "***")
                val url = request?.url?.toString() ?: ""
                val ext = File(url).extension.lowercase(Locale.getDefault())
                if (LIST_TYPE_OF_PICTURE.contains(ext)) {
                    showPicture(url)
                    return true
                }
                return true //禁用了网页内点击
            }
        }

        binding.webView.webChromeClient = object : WebChromeClient() {
            override fun getDefaultVideoPoster(): Bitmap? {
                return try {
                    super.getDefaultVideoPoster()
                } catch (e: Exception) {
                    Logger.e(TAG, "getDefaultVideoPoster: ${e.message}")
                    BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
                }
            }

            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                //super.onProgressChanged(view, newProgress)
                //Logger.i("PROGRESS", "$newProgress")
                dispatchProgressInfo(
                    taskIndex,
                    TaskInfo.TASK_NAME_LOAD_URL,
                    newProgress,
                    ""
                )
                if (newProgress == 100)
                    closeTaskDialog()
            }

            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                binding.webView.visibility = View.INVISIBLE
                Logger.i(
                    TAG,
                    "[onShowCustomView] fullScreenView = null ? ${fullScreenView == null}"
                )
                if (fullScreenView != null) {
                    fullScreenViewCallback?.onCustomViewHidden()
                    return
                }
                lastScrollY = binding.webView.scrollY
                lastOrientation = requestedOrientation
                Logger.i(TAG, "lastScrollY = $lastScrollY, lastOrientation = $lastOrientation")

                toggle(true)

                binding.fullscreenLayout.visibility = View.VISIBLE
                binding.fullscreenLayout.addView(view)
                fullScreenView = view
                fullScreenViewCallback = callback

                super.onShowCustomView(view, callback)
            }

            override fun onHideCustomView() {
                Logger.i(
                    TAG,
                    "[onHideCustomView] fullScreenView = null ? ${fullScreenView == null}"
                )
                if (fullScreenView == null) return
                fullScreenViewCallback?.onCustomViewHidden()
                toggle(false)

                binding.fullscreenLayout.visibility = View.GONE
                binding.fullscreenLayout.removeAllViews()
                fullScreenView = null
                binding.webView.visibility = View.VISIBLE

                binding.webView.postDelayed({
                    binding.webView.scrollTo(0, lastScrollY)
                }, if (isLargeHtmlFile) 5000 else 1000)
                super.onHideCustomView()
            }

            override fun getVideoLoadingProgressView(): View? {
                if (loadingView == null) {
                    loadingView = layoutInflater.inflate(R.layout.view_loading_video, null)
                }
                return loadingView //super.getVideoLoadingProgressView()
            }
        }
    }

    fun initViewModel() {
        viewModel.getProgress().observe(this) { taskInfo ->
            if (taskInfo.taskName == TaskInfo.TASK_NAME_LOAD_URL) {
                val showTvAdditionalInfo = taskDialog?.isShowing == true && isLargeHtmlFile
                Logger.i(TAG, "tvAdditionalInfo可见情况：$showTvAdditionalInfo")
                tvAdditionalInfo?.visibility =
                    if (showTvAdditionalInfo) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }
            }
            tvLoadingInfo?.text = taskInfo.taskInfo
            pbLoading?.visibility = if (taskInfo.progress != -2) {
                View.VISIBLE
            } else {
                View.GONE
            }
            pbLoading?.progress = taskInfo.progress

            if (taskInfo.isError) {
                closeTaskDialog()
                dialogWith1Btn("错误", "错误详情：${taskInfo.errorInfo}")
            }
        }
    }

    /////////////////////////////////////
    private fun showZoomWebViewDialog() {
        if (!isFirstPageStarted) {
            toast(R.string.info_no_loaded_page)
            return
        }
        val view = LayoutInflater.from(this).inflate(R.layout.layout_zoom_scale, null)
        val sbZoomScale: SeekBar = view.findViewById(R.id.sbZoomScale)
        val tvZoomScale: TextView = view.findViewById(R.id.tvZoomScale)
        val tvZoomTip: TextView = view.findViewById(R.id.tvZoomTip)
        tvZoomTip.setText(R.string.zoom_web_view_font_size)
        sbZoomScale.progress = fontZoomScale
        tvZoomScale.text = "${100 + fontZoomScale} %"
        sbZoomScale.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(arg0: SeekBar, progress: Int, fromUser: Boolean) {
                tvZoomScale.text = "${100 + progress} %"
                fontZoomScale = progress
                setPref(PREF_FONT_ZOOM_SCALE, progress)
                changePageCss()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }
        })
        AlertDialog.Builder(this).apply {
            setView(view)
            show()
            //setCancelable(false)
        }
    }

    var cvTodayNewsCalendar: CalendarView? = null
    private fun showCalendarDialog() {
        //1、檢查轉換
        //2、初始化View
        val view =
            LayoutInflater.from(this).inflate(R.layout.layout_calendar, null)
        cvTodayNewsCalendar = view.findViewById(R.id.cvTodayNewsCalendar)
        val rgDownloadZipType: RadioGroup = view.findViewById(R.id.rgDownloadZipType)

        if (zipWithPic)
            rgDownloadZipType.check(R.id.rbWithPic)
        else
            rgDownloadZipType.check(R.id.rbWithoutPic)
        rgDownloadZipType.setOnCheckedChangeListener { group, checkedId ->
            zipWithPic = checkedId == R.id.rbWithPic
            setPref(PREF_ZIP_WITH_PIC, zipWithPic)
            Logger.i(TAG, "downloadWithPic=$zipWithPic")
        }

        //val layoutParams = cvTodayNewsCalendar.layoutParams as LinearLayoutCompat.LayoutParams
        //layoutParams.height = DisplayUtil.getScreenWidth(this) * 9 / 10
        //layoutParams.width = DisplayUtil.getScreenWidth(this) * 4 / 5
        //cvTodayNewsCalendar.layoutParams = layoutParams
        //3、設置初始值等
        //3.1 设置起始日期
        val calendar = Calendar.getInstance()
        calendar.set(1999, 0, 1)//Month=[0,11] 0为起点
        cvTodayNewsCalendar?.minDate = calendar.timeInMillis
        cvTodayNewsCalendar?.maxDate = Date().time
        //3.2 设置控制所选的日期
        cvTodayNewsCalendar?.date = selectedDate.time
        //3.3 恢复当前值，因为 OnDateChange 其值才会变化，故默认保持在今日的状态
        //    也就是说，与 cvTodayNewsCalendar.maxDate 一致的最大值
        //calendar = Calendar.getInstance()
        //month: The month that was set [0-11].
        cvTodayNewsCalendar?.setOnDateChangeListener { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            cvTodayNewsCalendar?.date = calendar.timeInMillis
            Logger.i(TAG, "$year-${month + 1}-$dayOfMonth")
        }
        cvTodayNewsCalendar?.firstDayOfWeek = Calendar.MONDAY
        val listenerOk = DialogInterface.OnClickListener { _, _ ->
            //1.
            //old selectedDate = Date(calendar.timeInMillis)
            selectedDate = Date(cvTodayNewsCalendar?.date!!)
            dateString = DateUtils.date2String(selectedDate).trim()
            setPref(PREF_CURRENT_BROWSING_DATE, dateString)
            Logger.i(TAG, "所选择日期：$dateString")
            //toast("所选择日期：$dateString")
            //toast(R.string.loading, true)
            //2.
            if (test) {
                showTaskRunningDialog(
                    DateUtils.parse("2026-1-19")!!,
                    zipWithPic
                ) { date, withPic ->
                    runTasksForNewsDisplay(this, date, withPic)
                }
            } else {
                showTaskRunningDialog(
                    selectedDate,
                    zipWithPic
                ) { date, withPic ->
                    runTasksForNewsDisplay(this, date, withPic)
                }
            }
        }
        val listenerCancel = DialogInterface.OnClickListener { _, _ -> }
        //4、運行
        AlertDialog.Builder(this).apply {
            setView(view)
            setTitle(R.string.select_date_in_calendar)
            setPositiveButton(R.string.dialog_result_ok, listenerOk)
            setNegativeButton(R.string.dialog_result_cancel, listenerCancel)
            //setCancelable(false)
            with(show()) {
                getButton(DialogInterface.BUTTON_POSITIVE)?.isAllCaps = false
                getButton(DialogInterface.BUTTON_NEGATIVE)?.isAllCaps = false
            }
        }
    }

    private fun showPicture(url: String) {
        startActivity(
            ShowImageActivity.intentFor(
                applicationContext,
                url
            )
        )
    }

    private fun showCleanHistoryDialog() {
        val data = collectData()
        if (data.isEmpty()) {
            toast(R.string.info_no_history_info)
            return
        }
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_history, null)
        val rvHistory: RecyclerView = view.findViewById(R.id.rvHistory)
        val adapter = HistoryAdapter(data, false)
        rvHistory.layoutManager = LinearLayoutManager(this)
        rvHistory.adapter = adapter

        val listenerPositive = DialogInterface.OnClickListener { _, _ ->
            val list = adapter.getAllSelectedItem().mapNotNull { dateString ->
                DateUtils.parse(dateString, DateUtils.SDF_DATE_FOR_HISTORY)
            }
            if (list.isEmpty()) {
                toast(R.string.info_please_select_cleaning_date)
                return@OnClickListener
            }

            val pathsWithoutPic = list.map { date -> File(filesDir, date.toFileNamePart(false)) }
            val pathsWithPic = list.map { date -> File(filesDir, date.toFileNamePart(true)) }
            dialogWith2Btn(
                R.string.dialog_tip,
                R.string.confirmation_about_clean_selected_history,
                povAction = {
                    runTasksForCleanHistory(pathsWithoutPic + pathsWithPic)
                }
            )
        }
        val listenerNeutral = DialogInterface.OnClickListener { _, _ ->
            val list = (filesDir.listFiles()?.filter { it.isDirectory } ?: emptyList())
                .asSequence()
                .map { file -> file.name }
                .distinct()
                .filter { name -> name.matches(Regex("\\d{4}-\\d{1,2}-\\d{1,2}(-t)?")) }
                .map { name -> File(filesDir, name) }.toList()
            if (list.isEmpty()) {
                toast(R.string.info_no_history_info)
                return@OnClickListener
            }
            dialogWith2Btn(
                R.string.dialog_tip,
                R.string.confirmation_about_clean_all_history,
                povAction = {
                    runTasksForCleanHistory(list)
                }
            )
        }
        val listenerNegative = DialogInterface.OnClickListener { _, _ -> }

        AlertDialog.Builder(this).apply {
            setTitle(R.string.dialog_clean_history)
            setView(view)
            setPositiveButton(R.string.dialog_button_clean_selected_history, listenerPositive)
            setNeutralButton(R.string.dialog_button_clean_all_history, listenerNeutral)
            setNegativeButton(R.string.dialog_result_cancel, listenerNegative)
            //setCancelable(false)
            show()
        }
    }

    private fun showBrowseHistoryDialog() {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_history, null)
        val rvHistory: RecyclerView = view.findViewById(R.id.rvHistory)
        val data = collectData()
        if (data.isEmpty()) {
            toast(R.string.info_no_history_info)
            return
        }
        val adapter = HistoryAdapter(data)
        rvHistory.layoutManager = LinearLayoutManager(this)
        rvHistory.adapter = adapter

        val listenerPositive = DialogInterface.OnClickListener { _, _ ->
            val list = adapter.getAllSelectedItem()
            if (list.isEmpty()) {
                toast(R.string.info_please_select_one_date)
                return@OnClickListener
            }
            //1.
            Logger.i(TAG, "所选择日期：$list[0]")
            val date = DateUtils.parse(list[0], DateUtils.SDF_DATE_FOR_HISTORY)
            if (date == null) {
                toast(R.string.error_date_format)
                return@OnClickListener
            }
            //2.
            //selectedDate = date
            //dateString = DateUtils.date2String(selectedDate).trim()
            //setPref(PREF_CURRENT_BROWSING_DATE, dateString)
            //3.
            checkAndLoadMyUrl(date)
        }
        val listenerNegative = DialogInterface.OnClickListener { _, _ -> }

        AlertDialog.Builder(this).apply {
            setTitle(R.string.dialog_browse_history)
            setView(view)
            setPositiveButton(R.string.dialog_button_browse_history, listenerPositive)
            setNegativeButton(R.string.dialog_result_cancel, listenerNegative)
            //setCancelable(false)
            show()
        }
    }

    private fun showTaskRunningDialog(
        date: Date,
        withPic: Boolean,
        doSomeWork: ((date: Date, withPic: Boolean) -> Unit)? = null
    ) {
        Logger.i(TAG, "showTaskRunningDialog...${taskDialog == null}, ${taskDialog?.isShowing}")
        //1、对话框
        if (taskDialog == null || taskDialog?.isShowing == false) {
            Logger.i(TAG, "showTaskRunningDialog: isShowing == false")
            val view =
                LayoutInflater.from(this).inflate(R.layout.dialog_task_progress, null)
            tvLoadingInfo = view.findViewById(R.id.tvLoadingInfo)
            tvAdditionalInfo = view.findViewById(R.id.tvAdditionalInfo)
            pbLoading = view.findViewById(R.id.pbLoading)
            tvAdditionalInfo?.visibility = View.GONE

            val listenerOk = DialogInterface.OnClickListener { _, _ ->
                dialogWith2Btn(
                    R.string.dialog_tip,
                    R.string.confirmation_about_cancel_tasks,
                    povAction = {
                        closeTaskDialog()
                    }
                )
            }
            //val listenerCancel = DialogInterface.OnClickListener { _, _ -> }

            val builder = AlertDialog.Builder(this).apply {
                setView(view)
                setTitle("任务进度")
                setPositiveButton(R.string.dialog_button_cancel_tasks, listenerOk)
                //setNegativeButton(R.string.dialog_result_cancel, listenerCancel)
            }

            taskDialog = builder.show()
            taskDialog?.setCanceledOnTouchOutside(false)
        }

        //2.执行任务
        doSomeWork?.invoke(date, withPic)
    }

    private fun showAboutDialog() {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_app_target, null)
        val listenerPositive = DialogInterface.OnClickListener { _, _ -> }
        AlertDialog.Builder(this).apply {
            setTitle(R.string.dialog_title_about)
            setView(view)
            setPositiveButton(R.string.dialog_button_i_know, listenerPositive)
            //setCancelable(false)
            show()
        }
    }

    private fun runTasksForNewsDisplay(
        context: Context,
        date: Date,
        withPic: Boolean,
        delZipAfterUnzip: Boolean = true
    ) {
        taskJob = lifecycleScope.launch(Dispatchers.IO) {
            taskIndex = 0
            taskHistory = ""
            isLargeHtmlFile = false

            //0.检查历史存档
            val newHtmlFile =
                File(filesDir.absolutePath + File.separator + date.toFilePath(withPic, false))
            if (newHtmlFile.exists()) {
                //4.loadUrl 此处，task dialog 已经处于打开状态
                if (!isActive) return@launch
                launch(Dispatchers.Main) {
                    loadMyUrl(PageUtil.getBaseUrl(date, withPic))
                }
                return@launch
            }

            //1.download
            taskIndex++
            var filePath: String = ""
            val baseUrl = PageUtil.getBaseUrl(date, withPic)
            val url = HttpUtil.getDownloadUrl(date, withPic)
            HttpUtil.downloadFile(
                context,
                url,
                onProgress = { progress, info ->
                    dispatchProgressInfo(
                        taskIndex,
                        TaskInfo.TASK_NAME_DOWNLOAD,
                        progress,
                        info
                    )
                }).fold(
                onSuccess = { path ->
                    Logger.i("成功下载")
                    filePath = path
                },
                onFailure = { e ->
                    Logger.e("下载出错了，详情：${e.message}")
                    val info = if(e.message?.contains("404") == true)
                        "${DateUtils.date2String(date, DateUtils.SDF_DATE_ONLY_CN_SIMPLE)}的文件未找到或尚未发布。" else e.message ?: "原因不详"
                    dispatchProgressInfo(
                        taskIndex,
                        TaskInfo.TASK_NAME_DOWNLOAD,
                        -1,
                        info
                    )
                    delay(1000)
                    closeTaskDialog()
                    return@launch
                }
            )

            Logger.i(TAG, "filePah = $filePath")
            //2.unzip
            if (!isActive) return@launch
            taskIndex++
            val zipFile = File(filePath)
            ZipUtil.unZipFileWithProgress(
                zipFile,
                zipFile.parent!! + File.separator + baseUrl,
                onProgress = { progress, info ->
                    dispatchProgressInfo(
                        taskIndex,
                        TaskInfo.TASK_NAME_UNZIP,
                        progress,
                        info
                    )
                }
            ).fold(
                onSuccess = {
                    Logger.i("解压完成1")
                },
                onFailure = { e ->
                    Logger.e(TAG, "解压出错了")
                    dispatchProgressInfo(
                        taskIndex,
                        TaskInfo.TASK_NAME_UNZIP,
                        -1,
                        e.message ?: "原因不详"
                    )
                    delay(1000)
                    closeTaskDialog()
                    return@launch
                }
            )
            if (delZipAfterUnzip) {
                zipFile.delete()
            }

            //3.modify
            if (!isActive) return@launch
            taskIndex++
            PageUtil.modifyHtml(
                date,
                zipWithPic,
                resources.displayMetrics.widthPixels * 9 / 10,
                (resources.displayMetrics.density * 100).toInt(),
                onProgress = { progress, info ->
                    dispatchProgressInfo(
                        taskIndex,
                        TaskInfo.TASK_NAME_MODIFY_HTML_FILE,
                        progress,
                        info
                    )
                }
            ).fold(
                onSuccess = {
                    Logger.e(TAG, "完成文件处理")
                },
                onFailure = { e ->
                    Logger.e(TAG, "处理文件出错了,详情：${e.message}")
                    dispatchProgressInfo(
                        taskIndex,
                        TaskInfo.TASK_NAME_MODIFY_HTML_FILE,
                        -1,
                        e.message ?: "原因不详"
                    )
                    delay(1000)
                    closeTaskDialog()
                    return@launch
                }
            )

            //4.loadUrl
            if (!isActive) return@launch
            //taskIndex++ loadMyUrl自动++
            launch(Dispatchers.Main) {
                loadMyUrl(PageUtil.getBaseUrl(date, withPic))
            }
        }
    }

    private fun runTasksForWebViewReload(taskName: String) {
        Logger.i(TAG, "runTasksForWebViewReload...")
        lifecycleScope.launch(Dispatchers.IO) {
            taskIndex = 0
            taskHistory = ""
            //从 历史归档 和 日历 从新打开时，才修改这个状态，其余不重置 isLargeHtmlFile = false

            //1.修改
            taskIndex++
            dispatchProgressInfo(taskIndex, taskName, 0, "开始修改样式文件...")
            val taskInfo = PageUtil.generateStyleCss(
                100 + fontZoomScale,
                MyApp.currentNightMode == AppCompatDelegate.MODE_NIGHT_YES
            )
            if (taskInfo.isError) {
                dispatchProgressInfo(
                    taskIndex,
                    taskName,
                    -1,
                    taskInfo.errorInfo
                )
                return@launch
            }
            dispatchProgressInfo(taskIndex, taskName, 100, "修改样式文件成功")
            if (!isFirstPageStarted) {
                runOnUiThread { toast(R.string.info_no_loaded_page) }
                return@launch
            }
            //2. reload
            taskIndex++
            dispatchProgressInfo(taskIndex, taskName, 0, "开始加载网页...")
            launch(Dispatchers.Main) {
                Logger.i(TAG, "runTasksForWebViewReload: webview.reload...")
                binding.webView.reload()
            }
        }
    }

    private fun runTasksForCleanHistory(list: List<File>) {
        Logger.i(TAG, "runTasksForCleanHistory...")
        lifecycleScope.launch(Dispatchers.IO) {
            taskIndex = 0
            taskHistory = ""
            isLargeHtmlFile = false

            taskIndex++
            dispatchProgressInfo(
                taskIndex,
                TaskInfo.TASK_NAME_CLEAN_HISTORY,
                0,
                "开始清理存档..."
            )
            FileUtil.clearFiles(list) { progress, info ->
                dispatchProgressInfo(
                    taskIndex,
                    TaskInfo.TASK_NAME_CLEAN_HISTORY,
                    progress,
                    info
                )
            }.fold(
                onSuccess = {},
                onFailure = { e ->
                    dispatchProgressInfo(
                        taskIndex,
                        TaskInfo.TASK_NAME_CLEAN_HISTORY,
                        -1,
                        e.message ?: "原因不详"
                    )
                    return@launch
                }
            )
            runOnUiThread { toast(R.string.info_clear_history_ok) }
            dispatchProgressInfo(
                taskIndex,
                TaskInfo.TASK_NAME_CLEAN_HISTORY,
                100,
                "完成清理存档"
            )
        }
    }

    private fun dispatchProgressInfo(
        taskIndex: Int,
        taskName: String,
        progress: Int,
        info: String,
        showDetailInfo: Boolean = SHOW_DETAILED_RUNNING_INFO
    ) {
        Logger.i(TAG, "dispatchProgressInfo: $taskIndex, $taskName: $progress, $info")
        runOnUiThread {
            if (progress == 100) {
                val succInfo = "$taskIndex.${taskName}已完成\n"
                if (!taskHistory.contains(succInfo)) //连续切换出现问题，特别是 zoom 时
                    taskHistory += succInfo
            }
            val taskInfo = if (progress == -2)
                info
            else if (progress == -1)
                ""
            else
                "$taskHistory$taskIndex.${if (showDetailInfo) info else "正在${taskName}中..."}"//！！！暂未将传递过来的正常运行信息显示
            viewModel.setProgress(
                TaskInfo(
                    taskName,
                    taskInfo,
                    progress,
                    if (progress == -1) info else "",
                    progress == -1
                )
            )
        }
    }

    private fun closeTaskDialog(cancelTask: Boolean = true) {
        runOnUiThread {
            if (cancelTask) {
                taskJob?.cancel(_root_ide_package_.kotlinx.coroutines.CancellationException())
            }
            taskJob = null
            taskDialog?.dismiss()
            taskDialog = null
        }
    }

    private fun inFullScreenState() = fullScreenView != null

    @SuppressLint("WrongConstant")
    fun hideFullScreenView() {
        if (WebViewFeature.isFeatureSupported(WebViewFeature.GET_WEB_CHROME_CLIENT)) {
            WebViewCompat.getWebChromeClient(binding.webView)?.onHideCustomView()
        }
        if (lastOrientation != requestedOrientation)
            setRequestedOrientation(lastOrientation)
    }

    @Suppress("DEPRECATION")
    private fun toggle(fullscreen: Boolean) {
        if (fullscreen) {
            //0.
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            //1.
            val attrs = window.attributes
            attrs.flags = attrs.flags or
                    WindowManager.LayoutParams.FLAG_FULLSCREEN or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            if (Build.VERSION.SDK_INT >= 28) {
                attrs.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
            window.attributes = attrs
            //3
            binding.root.fitsSystemWindows = false//本程序这里不能省
            // Delayed removal of status and navigation bar
            if (Build.VERSION.SDK_INT >= 30) {
                window.setDecorFitsSystemWindows(false)
                window.insetsController?.apply {
                    hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                    systemBarsBehavior =
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            } else {
                // Note that some of these constants are new as of API 16 (Jelly Bean)
                // and API 19 (KitKat). It is safe to use them, as they are inlined
                // at compile-time and do nothing on earlier devices.
                binding.root.systemUiVisibility =
                    View.SYSTEM_UI_FLAG_LOW_PROFILE or
                            View.SYSTEM_UI_FLAG_FULLSCREEN or
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                //}
            }
            //2.
            supportActionBar?.hide()
        } else {
            //0.20260211 取消
            //requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            Logger.i(
                TAG,
                "currentOrientation = $requestedOrientation, lastOrientation = $lastOrientation"
            )
            if (lastOrientation != requestedOrientation)
                setRequestedOrientation(lastOrientation)
            //1.
            val attrs = window.attributes
            attrs.flags = attrs.flags and WindowManager.LayoutParams.FLAG_FULLSCREEN.inv()
            attrs.flags = attrs.flags and WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON.inv()
            /*if (Build.VERSION.SDK_INT >= 28) {
                attrs.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT
            }*/
            window.attributes = attrs
            //3.
            binding.root.fitsSystemWindows = true//本程序这里不能省
            if (Build.VERSION.SDK_INT >= 30) {
                window.setDecorFitsSystemWindows(true)
                window.insetsController?.apply {
                    show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                    //systemBarsBehavior = WindowInsetsController.BEHAVIOR_DEFAULT
                }
            } else {
                binding.root.systemUiVisibility =
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                //}
            }
            //2.
            supportActionBar?.show()
        }
    }

    private fun initialCheck(date: Date = Date()){
        Logger.i(TAG, "initialCheck...")
        showTaskRunningDialog(
            selectedDate,
            zipWithPic
        ) { date, withPic ->
            lifecycleScope.launch(Dispatchers.IO) {
                taskIndex = 0
                taskHistory = ""

                //1.检查夜间模式，修改css style
                if(delegate.localNightMode != MyApp.currentNightMode){
                    runOnUiThread {
                        delegate.localNightMode = MyApp.currentNightMode
                    }
                    //修改网页模式
                    taskIndex++
                    dispatchProgressInfo(taskIndex, TaskInfo.TASK_NAME_MODIFY_CSS_FILE, 0, "开始修改样式文件...")
                    val taskInfo = PageUtil.generateStyleCss(
                        100 + fontZoomScale,
                        MyApp.currentNightMode == AppCompatDelegate.MODE_NIGHT_YES
                    )
                    if (taskInfo.isError) {
                        dispatchProgressInfo(
                            taskIndex,
                            TaskInfo.TASK_NAME_MODIFY_CSS_FILE,
                            -1,
                            taskInfo.errorInfo
                        )
                        return@launch
                    }
                    dispatchProgressInfo(taskIndex, TaskInfo.TASK_NAME_MODIFY_CSS_FILE, 100, "修改样式文件成功")
                }

                //2.检查归档情况
                var newWithPicState: Boolean
                val fileNewWithPic =
                    File(filesDir.absolutePath + File.separator + date.toFilePath(true, false))
                val fileNewWithoutPic =
                    File(filesDir.absolutePath + File.separator + date.toFilePath(false, false))
                if (!fileNewWithPic.exists() && !fileNewWithoutPic.exists()) {
                    runOnUiThread {
                        showCalendarDialog()
                    }
                    return@launch
                } else if (fileNewWithPic.exists() || !fileNewWithoutPic.exists()) {
                    newWithPicState = true
                } else if (!fileNewWithPic.exists() || fileNewWithoutPic.exists()) {
                    newWithPicState = false
                } else {
                    newWithPicState = true
                }

                launch(Dispatchers.Main) {
                    isLargeHtmlFile = false
                    loadMyUrl(PageUtil.getBaseUrl(date, newWithPicState))
                }
            }
        }
    }

    private fun checkAndLoadMyUrl(date: Date = Date()) {
        //检查当前日期的修改后文件 2026-5-13-t-new.html 是否存在，不存在则显示日期选择对话框，因至少整个任务流程没完成

        //m1. 直接受 日历对话框 的有图和无图的选择制约
        /*val fileNew =
            File(filesDir.absolutePath + File.separator + date.toFilePath(zipWithPic, false))
        if (checkIt && !fileNew.exists()) {
            toast("无法浏览${DateUtils.date2String(date, DateUtils.SDF_DATE_ONLY_CN)}的${if(zipWithPic) "有图" else "无图"}文件，因找不到相应的文件。请从新选择日期。“)
            showCalendarDialog()
            return
        }
        showTaskRunningDialog(
            date,
            zipWithPic
        ) { date, withPic ->
            taskIndex = 0
            taskHistory = ""
            isLargeHtmlFile = false
            loadMyUrl(PageUtil.getBaseUrl(date, withPic))
        }*/

        //m2 当前日期，有哪个就打开哪个，但已有图的为优先选择打开浏览
        var newWithPicState: Boolean
        val fileNewWithPic =
            File(filesDir.absolutePath + File.separator + date.toFilePath(true, false))
        val fileNewWithoutPic =
            File(filesDir.absolutePath + File.separator + date.toFilePath(false, false))
        if (!fileNewWithPic.exists() && !fileNewWithoutPic.exists()) {
            showCalendarDialog()
            return
        } else if (fileNewWithPic.exists() || !fileNewWithoutPic.exists()) {
            newWithPicState = true
        } else if (!fileNewWithPic.exists() || fileNewWithoutPic.exists()) {
            newWithPicState = false
        } else {
            newWithPicState = true
        }
        showTaskRunningDialog(
            date,
            newWithPicState
        ) { date, withPic ->
            taskIndex = 0
            taskHistory = ""
            isLargeHtmlFile = false
            loadMyUrl(PageUtil.getBaseUrl(date, newWithPicState))
        }
        //val dateString = DateUtils.date2String(date)
        //loadMyUrl(getBaseUrl(dateString, zipWithPic))
    }

    /**
     *         var date = "2025-5-13"
     *         date="2026-1-19"
     *         val withPic = true
     */
    fun loadMyUrl(filePath: String) {
        Logger.i(TAG, "loadMyUrl...")
        HttpUtil.setProxyForWebView()

        taskIndex++
        dispatchProgressInfo(taskIndex, TaskInfo.TASK_NAME_LOAD_URL, 0, "开始加载网页...")
        val basePath = "${filesDir.absolutePath}/$filePath/"
        val file = "$basePath$filePath-new.html"//"$filePath-short.html"
        isLargeHtmlFile = File(file).length() >= MAX_FILE_SIZE_FOR_TIP

        Logger.i("WebView", "path = $file, ${if (isLargeHtmlFile) "较大文件" else ""}")
        val htmlString = File(file).readText()

        binding.webView.loadUrl(file)

        //20260211 取消，此会导致跳转问题
        /*binding.webView.loadDataWithBaseURL(
            "file://$basePath",
            file, "text/html", "utf-8", null
        )*/
    }

    fun createPageCss() {
        lifecycleScope.launch(Dispatchers.IO) {
            //1.
            Logger.i(TAG, "createPageCss...")
            if (File(filesDir.absolutePath, "style.css").exists())
                return@launch
            PageUtil.generateStyleCss(
                100 + fontZoomScale,
                MyApp.currentNightMode == AppCompatDelegate.MODE_NIGHT_YES
            )
            Logger.i(TAG, "createPageCss: OK...")
            //2.
            //if (filesDir.listFiles { it.isDirectory }?.size == 0) {
            //    runOnUiThread {
            //        showCalendarDialog()
            //    }
            //}
        }
    }

    fun changePageCss() {
        Logger.i(TAG, "changePageCss: webview.reload[$fontZoomScale]...")
        showTaskRunningDialog(
            selectedDate,
            zipWithPic
        ) { date, withPic ->
            runTasksForWebViewReload(TaskInfo.TASK_NAME_MODIFY_CSS_FILE)
        }
    }

    fun modifyHtml(date: Date, withPic: Boolean) {
        lifecycleScope.launch(Dispatchers.IO) {
            PageUtil.modifyHtml(
                date,
                withPic,
                resources.displayMetrics.widthPixels * 9 / 10,
                (resources.displayMetrics.density * 100).toInt(),
                onProgress = { progress, info ->
                    Logger.i(TAG, $"修改: $progress, $info")
                }
            )
        }
    }

    fun collectData(): List<HistoryItem> {
        val data = mutableListOf<HistoryItem>()
        //1.
        val map = (filesDir.listFiles()?.filter { it.isDirectory } ?: emptyList())
            .asSequence()
            .map { file ->
                if (file.name.endsWith("-t")) file.name.replace(
                    "-t",
                    ""
                ) else file.name
            }
            .distinct()
            .filter { name -> name.matches(Regex("\\d{4}-\\d{1,2}-\\d{1,2}")) }
            .map { name ->
                val s = name.split("-")
                YearMonthAndDay("${s[0]}年${s[1].toInt()}月", s[2].toInt())
            }.groupBy { it.yearMonth }

        for (yearMonth in map.keys) {
            var list =
                map[yearMonth]?.map { SelectedStateOfHistoryItem(it.day, false) } ?: emptyList()
            list = list.sortedBy { it.day }
            if (list.isNotEmpty())
                data.add(HistoryItem(yearMonth, list))
        }
        return data.sortedBy { it.yearMonth }
    }

}
