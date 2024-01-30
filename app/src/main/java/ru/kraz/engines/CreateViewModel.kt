package ru.kraz.engines

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CreateViewModel(
    private val storage: FirebaseStorage,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private var downloadUriSound = ""
    private var downloadUriImages = listOf<String>()

    private val _uiState = MutableLiveData<CreatePostState>()
    val uiState: LiveData<CreatePostState> get() = _uiState

    fun createPost(
        type: String,
        description: String,
        uriSound: Uri,
        selectedImages: List<SelectedImage>
    ) = viewModelScope.launch(Dispatchers.IO) {
        _uiState.postValue(CreatePostState.Loading)
        try {
            upload(uriSound, selectedImages).join()
            val document = firestore.collection("items").document()
            document.set(
                EngineCloud(
                    id = document.id,
                    name = type,
                    description = description,
                    sound = downloadUriSound,
                    images = downloadUriImages
                )
            ).addOnSuccessListener {
                _uiState.postValue(CreatePostState.Success)
            }
        } catch (e: Exception) {
            _uiState.postValue(CreatePostState.Error)
        }
    }

    private suspend fun upload(sound: Uri, selectedImages: List<SelectedImage>) =
        viewModelScope.launch(Dispatchers.IO) {
            val uriSound = async {
                val ref = storage.reference.child("upload/${sound.lastPathSegment}")
                ref.putFile(sound).await()
                val uri = ref.downloadUrl.await()
                uri
            }

            val uriImages = async {
                val uris = mutableListOf<Uri>()
                for (i in selectedImages.indices) {
                    val ref =
                        storage.reference.child("upload/$${selectedImages[i].uri.lastPathSegment}")
                    ref.putFile(selectedImages[i].uri).await()
                    val uri = ref.downloadUrl.await()
                    uris.add(uri)
                }
                uris
            }

            downloadUriSound = uriSound.await().toString()

            downloadUriImages = uriImages.await().map {
                it.toString()
            }
        }
}

sealed interface CreatePostState {
    data object Success : CreatePostState
    data object Loading : CreatePostState
    data object Error : CreatePostState
}