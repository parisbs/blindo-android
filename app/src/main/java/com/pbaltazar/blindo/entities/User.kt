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
    val coinsLeft: Int = 0,
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
        parcel.readInt(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readString() ?: "0",
        parcel.readString() ?: "0",
        parcel.readParcelable(PackConnection::class.java.classLoader),
        parcel.readParcelable(RatingConnection::class.java.classLoader)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(sub)
        parcel.writeString(email)
        parcel.writeString(name)
        parcel.writeString(picture)
        parcel.writeInt(coinsLeft)
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
            this.coinsLeft == otherUser.coinsLeft &&
            this.isVerified == otherUser.isVerified &&
            this.isPremium == otherUser.isPremium
    } ?: false

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + sub.hashCode()
        result = 31 * result + email.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + (picture?.hashCode() ?: 0)
        result = 31 * result + coinsLeft
        result = 31 * result + isVerified.hashCode()
        result = 31 * result + isPremium.hashCode()
        result = 31 * result + numberOfPacks.hashCode()
        result = 31 * result + numberOfRatings.hashCode()
        result = 31 * result + (packs?.hashCode() ?: 0)
        result = 31 * result + (ratings?.hashCode() ?: 0)
        return result
    }
}
