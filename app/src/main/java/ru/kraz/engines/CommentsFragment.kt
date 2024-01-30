package ru.kraz.engines

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.kraz.engines.databinding.FragmentCommentsBinding
import java.util.UUID

class CommentsFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentCommentsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CommentsViewModel by viewModel()

    private var engineId = ""

    private val adapter = CommentsAdapter { position ->
        viewModel.readComment(engineId, position)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        engineId = arguments?.getString(ENGINE_ID, "") ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCommentsBinding.inflate(inflater, container, false)
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

        binding.btnSend.setOnClickListener {
            val text = binding.inputMessage.text.toString()
            if (text != "") viewModel.sendComment(engineId, text)
            binding.inputMessage.setText("")
        }

        binding.informationContainer.btnRetry.setOnClickListener {
            viewModel.fetchComments(engineId)
        }

        binding.rvComments.adapter = adapter

        viewModel.uiState.observe(viewLifecycleOwner) {
            binding.informationContainer.loading.visibility = if (it is CommentsUiState.Loading) View.VISIBLE else View.GONE
            binding.informationContainer.containerError.visibility = if (it is CommentsUiState.Error) View.VISIBLE else View.GONE
            binding.sendContainer.visibility = if (it is CommentsUiState.Success || it is CommentsUiState.NotFound) View.VISIBLE else View.GONE
            binding.tvNoCommentsFound.visibility = if (it is CommentsUiState.NotFound) View.VISIBLE else View.GONE
            binding.rvComments.visibility = if (it is CommentsUiState.Success) View.VISIBLE else View.GONE
            if (it is CommentsUiState.Success) {
                adapter.submitList(it.list)
            }
        }

        viewModel.fetchComments(engineId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ENGINE_ID = "ENGINE_IO"
        fun newInstance(id: String) =
            CommentsFragment().apply {
                arguments = Bundle().apply {
                    putString(ENGINE_ID, id)
                }
            }
    }
}