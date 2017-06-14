package com.mrebollob.situmdemo.utils

import android.content.Context
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.mrebollob.situmdemo.R
import es.situm.sdk.model.cartography.Poi
import es.situm.sdk.model.cartography.Point
import es.situm.sdk.model.location.Location

fun Poi.toLatLng() = LatLng(this.coordinate.latitude, this.coordinate.longitude)

fun Poi.getIcon(context: Context): BitmapDescriptor {
    when (this.customFields["icon"]) {
        "android" -> return context.getBitmapDescriptor(R.drawable.ic_android)
        "ios" -> return context.getBitmapDescriptor(R.drawable.ic_iphone)
        "coffee" -> return context.getBitmapDescriptor(R.drawable.ic_coffee)
        else -> return context.getBitmapDescriptor(R.drawable.ic_place)
    }
}

fun Point.toLatLng() = LatLng(this.coordinate.latitude, this.coordinate.longitude)

fun Location.toLatLng() = LatLng(this.coordinate.latitude, this.coordinate.longitude)
