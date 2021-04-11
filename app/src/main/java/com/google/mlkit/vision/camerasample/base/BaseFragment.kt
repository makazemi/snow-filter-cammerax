package com.google.mlkit.vision.camerasample.base

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import androidx.fragment.app.Fragment
import com.google.mlkit.vision.camerasample.R
import com.google.mlkit.vision.camerasample.extension.displayLoadingDialog
import com.google.mlkit.vision.camerasample.extension.displayToast
import com.google.mlkit.vision.camerasample.util.ErrorBody
import com.google.mlkit.vision.camerasample.util.Event
import com.google.mlkit.vision.camerasample.util.Loading
import com.google.mlkit.vision.camerasample.util.TypeError
import timber.log.Timber



abstract class BaseFragment : Fragment() {


    private var loadingDialog: Dialog? = null



    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initDialog()
    }

    private fun handleError(msg: String?, typeError: TypeError, ignoreError: Boolean) {
        msg?.let {
            if (!ignoreError) {
                when (typeError) {
                    TypeError.TOAST -> displayToast(it)
                    else -> displayToast(it)
                }
            }
        }
    }

    fun onDataStateChange(
        loading: Loading,
        error: Event<ErrorBody>?,
        isDialog: Boolean,
        isShowProgress: Boolean = true,
        typeError: TypeError = TypeError.TOAST,
        ignoreError: Boolean = false
    ) {
        if (isShowProgress) {
            when {
                isDialog -> displayDialogProgressBar(loading.isLoading)
                else -> displayProgressBar(loading.isLoading)
            }
        }

        error?.let {
            it.getContentIfNotHandled()?.let {
              Timber.d("error in base fragment=${it.message}")
               // if(it.message!=ERROR_NETWORK_CONNECTION_IN_CACHE)
                handleError(it.message, typeError, ignoreError)
            }
        }

    }

    private fun displayDialogProgressBar(inProgress: Boolean) {
        if(loadingDialog==null){
            initDialog()
        }
        displayLoadingDialog(inProgress, loadingDialog)
    }

    abstract fun displayProgressBar(inProgress: Boolean)

    private fun initDialog() {
        loadingDialog = Dialog(this.requireContext())
        loadingDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        loadingDialog?.setContentView(R.layout.dialog_loading)
        loadingDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        loadingDialog?.setCancelable(false)
        loadingDialog?.setCanceledOnTouchOutside(false)
    }

    override fun onPause() {
        super.onPause()
        if(loadingDialog!=null){
            (loadingDialog as Dialog).dismiss()
            loadingDialog=null
        }

    }

}