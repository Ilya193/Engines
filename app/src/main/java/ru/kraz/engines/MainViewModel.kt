package ru.kraz.engines

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.getField
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(
    private val db: FirebaseFirestore,
) : ViewModel() {

    private var engines = mutableListOf<Engine>()
    private var uuid = ""

    private val _uiState = MutableLiveData<List<Engine>>()
    val uiState: LiveData<List<Engine>> get() = _uiState

    fun fetchEngines() = viewModelScope.launch(Dispatchers.IO) {
        db.collection("items")
            .addSnapshotListener { snapshot, e ->
                val list = mutableListOf<Engine>()
                for (i in snapshot!!.documents) {
                    list.add(i.toObject(Engine::class.java)!!)
                }
                for ((index, item) in list.withIndex()) {
                    var likeIt = false
                    for (uuidLike in item.liked) {
                        if (uuid == uuidLike) {
                            likeIt = true
                            break
                        }
                    }
                    if (likeIt) list[index] = item.copy(likeIt = true)

                }
                val temp = if (engines.isNotEmpty()) engines.toMutableList() else mutableListOf()
                engines = list
                temp.forEachIndexed { index, engine ->
                    if (engine.expanded) engines[index] = engines[index].copy(expanded = !engines[index].expanded)
                }
                _uiState.postValue(list.toList())
            }
    }

    fun like(position: Int) = viewModelScope.launch(Dispatchers.IO) {
        val num = if (engines[position].likeIt) {
            engines[position].liked.remove(uuid)
            -1L
        }
        else {
            engines[position].liked.add(uuid)
            1L
        }

        db.collection("items").document(engines[position].id)
            .update("liked", engines[position].liked)
        db.collection("items").document(engines[position].id)
            .update("countLike", FieldValue.increment(num))
    }

    fun expand(position: Int) {
        engines[position] = engines[position].copy(expanded = !engines[position].expanded)
        _uiState.postValue(engines.toList())
    }

    fun uuid(uuid: String) {
        this.uuid = uuid
    }
}

data class Engine(
    val id: String = "",
    val description: String = "",
    val countLike: Int = 0,
    val liked: MutableList<String> = mutableListOf(),
    val name: String = "",
    val photo: String = "",
    val likeIt: Boolean = false,
    val expanded: Boolean = false
)