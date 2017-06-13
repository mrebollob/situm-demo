package com.mrebollob.situmdemo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import com.mrebollob.situmdemo.utils.bindView


class MainActivity : AppCompatActivity() {

    val mapButton: Button by bindView(R.id.map_button)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mapButton.setOnClickListener {
            MapsActivity.open(this)
        }
    }
}