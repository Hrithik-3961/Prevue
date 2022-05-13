package com.hrithik.prevue.ui.crop

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
import com.google.android.material.snackbar.Snackbar
import com.hrithik.prevue.R
import com.hrithik.prevue.databinding.FragmentCropBinding
import com.hrithik.prevue.util.Constants
import com.hrithik.prevue.util.Response
import com.hrithik.prevue.util.Status
import kotlinx.coroutines.flow.collect


class CropFragment : Fragment(R.layout.fragment_crop) {
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
        val bitmap = navArgs<CropFragmentArgs>().value.bitmap
        viewModel.bitmap.value = Response.success(bitmap)

        viewModel.bitmap.observe(viewLifecycleOwner) { response ->
            when(response.status) {
                Status.SUCCESS -> {
                    binding.cropImageView.setImageBitmap(response.data)
                }
                Status.ERROR -> {
                    Snackbar.make(requireView(), response.message, Snackbar.LENGTH_SHORT).show()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.cropEvents.collect { event ->
                when (event) {
                    is CropViewModel.CropEvent.GetCroppedImage -> {
                        val bmp = binding.cropImageView.croppedImage
                        if (bmp != null) {
                            viewModel.bitmap.value = Response.success(bmp)
                            viewModel.onImageCropped()
                        } else {
                            viewModel.bitmap.value = Response.error("Error occurred in cropping the image")
                        }
                    }
                    is CropViewModel.CropEvent.NavigateBackWithResult -> {
                        setFragmentResult(
                            Constants.CROP_REQUEST,
                            bundleOf(Constants.CROP_RESPONSE to event.bitmap)
                        )
                        findNavController().popBackStack()
                    }
                }
            }
        }

        return binding.root
    }
}