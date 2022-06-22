package com.hrithik.prevue.di

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.*
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback
import com.hrithik.prevue.R
import java.util.*


class MyApplication : Application() {

    private var appOpenManager: AppOpenManager? = null

    override fun onCreate() {
        super.onCreate()

        MobileAds.initialize(this) {}

        appOpenManager = AppOpenManager(this)
    }

    private class AppOpenManager(myApplication: MyApplication) : ActivityLifecycleCallbacks {
        private var appOpenAd: AppOpenAd? = null

        private var loadCallback: AppOpenAdLoadCallback? = null

        private var myApplication: MyApplication? = null

        private var currentActivity: Activity? = null

        private var isShowingAd = false

        private var loadTime: Long = 0

        init {
            this.myApplication = myApplication
            this.myApplication!!.registerActivityLifecycleCallbacks(this)
            val observer = LifecycleEventObserver { _: LifecycleOwner?, event: Lifecycle.Event ->
                if (event == Lifecycle.Event.ON_START) {
                    showAdIfAvailable(currentActivity!!)
                }
            }
            ProcessLifecycleOwner.get().lifecycle.addObserver(observer)
        }

        private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean {
            val dateDifference = Date().time - loadTime
            val numMilliSecondsPerHour: Long = 3600000
            return dateDifference < numMilliSecondsPerHour * numHours
        }

        private val getAdRequest: AdRequest
            get() = AdRequest.Builder().build()

        private val isAdAvailable: Boolean
          get() = appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4)

        fun fetchAd(context: Context) {
            if (isAdAvailable) {
                return
            }
            loadCallback = object : AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    loadTime = Date().time
                    appOpenAd!!.show(currentActivity!!)
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {}
            }
            val request: AdRequest = getAdRequest
            AppOpenAd.load(
                myApplication!!, context.getString(R.string.admob_app_open), request,
                AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, loadCallback as AppOpenAdLoadCallback
            )
        }

        fun showAdIfAvailable(context: Context) {
            if (!isShowingAd && isAdAvailable) {
                val fullScreenContentCallback: FullScreenContentCallback =
                    object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            appOpenAd = null
                            isShowingAd = false
                            fetchAd(context)
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {}
                        override fun onAdShowedFullScreenContent() {
                            isShowingAd = true
                        }
                    }
                appOpenAd!!.fullScreenContentCallback = fullScreenContentCallback
            } else {
                fetchAd(context)
            }
        }

        override fun onActivityStarted(p0: Activity) {
            currentActivity = p0
        }

        override fun onActivityResumed(p0: Activity) {
            currentActivity = p0
        }

        override fun onActivityDestroyed(p0: Activity) {
            currentActivity = null
        }

        override fun onActivityCreated(p0: Activity, p1: Bundle?) {}
        override fun onActivityPaused(p0: Activity) {}
        override fun onActivityStopped(p0: Activity) {}
        override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {}

    }
}