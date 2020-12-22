package com.google.mlkit.vision.camerasample.base


import androidx.multidex.MultiDexApplication
import com.google.mlkit.vision.camerasample.BuildConfig
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber


import timber.log.Timber.DebugTree


@HiltAndroidApp
class BaseApplication : MultiDexApplication(){


    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }
    }

}