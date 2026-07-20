package com.wick.feature.discover

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

@Composable
fun rememberLocationRequester(
    onLocationReceived: (Double, Double) -> Unit,
    onDenied: () -> Unit,
): () -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            scope.launch {
                val location = fetchLastLocation(context)
                if (location != null) {
                    onLocationReceived(location.first, location.second)
                } else {
                    onDenied()
                }
            }
        } else {
            onDenied()
        }
    }

    return {
        permissionLauncher.launch(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
        )
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
@SuppressLint("MissingPermission")
private suspend fun fetchLastLocation(context: Context): Pair<Double, Double>? = suspendCancellableCoroutine { cont ->
    LocationServices.getFusedLocationProviderClient(context).lastLocation
        .addOnSuccessListener { location ->
            if (cont.isActive) {
                cont.resume(location?.let { it.latitude to it.longitude }) {}
            }
        }
        .addOnFailureListener {
            if (cont.isActive) cont.resume(null) {}
        }
}
