package com.mrebollob.situmdemo

import android.app.Application
import es.situm.sdk.SitumSdk


class SitumApp : Application() {


    override fun onCreate() {
        super.onCreate()

        SitumSdk.init(this)
    }
}