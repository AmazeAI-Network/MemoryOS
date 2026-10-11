package com.example.feedback

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.view.PixelCopy
import android.view.View
import androidx.core.content.FileProvider
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

/**
 * Decentralized Bug Reporting and User Feedback Engine.
 * Gathers hardware specs, captures real-time window pixels via PixelCopy,
 * and transmits payload to a custom webhook or native share fallback.
 */
class FeedbackManager(
  private val context: Context,
  private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(20, TimeUnit.SECONDS)
    .build()
) {

  private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
  private val adapter = moshi.adapter(FeedbackPayload::class.java)

  /**
   * Captures a real-time hardware-accelerated screenshot of the current Activity window.
   */
  suspend fun captureActivityScreenshot(activity: Activity): Bitmap? = withContext(Dispatchers.Main) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val window = activity.window ?: return@withContext null
      val view = window.decorView
      val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
      val location = IntArray(2)
      view.getLocationInWindow(location)

      suspendCancellableCoroutine { continuation ->
        try {
          PixelCopy.request(
            window,
            Rect(location[0], location[1], location[0] + view.width, location[1] + view.height),
            bitmap,
            { copyResult ->
              if (copyResult == PixelCopy.SUCCESS) {
                continuation.resume(bitmap)
              } else {
                continuation.resume(captureViewFallback(view))
              }
            },
            Handler(Looper.getMainLooper())
          )
        } catch (_: Exception) {
          continuation.resume(captureViewFallback(view))
        }
      }
    } else {
      captureViewFallback(activity.window?.decorView)
    }
  }

  private fun captureViewFallback(view: View?): Bitmap? {
    if (view == null || view.width <= 0 || view.height <= 0) return null
    return try {
      val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.RGB_565)
      val canvas = android.graphics.Canvas(bitmap)
      view.draw(canvas)
      bitmap
    } catch (_: Exception) {
      null
    }
  }

  fun bitmapToBase64(bitmap: Bitmap, qualityPercent: Int = 75): String {
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, qualityPercent, outputStream)
    val bytes = outputStream.toByteArray()
    return Base64.encodeToString(bytes, Base64.NO_WRAP)
  }

  fun saveScreenshotToFile(bitmap: Bitmap): File? {
    return try {
      val file = File(context.cacheDir, "feedback_screenshot_${System.currentTimeMillis()}.jpg")
      val stream = FileOutputStream(file)
      bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
      stream.flush()
      stream.close()
      file
    } catch (_: Exception) {
      null
    }
  }

  /**
   * Transmits the payload to a remote webhook or backend endpoint.
   */
  suspend fun sendReport(
    payload: FeedbackPayload,
    webhookUrl: String = DEFAULT_FEEDBACK_WEBHOOK
  ): Result<Unit> = withContext(Dispatchers.IO) {
    try {
      val json = adapter.toJson(payload)
      val body = json.toRequestBody("application/json; charset=utf-8".toMediaType())

      val request = Request.Builder()
        .url(webhookUrl)
        .post(body)
        .build()

      val response = okHttpClient.newCall(request).execute()
      if (response.isSuccessful) {
        Result.success(Unit)
      } else {
        Result.failure(Exception("Server returned status: ${response.code}"))
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /**
   * Native Intent Fallback: Opens email client with device diagnostics and screenshot attached.
   */
  fun shareFeedbackViaEmail(
    activity: Activity,
    payload: FeedbackPayload,
    screenshotFile: File?
  ) {
    val emailBody = buildString {
      append("Category: ${payload.category}\n\n")
      append("Message:\n${payload.userMessage}\n\n")
      append("--- Device Specifications ---\n")
      append("Device: ${payload.deviceManufacturer} ${payload.deviceModel}\n")
      append("Android OS: ${payload.androidVersion}\n")
      append("App Version: ${payload.appVersion}\n")
      append("Available RAM: ${payload.availableMemoryMb} MB / ${payload.totalMemoryMb} MB\n")
      append("Timestamp: ${java.util.Date(payload.timestamp)}\n")
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
      type = if (screenshotFile != null) "image/jpeg" else "text/plain"
      putExtra(Intent.EXTRA_EMAIL, arrayOf("bintangjanuarda0809@gmail.com"))
      putExtra(Intent.EXTRA_SUBJECT, "[MemoryOS Feedback] ${payload.category}: ${payload.deviceModel}")
      putExtra(Intent.EXTRA_TEXT, emailBody)

      if (screenshotFile != null && screenshotFile.exists()) {
        val uri: Uri = FileProvider.getUriForFile(
          context,
          "${context.packageName}.fileprovider",
          screenshotFile
        )
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      }
    }

    try {
      activity.startActivity(Intent.createChooser(intent, "Send Feedback"))
    } catch (_: Exception) {
      // Fallback
    }
  }

  companion object {
    // Custom webhook endpoint (e.g. Discord, Slack, or custom API gateway)
    const val DEFAULT_FEEDBACK_WEBHOOK = "https://httpbin.org/post"
  }
}
