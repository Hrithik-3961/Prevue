package com.hrithik.prevue

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.hrithik.prevue.databinding.FragmentHomeBinding
import kotlinx.coroutines.flow.collect
import java.io.File

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

        viewModel.permissionRequest.observe(viewLifecycleOwner) { permissionsList ->
            requestPermissions.launch(permissionsList.toTypedArray())
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.homeEvents.collect { event ->
                when (event) {
                    is HomeViewModel.HomeEvent.OpenCamera -> {
                        /*val root = Environment.getRootDirectory()
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                                .toString()
                        val myFile = File("$root/Prevue")
                        myFile.mkdirs()
                        val fname = "Selfie-${System.currentTimeMillis()}.jpg"
                        tempImgFile = File(myFile, fname)
                        tempImgUri = FileProvider.getUriForFile(
                            requireContext(),
                            BuildConfig.APPLICATION_ID + ".provider",
                            tempImgFile
                        )*/
                        /*val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        intent.putExtra("android.intent.extras.CAMERA_FACING", 1)
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, event.uri)*/
                        resultLauncher.launch(event.intent)
                        //tempImgUri = event.intent.extras?.get(MediaStore.EXTRA_OUTPUT) as Uri
                    }

                    is HomeViewModel.HomeEvent.NavigateToEditScreen -> {
                        val action = HomeFragmentDirections.actionHomeFragmentToEditFragment(event.image)
                        findNavController().navigate(action)
                    }
                }
            }
        }
        return binding.root
    }

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                viewModel.onImageCaptured()
                //binding.imageView.setImageURI(tempImgUri)
//
//                MediaScannerConnection.scanFile(
//                    requireContext(), arrayOf(tempImgFile.toString()), null
//                ) { _, _ -> }
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