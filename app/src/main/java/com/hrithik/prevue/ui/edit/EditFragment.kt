package com.hrithik.prevue.ui.edit

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.hrithik.prevue.R
import com.hrithik.prevue.databinding.FragmentEditBinding
import com.hrithik.prevue.util.Constants
import com.hrithik.prevue.util.Response
import com.hrithik.prevue.util.Status
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

        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    viewModel.onCancelClicked()
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(callback)

        val image = navArgs<EditFragmentArgs>().value.image
        viewModel.image.value = Response.success(image)

        setFragmentResultListener(Constants.CROP_REQUEST) { _, bundle ->
            val result = bundle.get(Constants.CROP_RESPONSE) as Bitmap?
            val img = viewModel.image.value
            if (img != null && result != null) {
                img.data?.bitmap = result
                viewModel.image.value = img
            }
        }

        viewModel.image.observe(viewLifecycleOwner) { response ->
            when (response.status) {
                Status.SUCCESS -> {
                    binding.imageView.setImageBitmap(response.data?.bitmap)
                }
                Status.ERROR -> {
                    Snackbar.make(requireView(), response.message, Snackbar.LENGTH_SHORT).show()
                }
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
                        if (event.image != null) {
                            Snackbar.make(
                                requireView(),
                                "Image saved successfully",
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                        setFragmentResult(
                            Constants.EDIT_REQUEST,
                            bundleOf(Constants.EDIT_RESPONSE to event.image)
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


