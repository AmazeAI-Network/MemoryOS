package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.ui.theme.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executor
import java.util.concurrent.Executors

private const val TAG = "CameraCaptureDialog"

/**
 * CameraX Preview & Photo Capture Composable Dialog:
 * Allows users to take high-resolution photos directly inside MemoryOS to include in their
 * memory intelligence database.
 */
@Composable
fun CameraCaptureDialog(
  onDismiss: () -> Unit,
  onImageCaptured: (File) -> Unit
) {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current

  var hasCameraPermission by remember {
    mutableStateOf(
      ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    )
  }

  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    hasCameraPermission = isGranted
    if (!isGranted) {
      Toast.makeText(context, "Camera permission is required to capture memories", Toast.LENGTH_SHORT).show()
    }
  }

  // Lifecycle observer: Auto-detect when returning from Settings after granting permission
  DisposableEffect(lifecycleOwner) {
    val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
      if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
        hasCameraPermission = ContextCompat.checkSelfPermission(
          context,
          Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose {
      lifecycleOwner.lifecycle.removeObserver(observer)
    }
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(
      usePlatformDefaultWidth = false,
      dismissOnBackPress = true,
      dismissOnClickOutside = false
    )
  ) {
    Surface(
      shape = RoundedCornerShape(28.dp),
      color = PureWhite,
      border = BorderStroke(1.dp, CardBorder),
      shadowElevation = 12.dp,
      modifier = Modifier
        .fillMaxWidth(0.94f)
        .padding(vertical = 16.dp)
        .semantics { contentDescription = "Camera photo capture dialog" }
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Header Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(DustyBlueLight)
                .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
              Text(
                text = "MEMORY CAMERA",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = DustyBlue
              )
            }
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(36.dp)
          ) {
            Icon(
              imageVector = IconlyIcons.Close,
              contentDescription = "Close Camera",
              tint = TextMuted,
              modifier = Modifier.size(20.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (hasCameraPermission) {
          CameraPreviewContent(
            onImageCaptured = { file ->
              onImageCaptured(file)
              onDismiss()
            },
            onCancel = onDismiss
          )
        } else {
          CameraPermissionPrompt(
            onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) },
            onCancel = onDismiss
          )
        }
      }
    }
  }
}

/**
 * CameraX Live Preview Viewport with Shutter & Lens Selector Controls
 */
@Composable
private fun CameraPreviewContent(
  onImageCaptured: (File) -> Unit,
  onCancel: () -> Unit
) {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current

  var lensFacing by remember { mutableStateOf(CameraSelector.DEFAULT_BACK_CAMERA) }
  var isCapturing by remember { mutableStateOf(false) }

  val imageCapture = remember {
    ImageCapture.Builder()
      .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
      .build()
  }

  val cameraExecutor: Executor = remember { Executors.newSingleThreadExecutor() }

  Column(
    modifier = Modifier.fillMaxWidth(),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // Camera Viewport Container
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(380.dp)
        .clip(RoundedCornerShape(22.dp))
        .background(BrandBlack),
      contentAlignment = Alignment.Center
    ) {
      AndroidView(
        factory = { ctx ->
          PreviewView(ctx).apply {
            layoutParams = ViewGroup.LayoutParams(
              ViewGroup.LayoutParams.MATCH_PARENT,
              ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = PreviewView.ScaleType.FILL_CENTER
          }
        },
        update = { previewView ->
          val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
          cameraProviderFuture.addListener({
            try {
              val cameraProvider = cameraProviderFuture.get()
              val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
              }

              cameraProvider.unbindAll()
              cameraProvider.bindToLifecycle(
                lifecycleOwner,
                lensFacing,
                preview,
                imageCapture
              )
            } catch (e: Exception) {
              Log.e(TAG, "Camera binding failed", e)
            }
          }, ContextCompat.getMainExecutor(context))
        },
        modifier = Modifier.fillMaxSize()
      )

      if (isCapturing) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(PureWhite.copy(alpha = 0.6f)),
          contentAlignment = Alignment.Center
        ) {
          CircularProgressIndicator(color = MidnightNavy)
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Shutter & Camera Controls Row
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp),
      horizontalArrangement = Arrangement.SpaceEvenly,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Toggle Camera Lens (Front/Back)
      IconButton(
        onClick = {
          lensFacing = if (lensFacing == CameraSelector.DEFAULT_BACK_CAMERA) {
            CameraSelector.DEFAULT_FRONT_CAMERA
          } else {
            CameraSelector.DEFAULT_BACK_CAMERA
          }
        },
        modifier = Modifier
          .size(48.dp)
          .clip(CircleShape)
          .background(WarmCreamDark)
      ) {
        Icon(
          imageVector = IconlyIcons.Refresh,
          contentDescription = "Switch Camera Lens",
          tint = MidnightNavy,
          modifier = Modifier.size(22.dp)
        )
      }

      // Shutter Button
      Box(
        modifier = Modifier
          .size(72.dp)
          .clip(CircleShape)
          .background(MidnightNavy)
          .border(4.dp, PureWhite, CircleShape)
          .clickable(enabled = !isCapturing) {
            isCapturing = true
            val photoFile = createTempImageFile(context)
            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

            imageCapture.takePicture(
              outputOptions,
              cameraExecutor,
              object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                  ContextCompat.getMainExecutor(context).execute {
                    isCapturing = false
                    Toast.makeText(context, "Photo captured!", Toast.LENGTH_SHORT).show()
                    onImageCaptured(photoFile)
                  }
                }

                override fun onError(exception: ImageCaptureException) {
                  ContextCompat.getMainExecutor(context).execute {
                    isCapturing = false
                    Log.e(TAG, "Photo capture failed: ${exception.message}", exception)
                    Toast.makeText(context, "Failed to capture photo", Toast.LENGTH_SHORT).show()
                  }
                }
              }
            )
          },
        contentAlignment = Alignment.Center
      ) {
        Box(
          modifier = Modifier
            .size(54.dp)
            .clip(CircleShape)
            .background(PureWhite)
        )
      }

      // Cancel / Close Button
      IconButton(
        onClick = onCancel,
        modifier = Modifier
          .size(48.dp)
          .clip(CircleShape)
          .background(WarmCreamDark)
      ) {
        Icon(
          imageVector = IconlyIcons.Close,
          contentDescription = "Cancel",
          tint = TextSecondary,
          modifier = Modifier.size(20.dp)
        )
      }
    }
  }
}

/**
 * Camera Permission Request Soft Prompt UI
 */
@Composable
private fun CameraPermissionPrompt(
  onRequestPermission: () -> Unit,
  onCancel: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 12.dp, horizontal = 8.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Box(
      modifier = Modifier
        .size(64.dp)
        .clip(CircleShape)
        .background(DustyBlueLight),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = IconlyIcons.Camera,
        contentDescription = null,
        tint = DustyBlue,
        modifier = Modifier.size(32.dp)
      )
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
      text = "Camera Access Required",
      style = MaterialTheme.typography.titleLarge.copy(
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.3).sp
      ),
      color = BrandBlack,
      textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
      text = "MemoryOS uses your camera to capture photos of receipts, handwritten notes, places, and moments to enrich your personal memory database.",
      style = MaterialTheme.typography.bodyMedium,
      color = TextSecondary,
      textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(24.dp))

    val context = LocalContext.current

    Button(
      onClick = onRequestPermission,
      shape = RoundedCornerShape(18.dp),
      colors = ButtonDefaults.buttonColors(
        containerColor = MidnightNavy,
        contentColor = PureWhite
      ),
      modifier = Modifier
        .fillMaxWidth()
        .height(50.dp)
    ) {
      Text("Enable Camera Access", fontWeight = FontWeight.Bold)
    }

    Spacer(modifier = Modifier.height(10.dp))

    OutlinedButton(
      onClick = {
        val intent = android.content.Intent(
          android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
          android.net.Uri.fromParts("package", context.packageName, null)
        )
        context.startActivity(intent)
      },
      shape = RoundedCornerShape(18.dp),
      colors = ButtonDefaults.outlinedButtonColors(
        contentColor = MidnightNavy
      ),
      border = BorderStroke(1.dp, CardBorder),
      modifier = Modifier
        .fillMaxWidth()
        .height(48.dp)
    ) {
      Text("Open App Settings", fontWeight = FontWeight.SemiBold)
    }

    Spacer(modifier = Modifier.height(8.dp))

    TextButton(
      onClick = onCancel,
      modifier = Modifier.fillMaxWidth()
    ) {
      Text("Not Now", color = TextMuted)
    }
  }
}

/**
 * Helper to create temporary image file for CameraX captured photos
 */
private fun createTempImageFile(context: Context): File {
  val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())
  val storageDir = context.getExternalFilesDir("captured_memories") ?: context.cacheDir
  if (!storageDir.exists()) {
    storageDir.mkdirs()
  }
  return File.createTempFile("MEMORY_${timeStamp}_", ".jpg", storageDir)
}
