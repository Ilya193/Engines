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
import android.view.Menu
import android.view.MenuItem
import android.view.View
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

    /*private lateinit var connectivityManager: ConnectivityManager
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
    private var internetConnection = false*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
    }

    /*override fun onStart() {
        super.onStart()
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    override fun onStop() {
        super.onStop()
        connectivityManager.unregisterNetworkCallback(networkCallback)
        internetConnection = false
    }*/

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.manu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            R.id.createEngine -> {
                val currentFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
                if (currentFragment !is CreateEngineFragment) supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, CreateEngineFragment.newInstance())
                    .addToBackStack(null)
                    .setCustomAnimations(
                        R.anim.slide_in,
                        R.anim.fade_out,
                        R.anim.fade_in,
                        R.anim.slide_out
                    )
                    .commit()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}