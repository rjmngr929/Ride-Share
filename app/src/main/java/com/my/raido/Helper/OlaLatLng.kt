package com.my.raido.Helper

import android.os.Parcel
import android.os.Parcelable

data class PassOlaLatLng(val latitude: Double, val longitude: Double) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readDouble(),
        parcel.readDouble()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PassOlaLatLng> {
        override fun createFromParcel(parcel: Parcel): PassOlaLatLng {
            return PassOlaLatLng(parcel)
        }

        override fun newArray(size: Int): Array<PassOlaLatLng?> {
            return arrayOfNulls(size)
        }
    }
}