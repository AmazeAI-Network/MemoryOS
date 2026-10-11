package com.example.crash

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import java.io.PrintWriter
import java.io.StringWriter

@JsonClass(generateAdapter = true)
data class CrashDetails(
  val timestamp: Long = System.currentTimeMillis(),
  val errorType: String,
  val errorMessage: String,
  val stackTrace: String,
  val threadName: String,
  val deviceManufacturer: String = Build.MANUFACTURER,
  val deviceModel: String = Build.MODEL,
  val androidVersion: String = "${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})",
  val appVersion: String = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
  val availableMemoryMb: Long = 0L,
  val totalMemoryMb: Long = 0L
) {
  companion object {
    fun fromThrowable(context: Context, thread: Thread, throwable: Throwable): CrashDetails {
      val stringWriter = StringWriter()
      throwable.printStackTrace(PrintWriter(stringWriter))

      val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
      val memInfo = ActivityManager.MemoryInfo()
      actManager?.getMemoryInfo(memInfo)

      val freeMb = memInfo.availMem / (1024 * 1024)
      val totalMb = memInfo.totalMem / (1024 * 1024)

      return CrashDetails(
        errorType = throwable.javaClass.simpleName,
        errorMessage = throwable.message ?: "No error message provided",
        stackTrace = stringWriter.toString(),
        threadName = thread.name,
        availableMemoryMb = freeMb,
        totalMemoryMb = totalMb
      )
    }
  }
}
