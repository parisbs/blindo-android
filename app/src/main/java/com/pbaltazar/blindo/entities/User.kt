package com.pbaltazar.blindo.entities

import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils

data class User(
    val id: String = "",
    val sub: String = "",
    val email: String = "",
    val name: String = "",
    val picture: String? = null,
    val isVerified: Boolean = false,
    val isPremium: Boolean = false
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte()
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
