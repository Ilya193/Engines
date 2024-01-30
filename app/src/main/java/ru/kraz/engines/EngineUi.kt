package ru.kraz.engines

data class EngineUi(
    val id: String = "",
    val description: String = "",
    val countLike: Int = 0,
    val liked: MutableList<String> = mutableListOf(),
    val name: String = "",
    val images: List<String> = mutableListOf(),
    val sound: String = "",
    val likeIt: Boolean = false,
    val expanded: Boolean = false,
    val soundPlaying: Boolean = false,
)