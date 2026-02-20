package org.tpmobile.minghuidaily.ui

import android.content.Context
import android.content.Intent
import android.graphics.PointF
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import coil.imageLoader
import coil.request.ImageRequest
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.ImageViewState
import org.tpmobile.minghuidaily.MyApp
import org.tpmobile.minghuidaily.R
import org.tpmobile.minghuidaily.databinding.ActivityShowImageBinding
import org.tpmobile.minghuidaily.util.Logger
import org.tpmobile.minghuidaily.util.ktx.toast


class ShowImageActivity : AppCompatActivity() {
    companion object {
        private val TAG = "ShowImageActivity"

        val ARG_URL = "url"
        fun intentFor(
            context: Context,
            url: String
        ): Intent {
            val intent = Intent(context, ShowImageActivity::class.java)
            intent.putExtra(ARG_URL, url)
            return intent
        }
    }

    private val zoomScale = 0.5f
    private var imageUrl: String = ""
    private lateinit var binding: ActivityShowImageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dispatchOnBackEvent()

        imageUrl = intent.getStringExtra(ARG_URL) ?: ""
        if (imageUrl.isBlank()) {
            toast(R.string.parameters_error)
            finish()
            return
        }

        initView()
        delayLoadImage()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.zivWebImage.recycle()
    }

    ////////////////////
    fun dispatchOnBackEvent() {
        onBackPressedDispatcher.addCallback(
            this,
            onBackPressedCallback = object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    finish()
                }
            })
    }

    private fun delayLoadImage() {
        Handler(Looper.getMainLooper()).postDelayed(
            {
                loadImage()
            }, 100
        )
    }

    private fun initView() {
        if(delegate.localNightMode != MyApp.currentNightMode) {
            delegate.localNightMode = MyApp.currentNightMode
        }
        binding.zivWebImage.maxScale = 10.0f
        binding.zivWebImage.minScale = 0.1f
        //binding.pbLoadingImage.setProgressColor(R.color.color_6200EE)
    }

    private fun loadImage() {
        if (imageUrl.isEmpty()) {
            toast("图片连接无效！")
            return
        }
        binding.pbLoadingImage.visibility = View.VISIBLE
        val request = ImageRequest.Builder(this)
            .data(imageUrl)
            .target(
                onSuccess = {
                    val info = "图片加载成功"
                    Logger.i(TAG, "$info url=${imageUrl}")
                    binding.pbLoadingImage.visibility = View.GONE
                    binding.zivWebImage.setImage(
                        ImageSource.cachedBitmap(it.toBitmap()),
                        ImageViewState(zoomScale, PointF(0f, 0f), 0)
                    )
                },
                onError = {
                    //加载小图
                    val info = "图片加载失败"
                    Logger.i(TAG, "$info url=${imageUrl}")
                    toast(info)
                }
            )
            .build()
        imageLoader.enqueue(request)
    }

}
