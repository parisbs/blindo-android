package com.pbaltazar.blindo.entities

import android.os.Parcel
import android.os.Parcelable
import com.pbaltazar.blindo.entities.connections.PackConnection
import com.pbaltazar.blindo.entities.connections.RatingConnection

data class App(
    val id: String = "",
    val packageName: String = "",
    val packageIcon: String? = null,
    val packageLabel: String = "",
    val category: String = "",
    val uiRating: Float? = null,
    val screenreadersRating: Float? = null,
    val labelsRating: Float? = null,
    val functionsRating: Float? = null,
    val performanceRating: Float? = null,
    val totalRating: Float? = null,
    val numberOfRatings: Int = 0,
    val availablePacks: Int = 0,
    val packs: PackConnection? = null,
    val ratings: RatingConnection? = null,
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readValue(Float::class.java.classLoader) as? Float,
        parcel.readValue(Float::class.java.classLoader) as? Float,
        parcel.readValue(Float::class.java.classLoader) as? Float,
        parcel.readValue(Float::class.java.classLoader) as? Float,
        parcel.readValue(Float::class.java.classLoader) as? Float,
        parcel.readValue(Float::class.java.classLoader) as? Float,
        parcel.readInt(),
        parcel.readInt(),
        parcel.readParcelable(PackConnection::class.java.classLoader),
        parcel.readParcelable(RatingConnection::class.java.classLoader)
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(packageName)
        parcel.writeString(packageIcon)
        parcel.writeString(packageLabel)
        parcel.writeString(category)
        parcel.writeValue(uiRating)
        parcel.writeValue(screenreadersRating)
        parcel.writeValue(labelsRating)
        parcel.writeValue(functionsRating)
        parcel.writeValue(performanceRating)
        parcel.writeValue(totalRating)
        parcel.writeInt(numberOfRatings)
        parcel.writeInt(availablePacks)
        parcel.writeParcelable(packs, flags)
        parcel.writeParcelable(ratings, flags)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<App> {
        override fun createFromParcel(parcel: Parcel): App = App(parcel)

        override fun newArray(size: Int): Array<App?> = arrayOfNulls(size)
    }
}
