package com.github.seijind.fliptowin.di

import com.github.seijind.fliptowin.data.repository.MockFlipToWinRepository
import com.github.seijind.fliptowin.domain.repository.FlipToWinRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FlipToWinModule {

    @Provides
    @Singleton
    fun provideFlipToWinRepository(): FlipToWinRepository {
        return MockFlipToWinRepository()
    }
}
