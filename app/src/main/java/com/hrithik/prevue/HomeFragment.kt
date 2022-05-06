package com.hrithik.prevue

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
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
        _binding?.apply {

            uploadGalleryBtn.setOnClickListener {
                viewModel.onUploadFromGalleryClicked()
            }

            selfieBtn.setOnClickListener {
                viewModel.onTakeSelfieClicked()
            }

        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.homeEvents.collect { event ->
                when(event) {
                    is HomeViewModel.HomeEvent.NavigateToEditScreen -> {
                        val action = HomeFragmentDirections.actionHomeFragmentToEditFragment()
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