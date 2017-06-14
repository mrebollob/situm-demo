package com.mrebollob.situmdemo.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.support.v4.content.ContextCompat
import android.widget.Toast
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

fun Context.toast(text: String) = Toast.makeText(this, text, Toast.LENGTH_SHORT).show()


fun Context.getBitmapDescriptor(id: Int): BitmapDescriptor {
    val vectorDrawable = ContextCompat.getDrawable(this, id)
    val size = 70
    vectorDrawable.setBounds(0, 0, size, size)
    val bm = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bm)
    vectorDrawable.draw(canvas)
    return BitmapDescriptorFactory.fromBitmap(bm)
}