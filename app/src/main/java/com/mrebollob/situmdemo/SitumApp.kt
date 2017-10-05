package com.mrebollob.situmdemo

import android.app.Application
import com.applivery.applvsdklib.Applivery
import es.situm.sdk.SitumSdk


class SitumApp : Application() {


    override fun onCreate() {
        super.onCreate()

        initApplivery()
        SitumSdk.init(this)
    }

    private fun initApplivery() {
        Applivery.init(this, BuildConfig.APPLIVERY_APP_ID, BuildConfig.APPLIVERY_API_KEY, false)
        Applivery.setUpdateCheckingInterval(30)
    }
}