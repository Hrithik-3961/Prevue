package com.hrithik.prevue

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.hrithik.prevue.databinding.FragmentEditBinding
import kotlinx.coroutines.flow.collect

class EditFragment : Fragment(R.layout.fragment_edit) {

    private var _binding: FragmentEditBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditBinding.inflate(layoutInflater, container, false)
        binding.viewModel = viewModel

        viewModel.image.value = navArgs<EditFragmentArgs>().value.image

        viewModel.image.observe(viewLifecycleOwner) { image ->
            binding.imageView.setImageBitmap(image.bitmap)
        }

        setFragmentResultListener("crop_request") { _, bundle ->
            val result = bundle.get("crop_result") as Image
            viewModel.image.value = result
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.editEvents.collect { event ->
                when (event) {
                    is EditViewModel.EditEvent.NavigateToCropScreen -> {
                        val action =
                            EditFragmentDirections.actionEditFragmentToCropFragment(event.image)
                        findNavController().navigate(action)
                    }
                }
            }
        }

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}


