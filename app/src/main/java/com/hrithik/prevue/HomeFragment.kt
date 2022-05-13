package com.hrithik.prevue

import android.app.Activity.RESULT_OK
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.hrithik.prevue.databinding.FragmentHomeBinding
import kotlinx.coroutines.flow.collect

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(layoutInflater, container, false)
        binding.viewModel = viewModel
        binding.activity = activity

        setFragmentResultListener("edit_request") { _, bundle ->
            val result = bundle.get("edit_response") as Image
            viewModel.image.value = Response.success(result)
        }

        viewModel.image.observe(viewLifecycleOwner) { response ->
            when (response.status) {
                Status.SUCCESS -> {
                    val image = response.data
                    binding.imageView.setImageBitmap(image?.bitmap)
                }
                Status.ERROR -> {
                    Snackbar.make(requireView(), response.message, Snackbar.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.permissionRequest.observe(viewLifecycleOwner) { permissionsList ->
            requestPermissions.launch(permissionsList.toTypedArray())
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.homeEvents.collect { event ->
                when (event) {
                    is HomeViewModel.HomeEvent.OpenGallery -> {
                        uploadImageResultLauncher.launch(event.intent)
                    }
                    is HomeViewModel.HomeEvent.OpenCamera -> {
                        captureImageResultLauncher.launch(event.intent)
                    }
                    is HomeViewModel.HomeEvent.NavigateToEditScreen -> {
                        val action =
                            HomeFragmentDirections.actionHomeFragmentToEditFragment(event.image)
                        findNavController().navigate(action)
                    }
                }
            }
        }
        return binding.root
    }

    private val uploadImageResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val uri = result?.data?.data
                if (uri != null)
                    viewModel.onImagePicked(uri, requireActivity())
            } else {
                viewModel.image.value = Response.error("Error in picking image from gallery")
            }
        }

    private val captureImageResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                viewModel.onImageCaptured()
            } else {
                viewModel.image.value = Response.error("Error in taking selfie")
            }
        }

    private val requestPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            var flag = true
            permissions.forEach { permission ->
                if (!permission.value)
                    flag = false
            }
            viewModel.onPermissionResult(requireActivity(), flag)
        }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}