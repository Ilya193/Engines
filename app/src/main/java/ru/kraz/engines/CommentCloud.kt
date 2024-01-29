package ru.kraz.engines

import com.google.firebase.database.ServerValue

data class CommentCloud(
    val id: String = "",
    val message: String = "",
    val senderId: String = "",
    val createdDate: Map<String, Any> = mapOf("timestamp" to ServerValue.TIMESTAMP),
    val messageRead: Boolean = false,
) {
    fun map(formattedDate: String, iSendThis: Boolean): CommentUi =
        CommentUi(
            id,
            message,
            senderId,
            formattedDate,
            createdDate,
            iSendThis,
            messageRead
        )
}