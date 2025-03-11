package com.my.raido.ui.home.bookRide

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.my.raido.R

fun createCustomMarker(
    context: Context,
    labelText: String
): BitmapDescriptor {
    // Inflate the custom layout
    val markerView = LayoutInflater.from(context).inflate(R.layout.marker_text_layout, null)

    // Set the label text
    val markerText = markerView.findViewById<TextView>(R.id.marker_text)
    markerText.text = labelText

    // Measure and layout the view
    markerView.measure(
        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    )
    markerView.layout(0, 0, markerView.measuredWidth, markerView.measuredHeight)

    // Create a Bitmap from the view
    val bitmap = Bitmap.createBitmap(
        markerView.measuredWidth,
        markerView.measuredHeight,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    markerView.draw(canvas)

    // Convert to BitmapDescriptor
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}
