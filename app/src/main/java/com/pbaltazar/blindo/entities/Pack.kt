package com.pbaltazar.blindo.entities

import android.os.Parcel
import android.os.Parcelable
import com.wizeline.simpleapollo.api.constants.DateTimePatterns
import com.wizeline.simpleapollo.utils.extensions.toDate
import com.wizeline.simpleapollo.utils.extensions.toSimpleApolloString
import java.util.*

data class Pack(
    val id: String = "",
    val numberOfLabels: Int = 0,
    val downloads: String = "",
    val language: String = "",
    val hash: String = "",
    val user: User? = null,
    val app: App? = null,
    val createdAt: Date = Date(System.currentTimeMillis()),
    val updatedAt: Date? = null
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readParcelable(User::class.java.classLoader),
        parcel.readParcelable(App::class.java.classLoader),
        parcel.readString()?.toDate(DateTimePatterns.ISO8601_MICROS_TZ.pattern) ?: Date(System.currentTimeMillis()),
        parcel.readString()?.toDate(DateTimePatterns.ISO8601_MICROS_TZ.pattern)
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeValue(numberOfLabels)
        parcel.writeString(downloads)
        parcel.writeString(language)
        parcel.writeString(hash)
        parcel.writeParcelable(user, flags)
        parcel.writeParcelable(app, flags)
        parcel.writeString(createdAt.toSimpleApolloString(DateTimePatterns.ISO8601_MICROS_TZ.pattern))
        parcel.writeString(updatedAt?.toSimpleApolloString(DateTimePatterns.ISO8601_MICROS_TZ.pattern))
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Pack> {
        override fun createFromParcel(parcel: Parcel): Pack = Pack(parcel)

        override fun newArray(size: Int): Array<Pack?> = arrayOfNulls(size)
    }
}
