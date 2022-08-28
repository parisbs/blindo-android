package com.pbaltazar.blindo.entities.connections

import android.os.Parcel
import android.os.Parcelable
import com.pbaltazar.blindo.entities.Pack

data class PackConnection(
    val packs: List<Pack>? = null,
    val hasNextPage: Boolean = false,
    val nextPageToken: String? = null
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.createTypedArrayList(Pack),
        parcel.readByte() != 0.toByte(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeTypedList(packs)
        parcel.writeByte(if (hasNextPage) 1 else 0)
        parcel.writeString(nextPageToken)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<PackConnection> {
        override fun createFromParcel(parcel: Parcel): PackConnection = PackConnection(parcel)

        override fun newArray(size: Int): Array<PackConnection?> = arrayOfNulls(size)
    }
}
