package abhishek.pathak.notificationstypes

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.session.MediaSession
import androidx.media3.ui.PlayerNotificationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@HiltViewModel
class MediaViewModel @Inject constructor(
    val player: ExoPlayer
) : ViewModel() {
    private val playlist = arrayListOf(
        TrackItem(
            "1",
            "https://www.matb3aa.com/music/Wegz/Dorak.Gai-Wegz-MaTb3aa.Com.mp3",
            "https://angartwork.anghcdn.co/?id=105597079&size=640",
            "Track 1",
            "Wegz",
            "4:18"
        ),
        TrackItem(
            "2",
            "https://mp3songs.nghmat.com/mp3_songs_Js54w1/CairoKee/Nghmat.Com_Cairokee_Marboot.B.Astek.mp3",
            "https://i.scdn.co/image/ab6761610000e5eb031d0209d9cb8abbc0505769",
            "Marboot B Astek",
            "Cairokee",
            "3:48"
        ),
        TrackItem(
            "3",
            "https://www.matb3aa.com/music/Marwan-Pablo/Album-CTRL-2021/GHABA-MARWAN.PABLO-MaTb3aa.Com.mp3",
            "https://lastfm.freetls.fastly.net/i/u/770x0/5ac22055e70c20939ae60b4825c8b04b.jpg",
            "GHABA",
            "Marwan Pablo",
            "3:02"
        ),
        TrackItem(
            "4",
            "https://www.matb3aa.com/music/Wegz/ATm-Wegz-MaTb3aa.Com.mp3",
            "https://www.qalimat.com/wp-content/uploads/2020/07/%D9%88%D9%8A%D8%AC%D8%B2.jpg",
            "ATM",
            "Wegz",
            "3:02"
        ),
        TrackItem(
            "5",
            "https://mp3songs.nghmat.com/mp3_songs_Js54w1/Sharmoofers/Nghmat.Com_Sharmoofers_Moftked.El.Habeba.mp3",
            "https://aghanyna.net/en/wp-content/uploads/2017/04/sharmoofers-2017.jpeg",
            "Moftked El Habeba",
            "Sharmoofers",
            "3:21"
        ),
        TrackItem(
            "6",
            "https://www.matb3aa.com/music/Shahyn/Ma.3aleena-Shahyn-MaTb3aa.Com.mp3",
            "https://i.scdn.co/image/ab6761610000e5eb368ee15b276f33ab10530737",
            "Ma Aleena",
            "Shahyn",
            "3:27"
        ),
    )

    private val _currentPlayingIndex = MutableStateFlow(0)
    val currentPlayingIndex = _currentPlayingIndex.asStateFlow()

    private val _totalDurationInMS = MutableStateFlow(0L)
    val totalDurationInMS = _totalDurationInMS.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    val uiState: StateFlow<PlayerUIState> =
        MutableStateFlow(PlayerUIState.Tracks(playlist)).stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            initialValue = PlayerUIState.Loading
        )

    private lateinit var notificationManager: MediaNotificationManager

    protected lateinit var mediaSession: MediaSession
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)


    private var isStarted = false

    fun preparePlayer(context: Context) {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

        player.setAudioAttributes(audioAttributes, true)
        player.repeatMode = Player.REPEAT_MODE_ALL

        player.addListener(playerListener)

        setupPlaylist(context)
    }

    private fun setupPlaylist(context: Context) {

        val videoItems: ArrayList<MediaSource> = arrayListOf()
        playlist.forEach {

            val mediaMetaData = MediaMetadata.Builder()
                .setArtworkUri(Uri.parse(it.teaserUrl))
                .setTitle(it.title)
                .setAlbumArtist(it.artistName)
                .build()

            val trackUri = Uri.parse(it.audioUrl)
            val mediaItem = MediaItem.Builder()
                .setUri(trackUri)
                .setMediaId(it.id)
                .setMediaMetadata(mediaMetaData)
                .build()
            val dataSourceFactory = DefaultDataSource.Factory(context)

            val mediaSource =
                ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)

            videoItems.add(
                mediaSource
            )
        }

        onStart(context)

        player.playWhenReady = false
        player.setMediaSources(videoItems)
        player.prepare()
    }

    fun updatePlaylist(action: ControlButtons) {
        when (action) {
            ControlButtons.Play -> if (player.isPlaying) player.pause() else player.play()
            ControlButtons.Next -> player.seekToNextMediaItem()
            ControlButtons.Rewind -> player.seekToPreviousMediaItem()
        }
    }

    fun updatePlayerPosition(position: Long) {
        player.seekTo(position)
    }

    fun onStart(context: Context) {
        if (isStarted) return

        isStarted = true

        // Build a PendingIntent that can be used to launch the UI.
        val sessionActivityPendingIntent =
            context.packageManager?.getLaunchIntentForPackage(context.packageName)
                ?.let { sessionIntent ->
                    PendingIntent.getActivity(
                        context,
                        SESSION_INTENT_REQUEST_CODE,
                        sessionIntent,
                        PendingIntent.FLAG_IMMUTABLE
                    )
                }

        // Create a new MediaSession.
        mediaSession = MediaSession.Builder(context, player)
            .setSessionActivity(sessionActivityPendingIntent!!).build()

        /**
         * The notification manager will use our player and media session to decide when to post
         * notifications. When notifications are posted or removed our listener will be called, this
         * allows us to promote the service to foreground (required so that we're not killed if
         * the main UI is not visible).
         */
        notificationManager =
            MediaNotificationManager(
                context,
                mediaSession.token,
                player,
                PlayerNotificationListener()
            )


        notificationManager.showNotificationForPlayer(player)
    }

    /**
     * Destroy audio notification
     */
    fun onDestroy() {
        onClose()
        player.release()
    }

    /**
     * Close audio notification
     */
    fun onClose() {
        if (!isStarted) return

        isStarted = false
        mediaSession.run {
            release()
        }

        // Hide notification
        notificationManager.hideNotification()

        // Free ExoPlayer resources.
        player.removeListener(playerListener)
    }

    /**
     * Listen for notification events.
     */
    private inner class PlayerNotificationListener :
        PlayerNotificationManager.NotificationListener {
        override fun onNotificationPosted(
            notificationId: Int,
            notification: Notification,
            ongoing: Boolean
        ) {

        }

        override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {

        }
    }

    /**
     * Listen to events from ExoPlayer.
     */
    private val playerListener = object : Player.Listener {

        override fun onPlaybackStateChanged(playbackState: Int) {
            Log.d(TAG, "onPlaybackStateChanged: ${playbackState}")
            super.onPlaybackStateChanged(playbackState)
            syncPlayerFlows()
            when (playbackState) {
                Player.STATE_BUFFERING,
                Player.STATE_READY -> {
                    notificationManager.showNotificationForPlayer(player)
                }

                else -> {
                    notificationManager.hideNotification()
                }
            }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            Log.d(TAG, "onMediaItemTransition: ${mediaItem?.mediaMetadata?.title}")
            super.onMediaItemTransition(mediaItem, reason)
            syncPlayerFlows()
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            Log.d(TAG, "onIsPlayingChanged: ${isPlaying}")
            super.onIsPlayingChanged(isPlaying)
            _isPlaying.value = isPlaying
        }

        override fun onPlayerError(error: PlaybackException) {
            super.onPlayerError(error)
            Log.e(TAG, "Error: ${error.message}")
        }
    }

    private fun syncPlayerFlows() {
        _currentPlayingIndex.value = player.currentMediaItemIndex
        _totalDurationInMS.value = player.duration.coerceAtLeast(0L)
    }

    companion object {
        const val SESSION_INTENT_REQUEST_CODE = 0
    }
}

private const val TAG = "Media3AppTag"

/**
 * Sealed interface representing the different states of the player UI.
 */
sealed interface PlayerUIState {
    /**
     * Represents the state when the player UI displays a list of tracks.
     *
     * @property items The list of track items to be displayed.
     */
    data class Tracks(val items: List<TrackItem>) : PlayerUIState

    /**
     * Represents the state when the player UI is in a loading state.
     */
    object Loading : PlayerUIState
}
