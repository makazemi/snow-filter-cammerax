package com.google.mlkit.vision.camerasample.di

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton


@InstallIn(ApplicationComponent::class)
@Module
object AppModule {


    @Singleton
    @Provides
    fun provideGlideInstance(@ApplicationContext application: Context): RequestManager {
        return Glide.with(application)
    }


    @Singleton
    @Provides
    fun provideRequestOptions() = RequestOptions().transform(CenterCrop(), RoundedCorners(50))
}