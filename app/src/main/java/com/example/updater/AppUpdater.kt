package com.example.updater

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.content.FileProvider
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Enterprise-grade in-app auto-updater tailored for off-store & independent distribution.
 * Bypasses Google Play / Samsung Store dependencies using Android DownloadManager & FileProvider.
 */
class AppUpdater(
  private val context: Context,
  private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(15, TimeUnit.SECONDS)
    .build()
) {

  private val moshi = Moshi.Builder()
    .addLast(KotlinJsonAdapterFactory())
    .build()

  private val adapter = moshi.adapter(AppUpdateInfo::class.java)

  /**
   * Checks remote server for the latest available APK release.
   * Compares remote versionCode against local BuildConfig.VERSION_CODE.
   */
  suspend fun checkForUpdate(
    versionCheckUrl: String = DEFAULT_UPDATE_URL
  ): Result<AppUpdateInfo?> = withContext(Dispatchers.IO) {
    try {
      val request = Request.Builder()
        .url(versionCheckUrl)
        .header("User-Agent", "MemoryOS/${BuildConfig.VERSION_NAME} (Android ${Build.VERSION.RELEASE})")
        .header("Cache-Control", "no-cache")
        .build()

      val response = okHttpClient.newCall(request).execute()
      if (!response.isSuccessful) {
        return@withContext Result.failure(
          Exception("Update server returned HTTP ${response.code}: ${response.message}")
        )
      }

      val bodyString = response.body?.string()
        ?: return@withContext Result.failure(Exception("Empty update response"))

      val updateInfo = adapter.fromJson(bodyString)
        ?: return@withContext Result.failure(Exception("Failed to parse update manifest"))

      val localVersionCode = BuildConfig.VERSION_CODE
      if (updateInfo.versionCode > localVersionCode) {
        Result.success(updateInfo)
      } else {
        Result.success(null) // App is up to date
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /**
   * Downloads the specified APK using Android DownloadManager and streams progress.
   */
  fun downloadApk(updateInfo: AppUpdateInfo): Flow<UpdateDownloadState> = flow {
    emit(UpdateDownloadState.Downloading(0L, updateInfo.fileSizeBytes, 0))

    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
    if (downloadManager == null) {
      emit(UpdateDownloadState.Error("System DownloadManager is unavailable"))
      return@flow
    }

    val downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
      ?: context.filesDir

    if (!downloadsDir.exists()) {
      downloadsDir.mkdirs()
    }

    val targetApk = File(downloadsDir, "MemoryOS-v${updateInfo.versionName}.apk")
    if (targetApk.exists()) {
      targetApk.delete()
    }

    val request = DownloadManager.Request(Uri.parse(updateInfo.apkUrl))
      .setTitle("MemoryOS Update v${updateInfo.versionName}")
      .setDescription("Downloading latest release...")
      .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
      .setDestinationUri(Uri.fromFile(targetApk))
      .setAllowedOverMetered(true)
      .setAllowedOverRoaming(false)

    val downloadId = try {
      downloadManager.enqueue(request)
    } catch (e: Exception) {
      emit(UpdateDownloadState.Error("Failed to enqueue download: ${e.localizedMessage}"))
      return@flow
    }

    var downloading = true
    while (downloading) {
      val query = DownloadManager.Query().setFilterById(downloadId)
      val cursor = downloadManager.query(query)

      if (cursor != null && cursor.moveToFirst()) {
        val statusIdx = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
        val downloadedIdx = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
        val totalIdx = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)

        val status = if (statusIdx >= 0) cursor.getInt(statusIdx) else -1
        val bytesDownloaded = if (downloadedIdx >= 0) cursor.getLong(downloadedIdx) else 0L
        val totalBytes = if (totalIdx >= 0) cursor.getLong(totalIdx) else updateInfo.fileSizeBytes

        val percent = if (totalBytes > 0) ((bytesDownloaded * 100) / totalBytes).toInt().coerceIn(0, 100) else 0

        when (status) {
          DownloadManager.STATUS_RUNNING, DownloadManager.STATUS_PAUSED, DownloadManager.STATUS_PENDING -> {
            emit(UpdateDownloadState.Downloading(bytesDownloaded, totalBytes, percent))
            delay(400)
          }
          DownloadManager.STATUS_SUCCESSFUL -> {
            downloading = false
            emit(UpdateDownloadState.DownloadCompleted(targetApk.absolutePath, updateInfo))
          }
          DownloadManager.STATUS_FAILED -> {
            downloading = false
            val reasonIdx = cursor.getColumnIndex(DownloadManager.COLUMN_REASON)
            val reason = if (reasonIdx >= 0) cursor.getInt(reasonIdx) else -1
            emit(UpdateDownloadState.Error("APK download failed (Reason code $reason)"))
          }
        }
        cursor.close()
      } else {
        cursor?.close()
        delay(500)
      }
    }
  }.flowOn(Dispatchers.IO)

  /**
   * Checks whether the app has permission to install unknown apps on Android 8.0+ (Oreo).
   */
  fun canRequestPackageInstalls(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      context.packageManager.canRequestPackageInstalls()
    } else {
      true
    }
  }

  /**
   * Launches system settings to request install permission for this package.
   */
  fun openInstallPermissionSettings() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
        data = Uri.parse("package:${context.packageName}")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(intent)
    }
  }

  /**
   * Triggers the Android package installer Intent using FileProvider.
   * Compliant with Android 7.0 (Nougat) through Android 15.
   */
  fun triggerInstall(apkFile: File): Result<Unit> {
    return try {
      if (!apkFile.exists()) {
        return Result.failure(Exception("APK file not found at: ${apkFile.absolutePath}"))
      }

      val contentUri: Uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        apkFile
      )

      val installIntent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(contentUri, "application/vnd.android.package-archive")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      }

      context.startActivity(installIntent)
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  companion object {
    // Configurable endpoint for independent release distribution (e.g., GitHub Releases / Web server)
    const val DEFAULT_UPDATE_URL = "https://raw.githubusercontent.com/nobodyhellow/memoryos/main/release-manifest.json"
  }
}
