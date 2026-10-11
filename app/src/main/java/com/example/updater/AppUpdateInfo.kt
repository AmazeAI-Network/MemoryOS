package com.example.updater

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AppUpdateInfo(
  @property:Json(name = "versionCode")
  val versionCode: Int,

  @property:Json(name = "versionName")
  val versionName: String,

  @property:Json(name = "apkUrl")
  val apkUrl: String,

  @property:Json(name = "releaseNotes")
  val releaseNotes: String = "",

  @property:Json(name = "forceUpdate")
  val forceUpdate: Boolean = false,

  @property:Json(name = "fileSizeBytes")
  val fileSizeBytes: Long = 0L,

  @property:Json(name = "sha256")
  val sha256: String? = null
)

sealed interface UpdateDownloadState {
  data object Idle : UpdateDownloadState
  data class Checking(val message: String = "Checking for updates...") : UpdateDownloadState
  data class Available(val updateInfo: AppUpdateInfo) : UpdateDownloadState
  data object UpToDate : UpdateDownloadState
  data class Downloading(
    val downloadedBytes: Long,
    val totalBytes: Long,
    val progressPercent: Int
  ) : UpdateDownloadState
  data class DownloadCompleted(val apkPath: String, val updateInfo: AppUpdateInfo) : UpdateDownloadState
  data class Error(val message: String, val canRetry: Boolean = true) : UpdateDownloadState
}
