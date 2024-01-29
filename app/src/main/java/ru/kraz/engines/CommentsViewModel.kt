package ru.kraz.engines

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CommentsViewModel(
    private val database: FirebaseDatabase
) : ViewModel() {

    private var uuid = ""

    private val comments = mutableListOf<CommentUi>()
    private val _uiState = MutableLiveData<List<CommentUi>>()
    val uiState: LiveData<List<CommentUi>> get() = _uiState

    private val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

    fun readComment(engineId: String, position: Int) = viewModelScope.launch(Dispatchers.IO) {
        val comment = comments[position].copy(messageRead = true).map()
        database.reference.child("comments/$engineId/${comment.id}").setValue(comment)
    }

    fun sendComment(engineId: String, text: String) = viewModelScope.launch(Dispatchers.IO) {
        val id = database.reference.child("comments/$engineId").push().key ?: ""
        database.reference.child("comments/$engineId/$id").setValue(CommentCloud(id, text, uuid))
    }

    fun fetchComments(engineId: String) = viewModelScope.launch(Dispatchers.IO) {
        database.reference.child("comments/$engineId").orderByChild("timestamp")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    comments.clear()
                    for (i in snapshot.children) {
                        val message = i.getValue(CommentCloud::class.java)
                        val date = Date(message!!.createdDate["timestamp"] as Long)
                        val formattedDate = sdf.format(date)
                        if (message.senderId == uuid) comments.add(message.map(formattedDate, true))
                        else comments.add(message.map(formattedDate, false))
                    }
                    _uiState.postValue(comments.toList())
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun uuid(uuid: String) {
        this.uuid = uuid
    }
}