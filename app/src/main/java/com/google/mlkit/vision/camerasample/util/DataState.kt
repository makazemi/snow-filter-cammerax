package com.google.mlkit.vision.camerasample.util




data class DataState<T>(
    var error: Event<ErrorBody>? = null,
    var loading: Loading = Loading(false),
    var data: Event<T>? = null
)  {

    companion object {

        fun <T> error(
            message: ErrorBody
        ): DataState<T> {
            return DataState(
                error = Event(message),
                loading = Loading(false),
                data = null
            )
        }

        fun <T> loading(
            isLoading: Boolean,
            cachedData: T? = null
        ): DataState<T> {
            return DataState(
                error = null,
                loading = Loading(isLoading),
                data = Event.dataEvent(cachedData)
            )
        }

        fun <T> data(
            data: T? = null
        ): DataState<T> {
            return DataState(
                error = null,
                loading = Loading(false),
                data = Event.dataEvent(data)
            )
        }
    }
}
enum class TypeError{
    DIALOG,TOAST,SNACKBAR
}