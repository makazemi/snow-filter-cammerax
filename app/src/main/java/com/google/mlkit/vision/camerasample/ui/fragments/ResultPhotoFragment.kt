package com.google.mlkit.vision.camerasample.ui.fragments


import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.RequestManager
import com.google.mlkit.vision.camerasample.R
import com.google.mlkit.vision.camerasample.base.BaseFragment
import com.google.mlkit.vision.camerasample.databinding.FragmentResultPhotoBinding
import com.google.mlkit.vision.camerasample.extension.displayToast
import com.google.mlkit.vision.camerasample.ui.CameraViewModel
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


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        init()
    }

    override fun displayProgressBar(inProgress: Boolean) {
    }


    private fun init(){
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        subscribeObserverBitmap()

        subscribeObserverResultSave()

        var animSave: AnimationDrawable
        binding.btnSave.apply {
            setBackgroundResource(R.drawable.to_save_done_anim)
            animSave = background as AnimationDrawable
            setOnClickListener {
                animSave.start()
                saveImageBitmap()
            }
        }

//        val scaleUp = ObjectAnimator.ofPropertyValuesHolder(binding.btnSave,
//            PropertyValuesHolder.ofFloat("scaleX", 1f),
//            PropertyValuesHolder.ofFloat("scaleY", 1f))
//        scaleUp.duration = 1000
//        scaleUp.repeatMode=ValueAnimator.RESTART
//        scaleUp.start()
//        val animatorSet1 = AnimatorSet()
//        val animatorSet2 = AnimatorSet()
//        val scaleXUp=ObjectAnimator.ofFloat(binding.btnSave, "scaleX", 50f).apply {
//            duration = 1000
//        }
//        val scaleYUp=ObjectAnimator.ofFloat(binding.btnSave, "scaleY", 50f).apply {
//            duration = 1000
//        }
//        val scaleXDown=ObjectAnimator.ofFloat(binding.btnSave, "scaleX", 20f).apply {
//            duration = 1000
//        }
//        val scaleYDown=ObjectAnimator.ofFloat(binding.btnSave, "scaleY", 20f).apply {
//            duration = 1000
//        }
//
//        animatorSet1.play(scaleXUp).with(scaleYUp)
//        animatorSet1.startDelay=1000
//
//        animatorSet2.play(scaleXDown).with(scaleYDown).after(animatorSet1)
//        animatorSet1.start()
//        animatorSet2.start()
    }

    private fun subscribeObserverBitmap(){
        cameraViewModel.resultBitmap.observe(viewLifecycleOwner){
            it?.getContentIfNotHandled()?.let {
                requestManager.load(it)
                    .into(binding.img)
            }
        }
    }

    private fun saveImageBitmap(){
        cameraViewModel.resultBitmap.value?.peekContent()?.let { bitmap ->
            viewModel.setResultBitmap(bitmap)
        }

    }

    private fun subscribeObserverResultSave(){
        viewModel.saveImageResult.observe(viewLifecycleOwner){
            it?.data?.getContentIfNotHandled()?.let {
                Timber.d("save image=$it")
                displayToast(getString(R.string.your_image_saved_successfully))
            }
            onDataStateChange(it.loading, it.error, isDialog = true)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}