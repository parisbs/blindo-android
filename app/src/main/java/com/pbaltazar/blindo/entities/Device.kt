package com.pbaltazar.blindo.entities

import android.os.Parcel
import android.os.Parcelable

data class Device(
    val id: String = "",
    val hardwareFingerprint: String = "",
    val gcmToken: String? = null,
    val name: String = "",
    val language: String = "",
    val country: String = ""
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(hardwareFingerprint)
        parcel.writeString(gcmToken)
        parcel.writeString(name)
        parcel.writeString(language)
        parcel.writeString(country)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Device> {
        override fun createFromParcel(parcel: Parcel): Device = Device(parcel)

        override fun newArray(size: Int): Array<Device?> = arrayOfNulls(size)
    }
}
