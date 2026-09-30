package com.example.crash

import android.content.Context
import android.content.Intent
import android.os.Process
import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File
import kotlin.system.exitProcess

/**
 * Global uncaught exception interceptor.
 * Catches fatal crashes, generates JSON diagnostics, and launches CrashReportActivity.
 */
class GlobalCrashHandler(
  private val context: Context,
  private val defaultHandler: Thread.UncaughtExceptionHandler? = Thread.getDefaultUncaughtExceptionHandler()
) : Thread.UncaughtExceptionHandler {

  private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
  private val adapter = moshi.adapter(CrashDetails::class.java)

  override fun uncaughtException(thread: Thread, throwable: Throwable) {
    try {
      Log.e(TAG, "Fatal uncaught exception intercepted on thread: ${thread.name}", throwable)

      val crashDetails = CrashDetails.fromThrowable(context, thread, throwable)
      val json = adapter.toJson(crashDetails)

      // Persist to local storage
      val crashDir = File(context.filesDir, "crashes")
      if (!crashDir.exists()) {
        crashDir.mkdirs()
      }
      val crashFile = File(crashDir, "crash_${crashDetails.timestamp}.json")
      crashFile.writeText(json)

      // Launch Recovery UI in a separate, isolated task
      val intent = Intent(context, CrashReportActivity::class.java).apply {
        putExtra(CrashReportActivity.EXTRA_CRASH_JSON, json)
        putExtra(CrashReportActivity.EXTRA_CRASH_FILE_PATH, crashFile.absolutePath)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
      }
      context.startActivity(intent)

      // Kill the crashed process cleanly
      Process.killProcess(Process.myPid())
      exitProcess(10)
    } catch (e: Exception) {
      Log.e(TAG, "Failed during crash handling, routing to default handler", e)
      defaultHandler?.uncaughtException(thread, throwable)
    }
  }

  companion object {
    private const val TAG = "GlobalCrashHandler"

    fun install(context: Context) {
      val current = Thread.getDefaultUncaughtExceptionHandler()
      if (current !is GlobalCrashHandler) {
        Thread.setDefaultUncaughtExceptionHandler(GlobalCrashHandler(context, current))
      }
    }
  }
}
