package com.pbaltazar.blindo.entities

import android.os.Parcel
import android.os.Parcelable
import com.blindo.apollito.api.constants.DateTimePatterns
import com.blindo.apollito.utils.extensions.toApollitoString
import com.blindo.apollito.utils.extensions.toDate
import java.util.*

data class Rating(
    val id: String = "",
    val ui: Int = 0,
    val screenreaders: Int = 0,
    val labels: Int = 0,
    val functions: Int = 0,
    val performance: Int = 0,
    val total: Float? = null,
    val comment: String? = null,
    val commentLanguage: String? = null,
    val user: User? = null,
    val app: App? = null,
    val createdAt: Date = Date(System.currentTimeMillis()),
    val updatedAt: Date? = null
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readValue(Float::class.java.classLoader) as? Float,
        parcel.readString(),
        parcel.readString(),
        parcel.readParcelable(User::class.java.classLoader),
        parcel.readParcelable(App::class.java.classLoader),
        parcel.readString()?.toDate(DateTimePatterns.ISO8601_MICROS_TZ.pattern) ?: Date(System.currentTimeMillis()),
        parcel.readString()?.toDate(DateTimePatterns.ISO8601_MICROS_TZ.pattern)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeValue(ui)
        parcel.writeValue(screenreaders)
        parcel.writeValue(labels)
        parcel.writeValue(functions)
        parcel.writeValue(performance)
        parcel.writeValue(total)
        parcel.writeString(comment)
        parcel.writeString(commentLanguage)
        parcel.writeParcelable(user, flags)
        parcel.writeParcelable(app, flags)
        parcel.writeString(createdAt.toApollitoString(DateTimePatterns.ISO8601_MICROS_TZ.pattern))
        parcel.writeString(updatedAt?.toApollitoString(DateTimePatterns.ISO8601_MICROS_TZ.pattern))
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Rating> {
        override fun createFromParcel(parcel: Parcel): Rating = Rating(parcel)

        override fun newArray(size: Int): Array<Rating?> = arrayOfNulls(size)
    }
}
