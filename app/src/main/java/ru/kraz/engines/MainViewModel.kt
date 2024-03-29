package ru.kraz.engines

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(
    private val firestore: FirebaseFirestore,
) : ViewModel() {

    private var uuid = ""

    private var engines = mutableListOf<EngineUi>()
    private val _uiState = MutableLiveData<EnginesUiState>()
    val uiState: LiveData<EnginesUiState> get() = _uiState

    fun fetchEngines() = viewModelScope.launch(Dispatchers.IO) {
        _uiState.postValue(EnginesUiState.Loading)
        try {
            firestore.collection("items")
                .addSnapshotListener { snapshot, e ->
                    if (e != null) _uiState.postValue(EnginesUiState.Error)
                    else {
                        val list = mutableListOf<EngineUi>()
                        for (i in snapshot!!.documents) {
                            var item = i.toObject(EngineUi::class.java)!!
                            var likeIt = false
                            for (uuidLike in item.liked) {
                                if (uuid == uuidLike) {
                                    likeIt = true
                                    break
                                }
                            }
                            if (likeIt) item = item.copy(likeIt = true)
                            list.add(item)
                        }
                        val temp =
                            if (engines.isNotEmpty()) engines.toMutableList() else mutableListOf()
                        engines = list
                        temp.forEachIndexed { index, tempEngine ->
                            if (tempEngine.expanded) engines[index] =
                                engines[index].copy(expanded = true)
                            if (tempEngine.soundPlaying) engines[index] =
                                engines[index].copy(soundPlaying = true)
                        }
                        if (engines.isEmpty()) _uiState.postValue(EnginesUiState.NotFound)
                        else _uiState.postValue(EnginesUiState.Success(engines.toList()))
                    }
                }
        } catch (e: Exception) {
            _uiState.postValue(EnginesUiState.Error)
        }

    }

    fun like(position: Int) = viewModelScope.launch(Dispatchers.IO) {
        val num = if (engines[position].likeIt) {
            engines[position].liked.remove(uuid)
            -1L
        } else {
            engines[position].liked.add(uuid)
            1L
        }

        firestore.collection("items").document(engines[position].id)
            .update("liked", engines[position].liked)
        firestore.collection("items").document(engines[position].id)
            .update("countLike", FieldValue.increment(num))
    }

    fun expand(position: Int) {
        engines[position] = engines[position].copy(expanded = !engines[position].expanded)
        _uiState.postValue(EnginesUiState.Success(engines.toList()))
    }

    fun sound(position: Int) {
        for (index in 0..<engines.size) {
            if (position == index) engines[index] =
                engines[index].copy(soundPlaying = !engines[index].soundPlaying)
            else engines[index] = engines[index].copy(soundPlaying = false)
        }
        _uiState.postValue(EnginesUiState.Success(engines.toList()))
    }

    fun sound() {
        for (index in 0..<engines.size) {
            engines[index] = engines[index].copy(soundPlaying = false)
        }
        _uiState.postValue(EnginesUiState.Success(engines.toList()))
    }

    fun uuid(uuid: String) {
        this.uuid = uuid
    }
}