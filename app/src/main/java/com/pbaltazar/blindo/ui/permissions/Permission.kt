package com.pbaltazar.blindo.ui.permissions

import android.os.Parcel
import android.os.Parcelable
import androidx.core.view.ViewCompat

data class Permission(
    val id: Int = ViewCompat.generateViewId(),
    val name: String,
    val description: String
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(name)
        parcel.writeString(description)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Permission> {
        override fun createFromParcel(parcel: Parcel): Permission =
            Permission(parcel)

        override fun newArray(size: Int): Array<Permission?> =
            arrayOfNulls(size)
    }
}
