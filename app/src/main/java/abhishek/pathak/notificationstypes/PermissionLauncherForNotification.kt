package abhishek.pathak.notificationstypes

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Composable function to request notification permissions.
 * This composable requests notification permissions using the `ActivityResultContracts.RequestPermission`
 * contract. It updates the state based on the permission result, allowing the caller to react accordingly.
 * @throws SecurityException if the permission is denied.
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun RequestNotificationPermissions() {
    // State to track whether notification permission is granted
    var hasNotificationPermission by remember { mutableStateOf(false) }

    // Request notification permission and update state based on the result
    val permissionResult = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { hasNotificationPermission = it }
    )

    // Request notification permission when the component is launched
    LaunchedEffect(key1 = true) {
        permissionResult.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}

