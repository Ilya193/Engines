package ru.kraz.engines

import android.content.Context
import android.media.MediaPlayer
import android.net.ConnectivityManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import coil.ImageLoader
import coil.decode.GifDecoder
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.kraz.engines.databinding.FragmentCreateEngineBinding
import ru.kraz.engines.databinding.FragmentEnginesBinding
import java.util.UUID

class EnginesFragment : Fragment(), EnginesAdapterListener {

    private var _binding: FragmentEnginesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by viewModel()

    private var musicPlayer: MediaPlayer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEnginesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sharedPreferences = context?.getSharedPreferences("data", Context.MODE_PRIVATE)
        val data = sharedPreferences?.getString("uuid", null)
        if (data == null) {
            sharedPreferences?.edit()?.putString("uuid", UUID.randomUUID().toString())?.apply()
            viewModel.uuid(sharedPreferences?.getString("uuid", "") ?: "")
        } else viewModel.uuid(data)

        val imageLoader = ImageLoader.Builder(requireContext())
            .components {
                add(GifDecoder.Factory())
            }
            .build()

        val adapter = EnginesAdapter(imageLoader, this)

        binding.engines.adapter = adapter
        binding.engines.setHasFixedSize(true)

        binding.informationContainer.btnRetry.setOnClickListener {
            viewModel.fetchEngines()
        }

        viewModel.uiState.observe(viewLifecycleOwner) {
            binding.informationContainer.loading.visibility = if (it is EnginesUiState.Loading) View.VISIBLE else View.GONE
            binding.informationContainer.containerError.visibility = if (it is EnginesUiState.Error) View.VISIBLE else View.GONE
            binding.tvNoPostsFound.visibility = if (it is EnginesUiState.NotFound) View.VISIBLE else View.GONE
            binding.engines.visibility = if (it is EnginesUiState.Success) View.VISIBLE else View.GONE
            if (it is EnginesUiState.Success) {
                adapter.submitList(it.list)
            }
        }
    }

    private fun stopSound() {
        musicPlayer?.release()
        musicPlayer = null
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

    override fun onLikeClicked(position: Int) {
        viewModel.like(position)
    }

    override fun onExpandClicked(position: Int) {
        viewModel.expand(position)
    }

    override fun openComments(id: String) {
        CommentsFragment.newInstance(id).show(parentFragmentManager, null)
    }

    override fun onSoundAction(position: Int, engine: EngineUi) {
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = EnginesFragment()
    }
}