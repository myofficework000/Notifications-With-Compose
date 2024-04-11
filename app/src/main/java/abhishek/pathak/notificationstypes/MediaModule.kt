package abhishek.pathak.notificationstypes

import android.app.Application
import androidx.media3.exoplayer.ExoPlayer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

/**
 * Module responsible for providing ExoPlayer instances to ViewModel components.
 */
@Module
@InstallIn(ViewModelComponent::class)
class MediaModule {

    /**
     * Provides a singleton instance of ExoPlayer scoped to ViewModel.
     * @param application The application context used to build ExoPlayer instance.
     * @return A singleton instance of ExoPlayer.
     */
    @Provides
    @ViewModelScoped
    fun provideExoPlayer(application: Application): ExoPlayer =
        ExoPlayer.Builder(application).build()
}
