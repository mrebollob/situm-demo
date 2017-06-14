package com.mrebollob.situmdemo

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import com.mrebollob.situmdemo.utils.bindView


class MainActivity : AppCompatActivity() {

    val mapButton: Button by bindView(R.id.map_button)
    val mappingAppButton: Button by bindView(R.id.mapping_app_button)
    val situmLinkButton: Button by bindView(R.id.situm_link_button)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mapButton.setOnClickListener {
            MapsActivity.open(this)
        }

        mappingAppButton.setOnClickListener {
            openMappingApp()
        }

        situmLinkButton.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://situm.es/en"))
            startActivity(browserIntent)
        }
    }

    private fun openMappingApp() {
        val appPackageName = "es.situm.maps"

        try {
            val LaunchIntent = packageManager.getLaunchIntentForPackage(appPackageName)
            startActivity(LaunchIntent)
        } catch (anfe: ActivityNotFoundException) {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)))
            } catch (anfe: ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)))
            }
        }
    }
}