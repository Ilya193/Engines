package ru.kraz.engines

import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.kraz.engines.databinding.FragmentCreateEngineBinding
import java.io.File
import java.util.UUID

class CreateEngineFragment : Fragment() {

    private var _binding: FragmentCreateEngineBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CreateViewModel by viewModel()

    private val adapter = SelectedImageAdapter { position ->
        selectedImages.removeAt(position)
        if (selectedImages.isEmpty()) binding.tvInformation.visibility = View.VISIBLE
        submitList()
    }
    private val selectedImages = mutableListOf<SelectedImage>()

    private var uriSound: Uri? = null

    private fun submitList() {
        adapter.submitList(selectedImages.toList())
    }

    private val pickSound = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val cursor = context?.contentResolver?.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val data = it.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)
                    val displayName = it.getString(data)
                    binding.tvNameSound.text = displayName
                    binding.deleteSound.visibility = View.VISIBLE
                    binding.btnActionSound.visibility = View.VISIBLE
                }
                this.uriSound = uri
            }
        }
    }

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            binding.tvInformation.visibility = View.GONE
            selectedImages.add(SelectedImage(uri))
            adapter.submitList(selectedImages.toList())
        }
    }

    private val makeImage: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { r ->
            if (r.resultCode == Activity.RESULT_OK) {
                binding.tvInformation.visibility = View.GONE
                selectedImages.add(SelectedImage(uriImage ?: Uri.parse("")))
                adapter.submitList(selectedImages.toList())
            }
            uriImage = null
        }

    private var uriImage: Uri? = null

    private var mediaPlayer: MediaPlayer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCreateEngineBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSelectSound.setOnClickListener {
            pickSound.launch("audio/*")
        }

        binding.deleteSound.setOnClickListener {
            binding.tvNameSound.text = ""
            binding.deleteSound.visibility = View.GONE
            binding.btnActionSound.visibility = View.GONE
            uriSound = null
            stopSound()
        }

        binding.btnActionSound.setOnClickListener {
            if (mediaPlayer == null) {
                binding.btnActionSound.setImageResource(R.drawable.pause)
                mediaPlayer = MediaPlayer()
                mediaPlayer?.setDataSource(requireContext(), uriSound ?: Uri.parse(""))
                mediaPlayer?.prepare()
                mediaPlayer?.start()
                mediaPlayer?.setOnCompletionListener {
                    stopSound()
                }
            }
            else {
                stopSound()
            }
        }

        binding.btnSelectImage.setOnClickListener {
            if (selectedImages.size <= 1)
                pickImage.launch("image/*")
            else Snackbar.make(binding.root,
            getString(R.string.information_max_image), Snackbar.LENGTH_SHORT).show()
        }

        binding.btnMakeImage.setOnClickListener {
            if (selectedImages.size <= 1) {
                val tempFile = File(requireContext().cacheDir, "${UUID.randomUUID()}.jpg")
                uriImage = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().packageName + ".provider",
                    tempFile
                )
                val makeImageIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                makeImageIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriImage)
                makeImage.launch(makeImageIntent)
            } else Snackbar.make(binding.root,
                getString(R.string.information_max_image), Snackbar.LENGTH_SHORT).show()
        }

        binding.btnCreatePost.setOnClickListener {
            val type = binding.inputTypeEngine.text.toString()
            val description = binding.inputInformation.text.toString()

            if (type.isNotEmpty() && description.isNotEmpty() && uriSound != null && selectedImages.isNotEmpty()) {
                viewModel.createPost(type, description, uriSound!!, selectedImages)
                binding.content.visibility = View.GONE
            } else Snackbar.make(binding.root,
                getString(R.string.all_fiels_must_be_filled), Snackbar.LENGTH_SHORT).show()
        }

        binding.btnRetry.setOnClickListener {
            binding.containerError.visibility = View.GONE
            binding.content.visibility = View.VISIBLE
        }

        binding.viewPager.adapter = adapter

        viewModel.uiState.observe(viewLifecycleOwner) {
            binding.loading.visibility = if (it is CreatePostState.Loading) View.VISIBLE else View.GONE
            binding.containerError.visibility = if (it is CreatePostState.Error) View.VISIBLE else View.GONE
            if (it is CreatePostState.Success) parentFragmentManager.popBackStack()
        }
    }

    private fun stopSound() {
        mediaPlayer?.release()
        mediaPlayer = null
        binding.btnActionSound.setImageResource(R.drawable.play)
    }

    override fun onPause() {
        super.onPause()
        stopSound()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = CreateEngineFragment()
    }
}