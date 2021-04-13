package com.google.mlkit.vision.camerasample.ui.fragments


import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.RequestManager
import com.google.mlkit.vision.camerasample.R
import com.google.mlkit.vision.camerasample.base.BaseFragment
import com.google.mlkit.vision.camerasample.databinding.FragmentResultPhotoBinding
import com.google.mlkit.vision.camerasample.extension.displayToast
import com.google.mlkit.vision.camerasample.extension.scaleUpAnim
import com.google.mlkit.vision.camerasample.ui.CameraViewModel
import com.google.mlkit.vision.camerasample.util.Loading
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject


@AndroidEntryPoint
class ResultPhotoFragment : BaseFragment() {


    private val cameraViewModel:CameraViewModel by activityViewModels()

    private val viewModel:ResultPhotoViewModel by viewModels()

    @Inject
    lateinit var requestManager: RequestManager

    private var _binding: FragmentResultPhotoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentResultPhotoBinding.inflate(inflater, container, false)
        return binding.root
    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        init()
    }

    override fun displayProgressBar(inProgress: Boolean) {
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun init() {



        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        subscribeObserverBitmap()

        subscribeObserverResultSave()

        binding.btnSave.setOnClickListener {
            it.scaleUpAnim()
            saveImageBitmap()
        }



    }


    private fun subscribeObserverBitmap() {
        cameraViewModel.resultBitmap.observe(viewLifecycleOwner) {
            it?.getContentIfNotHandled()?.let {
                requestManager.load(it)
                    .into(binding.img)
            }
        }
    }

    private fun saveImageBitmap() {
        cameraViewModel.resultBitmap.value?.peekContent()?.let { bitmap ->
            viewModel.setResultBitmap(bitmap)
            Timber.d("resultBitmap")
        }

    }


    private fun subscribeObserverResultSave() {
        viewModel.saveImageResult.observe(viewLifecycleOwner) {
            it?.data?.getContentIfNotHandled()?.let {
                Timber.d("save image=$it")
                displayToast(getString(R.string.your_image_saved_successfully))
                binding.btnSave.setImageResource(R.drawable.ic_save_done)
            }
            onDataStateChange(it.loading, it.error, isDialog = false, isShowProgress = false)
            showLoadingSaveImage(it.loading)
        }
    }

    private fun showLoadingSaveImage(loading: Loading) {
        if (loading.isLoading) {
            binding.animSaveImage.visibility = View.VISIBLE
            binding.animSaveImage.playAnimation()
            binding.btnSave.visibility = View.INVISIBLE
        } else {
            binding.animSaveImage.visibility = View.GONE
            binding.animSaveImage.pauseAnimation()
            binding.btnSave.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}