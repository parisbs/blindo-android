package com.pbaltazar.blindo.entities

import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import com.pbaltazar.blindo.entities.connections.PackConnection
import com.pbaltazar.blindo.entities.connections.RatingConnection

data class User(
    val id: String = "",
    val sub: String = "",
    val email: String = "",
    val name: String = "",
    val picture: String? = null,
    val isVerified: Boolean = false,
    val isPremium: Boolean = false,
    val numberOfPacks: String = "0",
    val numberOfRatings: String = "0",
    val packs: PackConnection? = null,
    val ratings: RatingConnection? = null
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readString() ?: "0",
        parcel.readString() ?: "0",
        parcel.readParcelable(PackConnection::class.java.classLoader),
        parcel.readParcelable(RatingConnection::class.java.classLoader)
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(sub)
        parcel.writeString(email)
        parcel.writeString(name)
        parcel.writeString(picture)
        parcel.writeByte(if (isVerified) 1 else 0)
        parcel.writeByte(if (isPremium) 1 else 0)
        parcel.writeString(numberOfPacks)
        parcel.writeString(numberOfRatings)
        parcel.writeParcelable(packs, flags)
        parcel.writeParcelable(ratings, flags)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User = User(parcel)

        override fun newArray(size: Int): Array<User?> = arrayOfNulls(size)
    }

    override fun equals(other: Any?): Boolean = (other as? User)?.let { otherUser ->
        TextUtils.equals(this.id, otherUser.id) &&
            TextUtils.equals(this.sub, otherUser.sub) &&
            TextUtils.equals(this.email, otherUser.email) &&
            TextUtils.equals(this.name, otherUser.name) &&
            TextUtils.equals(this.picture, otherUser.picture) &&
            this.isVerified.equals(otherUser.isVerified) &&
            this.isPremium.equals(otherUser.isPremium)
    } ?: false
}
