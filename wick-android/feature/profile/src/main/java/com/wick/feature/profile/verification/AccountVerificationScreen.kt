package com.wick.feature.profile.verification

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.wick.core.designsystem.theme.WickColors
import com.wick.core.designsystem.theme.WickShapes
import com.wick.core.designsystem.theme.WickTextStyles
import com.wick.core.model.UiError
import com.wick.feature.profile.R
import java.io.File
import java.util.concurrent.Executors

@Composable
fun AccountVerificationRoute(
    onBack: () -> Unit,
    viewModel: AccountVerificationViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    AccountVerificationScreen(
        onBack = onBack,
        state = state,
        onCaptured = viewModel::onCaptured,
        onRetake = viewModel::retake,
        onSubmit = viewModel::submit,
        onDismissRejected = viewModel::dismissRejected,
    )
}

@Composable
private fun AccountVerificationScreen(
    onBack: () -> Unit,
    state: VerificationUiState,
    onCaptured: (Uri) -> Unit,
    onRetake: () -> Unit,
    onSubmit: () -> Unit,
    onDismissRejected: () -> Unit = {},
) {
    val context = LocalContext.current
    var cameraGranted by remember { mutableStateOf(false) }
    var permissionDenied by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        cameraGranted = granted
        permissionDenied = !granted
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(android.Manifest.permission.CAMERA)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        when {
            state.isCheckingStatus -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = WickColors.Primary,
            )

            state.isSubmitted -> SubmittedContent(onBack = onBack)

            state.isRejected -> RejectedContent(onRetry = onDismissRejected, onBack = onBack)

            state.capturedUriString != null -> ReviewContent(
                uriString = state.capturedUriString,
                isUploading = state.isUploading,
                error = state.error,
                onRetake = onRetake,
                onSubmit = onSubmit,
            )

            cameraGranted -> CameraContent(
                onCaptured = { file ->
                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                    onCaptured(uri)
                },
            )

            permissionDenied -> PermissionDeniedContent(
                onRequest = { permissionLauncher.launch(android.Manifest.permission.CAMERA) },
            )
        }

        if (!state.isSubmitted) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(4.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = Color.White,
                )
            }
        }
    }
}

@Composable
private fun CameraContent(onCaptured: (File) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val imageCapture = remember { ImageCapture.Builder().build() }
    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }
    var capturing by remember { mutableStateOf(false) }
    var faceDetected by remember { mutableStateOf(false) }

    val faceDetector = remember {
        FaceDetection.getClient(
            FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setMinFaceSize(0.35f)
                .build(),
        )
    }

    val frameColor by animateColorAsState(
        targetValue = if (faceDetected) WickColors.Primary else Color.White,
        animationSpec = tween(300),
        label = "frameColor",
    )

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val future = ProcessCameraProvider.getInstance(ctx)
                future.addListener({
                    val provider = future.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    val analysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                    analysis.setAnalyzer(analysisExecutor) { imageProxy ->
                        @androidx.camera.core.ExperimentalGetImage
                        val mediaImage = imageProxy.image
                        if (mediaImage != null) {
                            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                            faceDetector.process(image)
                                .addOnSuccessListener { faces ->
                                    val w = imageProxy.width.toFloat()
                                    val h = imageProxy.height.toFloat()
                                    val minDim = minOf(w, h)
                                    faceDetected = faces.any { face ->
                                        val box = face.boundingBox
                                        val faceSize = (box.width() + box.height()) / 2f
                                        val faceCx = box.exactCenterX()
                                        val faceCy = box.exactCenterY()
                                        val centerThreshold = minDim * 0.25f
                                        val isCentered = kotlin.math.abs(faceCx - w / 2f) < centerThreshold &&
                                            kotlin.math.abs(faceCy - h / 2f) < centerThreshold
                                        val isBigEnough = faceSize >= minDim * 0.38f
                                        isCentered && isBigEnough
                                    }
                                }
                                .addOnCompleteListener { imageProxy.close() }
                        } else {
                            imageProxy.close()
                        }
                    }
                    provider.unbindAll()
                    provider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_FRONT_CAMERA,
                        preview,
                        imageCapture,
                        analysis,
                    )
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            },
            modifier = Modifier.fillMaxSize(),
        )

        FaceOverlay(frameColor = frameColor)

        Text(
            text = if (faceDetected) stringResource(R.string.account_verification_face_ready) else stringResource(R.string.account_verification_face_hint),
            style = WickTextStyles.BodySmall,
            color = if (faceDetected) WickColors.Primary else Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 152.dp),
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 48.dp)
                .size(72.dp)
                .clip(CircleShape)
                .background(if (capturing || !faceDetected) Color.Gray else Color.White)
                .clickable(enabled = !capturing && faceDetected) {
                    capturing = true
                    val dir = File(context.cacheDir, "verification").apply { mkdirs() }
                    val file = File.createTempFile("verify_", ".jpg", dir)
                    val output = ImageCapture.OutputFileOptions.Builder(file).build()
                    imageCapture.takePicture(
                        output,
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(results: ImageCapture.OutputFileResults) = onCaptured(file)
                            override fun onError(e: ImageCaptureException) { capturing = false }
                        },
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            if (capturing) {
                CircularProgressIndicator(modifier = Modifier.size(28.dp), color = WickColors.Primary, strokeWidth = 2.dp)
            } else {
                Icon(
                    imageVector = Icons.Filled.CameraAlt,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(32.dp),
                )
            }
        }
    }
}

@Composable
private fun FaceOverlay(frameColor: Color = Color.White) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen },
    ) {
        val radius = size.minDimension * 0.40f
        drawRect(Color.Black.copy(alpha = 0.55f))
        drawCircle(color = Color.Transparent, radius = radius, blendMode = BlendMode.Clear)
        drawCircle(color = frameColor, radius = radius, style = Stroke(width = 3.dp.toPx()))
    }
}

@Composable
private fun ReviewContent(
    uriString: String,
    isUploading: Boolean,
    error: UiError?,
    onRetake: () -> Unit,
    onSubmit: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        AsyncImage(
            model = uriString,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        FaceOverlay(frameColor = WickColors.Primary)

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            error?.let {
                Text(
                    text = stringResource(R.string.account_verification_error_unknown),
                    style = WickTextStyles.BodySmall,
                    color = Color(0xFFFF6B6B),
                    textAlign = TextAlign.Center,
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(WickShapes.Button)
                        .background(Color.White.copy(alpha = 0.15f))
                        .clickable(enabled = !isUploading, onClick = onRetake)
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(imageVector = Icons.Filled.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Text(text = stringResource(R.string.account_verification_retake), style = WickTextStyles.Label, color = Color.White)
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(WickShapes.Button)
                        .background(WickColors.Primary)
                        .clickable(enabled = !isUploading, onClick = onSubmit)
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = WickColors.OnPrimary, strokeWidth = 2.dp)
                    } else {
                        Text(text = stringResource(R.string.account_verification_submit), style = WickTextStyles.Label, color = WickColors.OnPrimary)
                    }
                }
            }
        }
    }
}

@Composable
private fun RejectedContent(onRetry: () -> Unit, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.CameraAlt,
            contentDescription = null,
            tint = Color(0xFFFF6B6B),
            modifier = Modifier.size(80.dp),
        )
        Spacer(modifier = Modifier.size(24.dp))
        Text(
            text = stringResource(R.string.account_verification_rejected_title),
            style = WickTextStyles.HeadlineLarge,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = stringResource(R.string.account_verification_rejected_body),
            style = WickTextStyles.Body,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.size(40.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(WickShapes.Button)
                .background(WickColors.Primary)
                .clickable(onClick = onRetry)
                .padding(horizontal = 32.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = stringResource(R.string.account_verification_rejected_retry), style = WickTextStyles.Label, color = WickColors.OnPrimary)
        }
        Spacer(modifier = Modifier.size(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(WickShapes.Button)
                .background(Color.White.copy(alpha = 0.15f))
                .clickable(onClick = onBack)
                .padding(horizontal = 32.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = stringResource(R.string.account_verification_back), style = WickTextStyles.Label, color = Color.White)
        }
    }
}

@Composable
private fun SubmittedContent(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = WickColors.Primary,
            modifier = Modifier.size(80.dp),
        )
        Spacer(modifier = Modifier.size(24.dp))
        Text(
            text = stringResource(R.string.account_verification_submitted_title),
            style = WickTextStyles.HeadlineLarge,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = stringResource(R.string.account_verification_submitted_body),
            style = WickTextStyles.Body,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.size(40.dp))
        Box(
            modifier = Modifier
                .clip(WickShapes.Button)
                .background(Color.White.copy(alpha = 0.15f))
                .clickable(onClick = onBack)
                .padding(horizontal = 32.dp, vertical = 14.dp),
        ) {
            Text(text = stringResource(R.string.account_verification_back), style = WickTextStyles.Label, color = Color.White)
        }
    }
}

@Composable
private fun PermissionDeniedContent(onRequest: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.CameraAlt,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.5f),
            modifier = Modifier.size(64.dp),
        )
        Spacer(modifier = Modifier.size(16.dp))
        Text(
            text = stringResource(R.string.account_verification_permission_title),
            style = WickTextStyles.HeadlineLarge,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = stringResource(R.string.account_verification_permission_body),
            style = WickTextStyles.Body,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.size(24.dp))
        Box(
            modifier = Modifier
                .clip(WickShapes.Button)
                .background(WickColors.Primary)
                .clickable(onClick = onRequest)
                .padding(horizontal = 32.dp, vertical = 14.dp),
        ) {
            Text(text = stringResource(R.string.account_verification_permission_cta), style = WickTextStyles.Label, color = WickColors.OnPrimary)
        }
    }
}
