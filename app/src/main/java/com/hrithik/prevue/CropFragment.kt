package com.hrithik.prevue

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.hrithik.prevue.databinding.FragmentCropBinding
import kotlinx.coroutines.flow.collect

class CropFragment: Fragment(R.layout.fragment_crop) {
    private var _binding: FragmentCropBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CropViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCropBinding.inflate(layoutInflater, container, false)
        binding.viewModel = viewModel

        viewModel.image.value = navArgs<EditFragmentArgs>().value.image

        viewModel.image.observe(viewLifecycleOwner) { image ->
            binding.cropImageView.setImageBitmap(image.bitmap)
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.cropEvents.collect { event ->
                when (event) {
                    is CropViewModel.CropEvent.GetCroppedImage -> {
                        val bitmap = binding.cropImageView.croppedImage
                        val img = viewModel.image.value!!
                        img.bitmap = bitmap
                        viewModel.image.value = img
                    }
                    is CropViewModel.CropEvent.NavigateBackWithResult -> {
                        setFragmentResult(
                            "crop_request",
                            bundleOf("crop_result" to event.image)
                        )
                        findNavController().popBackStack()
                    }
                }
            }
        }

        return binding.root
    }
}