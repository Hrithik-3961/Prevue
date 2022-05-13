package com.hrithik.prevue

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
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
        binding.activity = activity

        val image = navArgs<EditFragmentArgs>().value.image
        viewModel.image.value = Response.success(image)

        viewModel.image.observe(viewLifecycleOwner) { response ->
            when(response.status) {
                Status.SUCCESS -> {
                    binding.imageView.setImageBitmap(response.data?.bitmap)
                }
                Status.ERROR -> {
                    Snackbar.make(requireView(), response.message, Snackbar.LENGTH_SHORT).show()
                }
            }
        }

        setFragmentResultListener("crop_request") { _, bundle ->
            val result = bundle.get("crop_result") as Bitmap?
            val img = viewModel.image.value
            if (img != null) {
                img.data?.bitmap = result
                viewModel.image.value = img
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.editEvents.collect { event ->
                when (event) {
                    is EditViewModel.EditEvent.NavigateToCropScreen -> {
                        val action =
                            EditFragmentDirections.actionEditFragmentToCropFragment(event.bitmap)
                        findNavController().navigate(action)
                    }
                    is EditViewModel.EditEvent.NavigateBackWithResult -> {
                        setFragmentResult(
                            "edit_request",
                            bundleOf("edit_response" to event.image)
                        )
                        findNavController().popBackStack()
                    }
                    is EditViewModel.EditEvent.RotateImage -> {
                        binding.imageView.startAnimation(event.rotateAnimation)
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


