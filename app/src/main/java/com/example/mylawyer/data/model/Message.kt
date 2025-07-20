package com.example.mylawyer.data.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import java.util.UUID

data class Message(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("chat_id") val chatId: String? = null,
    @SerializedName("user_id") val userId: String? = null,
    @SerializedName("user_message") val userMessage: String? = null,
    @SerializedName("bot_response") val botResponse: String? = null,
    @SerializedName("timestamp") val timestamp: String? = null,
    @SerializedName("metadata") val metadata: Map<String, Any>? = null,
    val text: String? = null,
    val isUser: Boolean = false,
    val tempId: String = UUID.randomUUID().toString(),
    val reaction: Int = 0 // 0 - нет реакции, 1 - лайк, 2 - дизлайк
) : Parcelable {
    constructor(parcel: Parcel) : this(
        id = parcel.readInt().takeIf { it != -1 },
        chatId = parcel.readString(),
        userId = parcel.readString(),
        userMessage = parcel.readString(),
        botResponse = parcel.readString(),
        timestamp = parcel.readString(),
        metadata = parcel.readValue(HashMap::class.java.classLoader) as? Map<String, Any>,
        text = parcel.readString(),
        isUser = parcel.readByte() != 0.toByte(),
        tempId = parcel.readString() ?: UUID.randomUUID().toString(),
        reaction = parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id ?: -1)
        parcel.writeString(chatId)
        parcel.writeString(userId)
        parcel.writeString(userMessage)
        parcel.writeString(botResponse)
        parcel.writeString(timestamp)
        parcel.writeValue(metadata)
        parcel.writeString(text)
        parcel.writeByte(if (isUser) 1 else 0)
        parcel.writeString(tempId)
        parcel.writeInt(reaction)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Message> {
        override fun createFromParcel(parcel: Parcel): Message = Message(parcel)
        override fun newArray(size: Int): Array<Message?> = arrayOfNulls(size)
    }
}