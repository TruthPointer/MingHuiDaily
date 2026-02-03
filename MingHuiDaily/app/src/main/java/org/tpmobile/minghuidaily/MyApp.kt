package org.tpmobile.minghuidaily

import android.app.Application
import java.net.Proxy
import java.net.InetSocketAddress
import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import org.tpmobile.minghuidaily.util.PREF_PROXY_PORT
import org.tpmobile.minghuidaily.util.PREF_THEME_NIGHT
import org.tpmobile.minghuidaily.util.PROXY_PORT_FREEGATE
import org.tpmobile.minghuidaily.util.PROXY_PORT_HOST
import org.tpmobile.minghuidaily.util.ktx.getPref
import org.tpmobile.minghuidaily.util.ktx.setPref

class MyApp : Application() {

    companion object {
        lateinit var appContext: Context
        var USE_PROXY = true
        var proxyHost: String = PROXY_PORT_HOST//19966 //20210917 由自由門的代理改為使用無界一點通的代理
        var proxyPort: Int = PROXY_PORT_FREEGATE
        var currentNightMode = AppCompatDelegate.MODE_NIGHT_NO

        @JvmField
        var proxy: Proxy = Proxy(
            Proxy.Type.HTTP,
            InetSocketAddress(proxyHost, proxyPort)
        )

        fun setProxy(port: Int) {
            if (port != proxyPort) {
                setPref(PREF_PROXY_PORT, port)
                proxyPort = port
                proxy = Proxy(
                    Proxy.Type.HTTP,
                    InetSocketAddress(proxyHost, proxyPort)
                )
            }
        }
    }

    override fun onCreate() {
        if (USE_PROXY) {
            initProxy()
        }
        super.onCreate()
        appContext = applicationContext
        checkNightMode()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    ///////////////////////////////////////////////////
    private fun initProxy() {
        proxyPort = getPref(PREF_PROXY_PORT, PROXY_PORT_FREEGATE)
        //1.
        System.getProperties().apply {
            //1. OK
            //etProperty("proxySet", "true")
            //etProperty("proxyHost", PROXY_HOST)
            //etProperty("proxyPort", PROXY_PORT.toString())

            //2. 也许更好
            setProperty("http.proxyHost", proxyHost) //http.proxyHost
            setProperty("http.proxyPort", proxyPort.toString()) //http.proxyPort
            setProperty("https.proxyHost", proxyHost) //http.proxyHost
            setProperty("https.proxyPort", proxyPort.toString()) //http.proxyPort
        }
        //2.
        setProxy(proxyPort)
    }

    private fun checkNightMode() {
        currentNightMode =
            getPref(PREF_THEME_NIGHT, AppCompatDelegate.MODE_NIGHT_NO)
        if (currentNightMode == AppCompatDelegate.MODE_NIGHT_YES) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

}