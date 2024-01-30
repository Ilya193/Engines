package ru.kraz.engines

data class CommentUi(
    val id: String,
    val message: String,
    val senderId: String,
    val createdDate: String,
    val createdDateMap: Map<String, Any>,
    val iSendThis: Boolean = false,
    val messageRead: Boolean = false,
) {
    fun map(): CommentCloud = CommentCloud(
        id,
        message,
        senderId,
        createdDateMap,
        messageRead
    )
}

sealed interface CommentsUiState {
    data class Success(val list: List<CommentUi>) : CommentsUiState
    data object NotFound : CommentsUiState
}