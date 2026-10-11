package com.example.feedback

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import com.example.BuildConfig
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FeedbackPayload(
  val category: String,
  val userMessage: String,
  val timestamp: Long = System.currentTimeMillis(),
  val deviceManufacturer: String = Build.MANUFACTURER,
  val deviceModel: String = Build.MODEL,
  val androidVersion: String = "${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})",
  val appVersion: String = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
  val availableMemoryMb: Long = 0L,
  val totalMemoryMb: Long = 0L,
  val screenshotBase64: String? = null
) {
  companion object {
    fun create(
      context: Context,
      category: String,
      message: String,
      screenshotBase64: String?
    ): FeedbackPayload {
      val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
      val memInfo = ActivityManager.MemoryInfo()
      actManager?.getMemoryInfo(memInfo)

      return FeedbackPayload(
        category = category,
        userMessage = message,
        availableMemoryMb = memInfo.availMem / (1024 * 1024),
        totalMemoryMb = memInfo.totalMem / (1024 * 1024),
        screenshotBase64 = screenshotBase64
      )
    }
  }
}
