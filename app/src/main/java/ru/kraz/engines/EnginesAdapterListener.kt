package ru.kraz.engines

interface EnginesAdapterListener {
    fun onLikeClicked(position: Int)
    fun onExpandClicked(position: Int)
    fun openComments(id: String)
    fun onSoundAction(position: Int, engine: EngineUi)
}