package abhishek.pathak.notificationstypes

import abhishek.pathak.notificationstypes.ui.theme.NotificationsTypesTheme
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.app.NotificationCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

/**
 * Activity responsible for handling notifications and displaying notification-related UI.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationChannel: NotificationChannel
    private lateinit var notificationBuilder: Notification.Builder
    private val channelId = "ChannelId"

    /**
     * Called when the activity is starting.
     */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NotificationsTypesTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RequestNotificationPermissions()
                    NotificationUI(OnMediaClick())
                }
            }
        }
    }

    /**
     * Composable function to display the notification-related UI elements.
     */
    @Composable
    fun NotificationUI(onMediaClick: (Unit)) {
        Column {
            Button(onClick = { invokeSimpleStyle() }) {
                Text(text = "Simple Notification")
            }
            Button(onClick = { invokeMessageStyle() }) {
                Text(text = "Message Style Notification")
            }
            Button(onClick = { invokeBigPictureStyle() }) {
                Text(text = "Big Picture Style Notification")
            }
            Button(onClick = { onMediaClick }) {
                Text(text = "Media Style Notification")
            }
        }
    }

    /**
     * Function to handle media click.
     */
    @Composable
    private fun OnMediaClick() {
        val viewModel: MediaViewModel = hiltViewModel()

        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        val currentTrackState by viewModel.currentPlayingIndex.collectAsStateWithLifecycle()
        val isPlayingState by viewModel.isPlaying.collectAsStateWithLifecycle()
        val totalDurationState by viewModel.totalDurationInMS.collectAsStateWithLifecycle()
        var currentPositionState by remember { mutableLongStateOf(0L) }

        LaunchedEffect(isPlayingState) {
            while (isPlayingState) {
                currentPositionState = viewModel.player.currentPosition
                delay(1.seconds)
            }
        }

        when (uiState) {
            PlayerUIState.Loading -> {

            }

            is PlayerUIState.Tracks -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    AudioPlayerView(viewModel)
                    PlayerControlsView(
                        currentTrackImage = (uiState as PlayerUIState.Tracks).items[currentTrackState].teaserUrl,
                        totalDuration = totalDurationState,
                        currentPosition = currentPositionState,
                        isPlaying = isPlayingState,
                        navigateTrack = { action -> viewModel.updatePlaylist(action) },
                        seekPosition = { position -> viewModel.updatePlayerPosition((position * 1000).toLong()) }
                    )
                    PlaylistView((uiState as PlayerUIState.Tracks).items, currentTrackState)
                }
            }
        }
    }

    /**
     * Function to invoke big picture style notification.
     */
    private fun invokeBigPictureStyle() {
        getNotificationChannel()

        val style = NotificationCompat.BigPictureStyle()
        style.bigPicture(BitmapFactory.decodeResource(resources, R.drawable.img))

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Big Picture notification title")
            .setContentText("Big Picture content")
            .setSmallIcon(R.drawable.ic_android_black_24dp)
            .setStyle(style)
            .setContentIntent(setPendingIntent(this))

        notificationManager.notify(1, notificationBuilder.build())
    }

    /**
     * Function to invoke message style notification.
     */
    private fun invokeMessageStyle() {
        getNotificationChannel()

        val style = Notification.InboxStyle()
        style.apply {
            addLine("Message 1 hey")
            addLine("Message 1 where are u?")
            addLine("I need your some time")
            addLine("to fix a bug")
            addLine("I need some code clarity")
            addLine("give me a call when you are there?")
            setSummaryText("+20 more item...")
        }

        notificationBuilder = Notification.Builder(this, channelId)
            .setContentTitle("Simple notification title")
            .setContentText("Simple notification content")
            .setSmallIcon(R.drawable.ic_android_black_24dp)
            .setStyle(style)
            .setContentIntent(setPendingIntent(this))

        notificationManager.notify(1, notificationBuilder.build())
    }

    /**
     * Function to invoke simple style notification.
     */
    fun invokeSimpleStyle() {
        getNotificationChannel()

        notificationBuilder = Notification.Builder(this, channelId)
            .setContentTitle("Inbox notification title")
            .setContentText("Message Style content")
            .setSmallIcon(R.drawable.ic_android_black_24dp)
            .setContentIntent(setPendingIntent(this))

        notificationManager.notify(1, notificationBuilder.build())
    }

    /**
     * Function to set a pending intent for notification.
     */
    private fun setPendingIntent(context: Context): PendingIntent? {
        val intent = Intent(context, MainActivity::class.java)
        return PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * Function to get notification channel.
     */
    private fun getNotificationChannel() {
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        notificationChannel = NotificationChannel(
            channelId,
            getString(R.string.app_name),
            NotificationManager.IMPORTANCE_HIGH
        ).also {
            it.enableLights(true)
            it.enableVibration(true)
        }

        notificationManager.createNotificationChannel(notificationChannel)
    }
}

