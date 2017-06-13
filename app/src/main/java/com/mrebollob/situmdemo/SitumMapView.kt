package com.mrebollob.situmdemo

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.properties.Delegates


class Point constructor(val x: Float, val y: Float)

class SitumMapView : View {

    val paint = Paint()
    var point: Point by Delegates.observable(Point(0f, 0f))
    { _, _, _ -> invalidate() }
    var mapImage: Bitmap by Delegates.observable(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
    { _, _, _ -> invalidate() }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        paint.color = Color.RED
        paint.strokeWidth = 15f
        paint.style = Paint.Style.STROKE

        canvas.drawBitmap(mapImage, 0f, 0f, paint)
        canvas.drawCircle(point.x, point.y, 50f, paint)
    }
}