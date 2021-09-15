package com.pbaltazar.blindo.entities.connections

import android.os.Parcel
import android.os.Parcelable
import com.pbaltazar.blindo.entities.Rating

data class CommentConnection(
    val comments: List<Rating>? = null,
    val hasNextPage: Boolean = false,
    val nextPageToken: String? = null
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.createTypedArrayList(Rating),
        parcel.readByte() != 0.toByte(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeTypedList(comments)
        parcel.writeByte(if (hasNextPage) 1 else 0)
        parcel.writeString(nextPageToken)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<CommentConnection> {
        override fun createFromParcel(parcel: Parcel): CommentConnection = CommentConnection(parcel)

        override fun newArray(size: Int): Array<CommentConnection?> = arrayOfNulls(size)
    }
}
