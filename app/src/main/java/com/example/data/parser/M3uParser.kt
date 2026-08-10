package com.example.data.parser

import com.example.data.entity.ChannelEntity
import com.example.data.entity.StreamType
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

object M3uParser {

    fun parse(inputStream: InputStream, playlistId: Long): List<ChannelEntity> {
        val channels = mutableListOf<ChannelEntity>()
        val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))

        var currentTvgId: String? = null
        var currentTvgName: String? = null
        var currentLogo: String? = null
        var currentGroup = "عام"
        var currentName: String? = null

        reader.useLines { lines ->
            for (rawLine in lines) {
                val line = rawLine.trim()
                if (line.isEmpty()) continue

                if (line.startsWith("#EXTINF:")) {
                    // Extract attributes
                    currentTvgId = extractAttribute(line, "tvg-id")
                    currentTvgName = extractAttribute(line, "tvg-name")
                    currentLogo = extractAttribute(line, "tvg-logo")
                    currentGroup = extractAttribute(line, "group-title") ?: "عام"

                    // Extract name after comma
                    val commaIdx = line.indexOf(',')
                    if (commaIdx != -1 && commaIdx < line.length - 1) {
                        currentName = line.substring(commaIdx + 1).trim()
                    } else {
                        currentName = currentTvgName ?: "قناة بدون اسم"
                    }
                } else if (!line.startsWith("#")) {
                    // This is stream URL
                    val streamUrl = line
                    val name = currentName ?: currentTvgName ?: "قناة بدون اسم"
                    val group = currentGroup

                    // Determine stream type
                    val lowerGroup = group.lowercase()
                    val lowerUrl = streamUrl.lowercase()
                    val streamType = when {
                        lowerGroup.contains("movie") || lowerGroup.contains("أفلام") || lowerGroup.contains("فلم") || lowerGroup.contains("افلام") || lowerUrl.endsWith(".mp4") || lowerUrl.endsWith(".mkv") -> StreamType.MOVIE
                        lowerGroup.contains("series") || lowerGroup.contains("مسلسلات") || lowerGroup.contains("مسلسل") || lowerUrl.contains("/series/") -> StreamType.SERIES
                        else -> StreamType.LIVE
                    }

                    channels.add(
                        ChannelEntity(
                            playlistId = playlistId,
                            name = name,
                            logo = currentLogo,
                            groupTitle = group,
                            streamUrl = streamUrl,
                            streamType = streamType,
                            tvgId = currentTvgId ?: currentTvgName
                        )
                    )

                    // Reset per-channel temp attributes
                    currentTvgId = null
                    currentTvgName = null
                    currentLogo = null
                    currentName = null
                    currentGroup = "عام"
                }
            }
        }

        return channels
    }

    private fun extractAttribute(line: String, attrName: String): String? {
        val pattern = "$attrName=\"([^\"]*)\"".toRegex(RegexOption.IGNORE_CASE)
        val match = pattern.find(line)
        if (match != null) {
            return match.groupValues[1].ifEmpty { null }
        }
        // Try without quotes
        val noQuotePattern = "$attrName=([^\\s,]+)".toRegex(RegexOption.IGNORE_CASE)
        val noQuoteMatch = noQuotePattern.find(line)
        return noQuoteMatch?.groupValues?.get(1)?.ifEmpty { null }
    }
}
