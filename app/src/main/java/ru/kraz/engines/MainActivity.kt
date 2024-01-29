package ru.kraz.engines

import android.content.Context
import android.media.MediaPlayer
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import coil.ImageLoader
import coil.decode.GifDecoder
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.kraz.engines.databinding.ActivityMainBinding
import java.util.UUID


class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val viewModel: MainViewModel by viewModel()

    private lateinit var connectivityManager: ConnectivityManager
    private val networkCallback: NetworkCallback = object : NetworkCallback() {
        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities,
        ) {
            super.onCapabilitiesChanged(network, networkCapabilities)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                internetConnection =
                    networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                            && networkCapabilities.hasCapability(
                        NetworkCapabilities.NET_CAPABILITY_VALIDATED
                    )
            }
        }
    }
    private var internetConnection = false

    private var musicPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        setContentView(binding.root)

        val sharedPreferences = getSharedPreferences("data", Context.MODE_PRIVATE)
        val data = sharedPreferences.getString("uuid", null)
        if (data == null) {
            sharedPreferences.edit().putString("uuid", UUID.randomUUID().toString()).apply()
            viewModel.uuid(sharedPreferences.getString("uuid", "") ?: "")
        } else viewModel.uuid(data)

        connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

        val imageLoader = ImageLoader.Builder(this)
            .components {
                add(GifDecoder.Factory())
            }
            .build()

        val adapter = EnginesAdapter(imageLoader, like = { position ->
            viewModel.like(position)
        }, expand = { position ->
            viewModel.expand(position)
        }, chat = { id ->
            CommentsFragment.newInstance(id).show(supportFragmentManager, null)
        }, soundAction = { position, engine ->
            viewModel.sound(position)
            if (engine.soundPlaying) {
                stopSound()
            } else {
                if (musicPlayer != null)
                    stopSound()
                musicPlayer = MediaPlayer()
                musicPlayer?.setDataSource(engine.sound)
                musicPlayer?.prepareAsync()
                musicPlayer?.setOnPreparedListener {
                    musicPlayer?.start()
                }
                musicPlayer?.setOnCompletionListener {
                    stopSound()
                    viewModel.sound()
                }
            }
        })

        binding.engines.adapter = adapter
        binding.engines.setHasFixedSize(true)

        viewModel.uiState.observe(this) {
            adapter.submitList(it)
        }
    }

    private fun stopSound() {
        musicPlayer?.release()
        musicPlayer = null
    }

    override fun onStart() {
        super.onStart()
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchEngines()
    }

    override fun onPause() {
        super.onPause()
        stopSound()
        viewModel.sound()
    }

    override fun onStop() {
        super.onStop()
        connectivityManager.unregisterNetworkCallback(networkCallback)
        internetConnection = false
    }
}