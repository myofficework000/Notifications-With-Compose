package abhishek.pathak.notificationstypes

/**
 * Data class representing a track item in a media playlist.
 * Contains properties for unique identifier, audio URL, teaser URL, title, artist name, and duration.
 * @property id Unique identifier for the track item.
 * @property audioUrl URL for the full audio of the track.
 * @property teaserUrl URL for a teaser or preview of the track.
 * @property title Title of the track.
 * @property artistName Name of the artist or performer.
 * @property duration Duration of the track.
 */
data class TrackItem (
    var id: String,
    var audioUrl: String,
    var teaserUrl: String,
    var title: String,
    var artistName: String,
    var duration: String,
)
