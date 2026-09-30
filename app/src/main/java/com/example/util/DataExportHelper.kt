package com.example.util

import android.content.Context
import android.content.Intent
import com.example.data.model.MemoryEntity
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DataExportHelper {

  fun formatMemoriesAsJson(memories: List<MemoryEntity>): String {
    val array = JSONArray()
    memories.forEach { mem ->
      val obj = JSONObject().apply {
        put("id", mem.id)
        put("title", mem.title)
        put("memoryType", mem.memoryType)
        put("category", mem.category)
        put("summary", mem.summary)
        put("rawText", mem.rawText)
        put("tags", mem.tags)
        put("merchant", mem.merchant ?: JSONObject.NULL)
        put("amount", mem.amount ?: JSONObject.NULL)
        put("currency", mem.currency ?: JSONObject.NULL)
        put("eventDate", mem.eventDate ?: JSONObject.NULL)
        put("location", mem.location ?: JSONObject.NULL)
        put("people", mem.people ?: JSONObject.NULL)
        put("isFavorite", mem.isFavorite)
        put("createdAt", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(mem.createdAt)))
      }
      array.put(obj)
    }
    return array.toString(2)
  }

  fun formatMemoriesAsMarkdown(memories: List<MemoryEntity>): String {
    val sb = StringBuilder()
    sb.append("# MemoryOS Export\n")
    sb.append("Exported on ${SimpleDateFormat("MMMM dd, yyyy", Locale.US).format(Date())}\n\n")
    memories.forEachIndexed { index, mem ->
      sb.append("## ${index + 1}. ${mem.title}\n")
      sb.append("- **Category**: ${mem.category} | **Type**: ${mem.memoryType}\n")
      if (!mem.eventDate.isNullOrBlank()) sb.append("- **Date**: ${mem.eventDate}\n")
      if (!mem.merchant.isNullOrBlank()) sb.append("- **Merchant**: ${mem.merchant}\n")
      if (mem.amount != null) sb.append("- **Amount**: ${mem.currency ?: "$"}${mem.amount}\n")
      if (!mem.location.isNullOrBlank()) sb.append("- **Location**: ${mem.location}\n")
      if (mem.tags.isNotBlank()) sb.append("- **Tags**: ${mem.tags}\n")
      sb.append("\n**Summary**:\n${mem.summary}\n\n")
      if (mem.rawText.isNotBlank()) {
        sb.append("**Original Note / Transcript**:\n> ${mem.rawText.replace("\n", "\n> ")}\n\n")
      }
      sb.append("---\n\n")
    }
    return sb.toString()
  }

  fun shareExport(context: Context, content: String, title: String = "Export MemoryOS Data") {
    val intent = Intent(Intent.ACTION_SEND).apply {
      type = "text/plain"
      putExtra(Intent.EXTRA_SUBJECT, title)
      putExtra(Intent.EXTRA_TEXT, content)
    }
    context.startActivity(Intent.createChooser(intent, "Share Memories"))
  }
}
