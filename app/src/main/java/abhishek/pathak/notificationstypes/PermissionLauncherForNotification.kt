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

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun askPermissions() {

    var hasNotificationPermission by remember { mutableStateOf(false) }

    // Request camera permission and update state based on the result
    val permissionResult = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { hasNotificationPermission = it }
    )

    // Request camera permission when the component is launched
    LaunchedEffect(key1 = true) {
        permissionResult.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
