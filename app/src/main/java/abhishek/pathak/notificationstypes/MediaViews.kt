package abhishek.pathak.notificationstypes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun AudioPlayerView(viewModel: MediaViewModel) {
    // Fetching the Local Context
    val mContext = LocalContext.current

    // Declaring ExoPlayer
    val mExoPlayer = remember(viewModel.player) {
        ExoPlayer.Builder(mContext).build().apply {
            viewModel.preparePlayer(context = mContext)
        }
    }

    // Implementing ExoPlayer
    DisposableEffect(
        AndroidView(modifier = Modifier.size(0.dp), factory = { context ->
            PlayerView(context).apply {
                this.player = mExoPlayer
                hideController()
                useController = false
                controllerHideOnTouch = false
            }
        })
    ) {
        onDispose { viewModel.onDestroy() }
    }
}

@Composable
fun PlayerControlsView(
    currentTrackImage: String,
    totalDuration: Long,
    currentPosition: Long,
    isPlaying: Boolean,
    navigateTrack: (ControlButtons) -> Unit,
    seekPosition: (Float) -> Unit
) {

    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Spacer(modifier = Modifier.height(40.dp))
        AsyncImage(
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape),
            model = currentTrackImage,
            contentDescription = "player_image"
        )

        Slider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, start = 30.dp, end = 30.dp),
            value = (currentPosition / 1000).toFloat(),
            valueRange = 0f..(totalDuration / 1000).toFloat(),
            onValueChange = { seekPosition(it) },
            colors =
            SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.tertiary,
                activeTickColor = MaterialTheme.colorScheme.onBackground,
                activeTrackColor = MaterialTheme.colorScheme.tertiary
            )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 30.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = currentPosition.toString())
            Text(text = totalDuration.toString())
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 30.dp, end = 30.dp, top = 5.dp, bottom = 30.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(modifier = Modifier
                .size(45.dp),
                onClick = { navigateTrack(ControlButtons.Rewind) }) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.baseline_autorenew_24),
                    contentDescription = "player_rewind",
                    tint = Color.Red
                )
            }
            Spacer(modifier = Modifier.width(30.dp))
            IconButton(modifier = Modifier
                .size(70.dp),
                onClick = { navigateTrack(ControlButtons.Play) }) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = if (isPlaying) R.drawable.baseline_pause_circle_24 else R.drawable.baseline_play_circle_24),
                    contentDescription = "player_play",
                    tint = Color.Red,
                    modifier = Modifier
                        .size(70.dp)
                )
            }
            Spacer(modifier = Modifier.width(30.dp))
            IconButton(modifier = Modifier
                .size(45.dp),
                onClick = { navigateTrack(ControlButtons.Next) }) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.baseline_skip_next_24),
                    contentDescription = "player_next",
                    tint = Color.Red
                )
            }
        }
    }
}

@Composable
fun PlaylistView(tracks: List<TrackItem>, currentTrack: Int) {
    LazyColumn {
        items(tracks.size) {
            PlaylistItemView(tracks[it], currentTrack == it)
        }
    }
}

@Composable
fun PlaylistItemView(trackItem: TrackItem, isPlaying: Boolean) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(if (isPlaying) Color.Blue else Color.Transparent),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {


        TrackImageView(imageUrl = trackItem.teaserUrl)

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceAround
        ) {
            Text(text = trackItem.title)
            Text(text = trackItem.artistName)
        }

        Text(text = trackItem.duration)
    }
}


@Composable
fun TrackImageView(size: Dp = 75.dp, imageUrl: String) {
    AsyncImage(
        modifier = Modifier
            .size(size)
            .padding(horizontal = 10.dp),
        model = imageUrl,
        contentDescription = null
    )
}