package ru.kraz.engines

import android.app.Application
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(module)
        }
    }
}

val module = module {
    viewModel<MainViewModel> {
        MainViewModel(get())
    }

    viewModel<CommentsViewModel> {
        CommentsViewModel(get())
    }

    viewModel<CreateViewModel> {
        CreateViewModel(get(), get())
    }

    single<FirebaseFirestore> {
        FirebaseFirestore.getInstance()
    }

    single<FirebaseDatabase> {
        FirebaseDatabase.getInstance()
    }

    single<FirebaseStorage> {
        FirebaseStorage.getInstance()
    }
}