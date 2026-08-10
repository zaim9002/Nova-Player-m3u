package com.example.data.parser

import com.example.data.entity.ChannelEntity
import com.example.data.entity.StreamType
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class XtreamParser(
    private val serverUrl: String,
    private val username: String,
    private val password: String
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val baseUrl = cleanServerUrl(serverUrl)

    private fun cleanServerUrl(url: String): String {
        var clean = url.trim()
        if (!clean.startsWith("http://") && !clean.startsWith("https://")) {
            clean = "http://$clean"
        }
        if (clean.endsWith("/")) {
            clean = clean.substring(0, clean.length - 1)
        }
        return clean
    }

    suspend fun authenticateAndFetchChannels(playlistId: Long): List<ChannelEntity> {
        val channels = mutableListOf<ChannelEntity>()

        // 1. Fetch Categories for Live, Movie, Series
        val liveCategoryMap = fetchCategories("get_live_categories")
        val vodCategoryMap = fetchCategories("get_vod_categories")
        val seriesCategoryMap = fetchCategories("get_series_categories")

        // 2. Fetch Live Streams
        val liveJson = fetchJson("get_live_streams")
        if (liveJson != null) {
            for (i in 0 until liveJson.length()) {
                val item = liveJson.optJSONObject(i) ?: continue
                val streamId = item.optString("stream_id")
                val name = item.optString("name", "قناة بدون اسم")
                val logo = item.optString("stream_icon")
                val catId = item.optString("category_id")
                val groupName = liveCategoryMap[catId] ?: "مباشر"
                val tvgId = item.optString("epg_channel_id")

                val streamUrl = "$baseUrl/live/$username/$password/$streamId.m3u8"

                channels.add(
                    ChannelEntity(
                        playlistId = playlistId,
                        name = name,
                        logo = logo.ifEmpty { null },
                        groupTitle = groupName,
                        streamUrl = streamUrl,
                        streamType = StreamType.LIVE,
                        tvgId = tvgId.ifEmpty { null }
                    )
                )
            }
        }

        // 3. Fetch Movies
        val vodJson = fetchJson("get_vod_streams")
        if (vodJson != null) {
            for (i in 0 until vodJson.length()) {
                val item = vodJson.optJSONObject(i) ?: continue
                val streamId = item.optString("stream_id")
                val name = item.optString("name", "فيلم بدون اسم")
                val logo = item.optString("stream_icon")
                val catId = item.optString("category_id")
                val groupName = vodCategoryMap[catId] ?: "أفلام"
                val containerExt = item.optString("container_extension", "mp4")
                val rating = item.optString("rating")

                val streamUrl = "$baseUrl/movie/$username/$password/$streamId.$containerExt"

                channels.add(
                    ChannelEntity(
                        playlistId = playlistId,
                        name = name,
                        logo = logo.ifEmpty { null },
                        groupTitle = groupName,
                        streamUrl = streamUrl,
                        streamType = StreamType.MOVIE,
                        rating = rating.ifEmpty { null }
                    )
                )
            }
        }

        // 4. Fetch Series
        val seriesJson = fetchJson("get_series")
        if (seriesJson != null) {
            for (i in 0 until seriesJson.length()) {
                val item = seriesJson.optJSONObject(i) ?: continue
                val seriesId = item.optString("series_id")
                val name = item.optString("name", "مسلسل بدون اسم")
                val logo = item.optString("cover")
                val catId = item.optString("category_id")
                val groupName = seriesCategoryMap[catId] ?: "مسلسلات"
                val rating = item.optString("rating")
                val plot = item.optString("plot")

                val streamUrl = "$baseUrl/series/$username/$password/$seriesId.mp4"

                channels.add(
                    ChannelEntity(
                        playlistId = playlistId,
                        name = name,
                        logo = logo.ifEmpty { null },
                        groupTitle = groupName,
                        streamUrl = streamUrl,
                        streamType = StreamType.SERIES,
                        rating = rating.ifEmpty { null },
                        description = plot.ifEmpty { null }
                    )
                )
            }
        }

        return channels
    }

    private fun fetchCategories(action: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        val jsonArray = fetchJson(action) ?: return map
        for (i in 0 until jsonArray.length()) {
            val item = jsonArray.optJSONObject(i) ?: continue
            val id = item.optString("category_id")
            val name = item.optString("category_name")
            if (id.isNotEmpty() && name.isNotEmpty()) {
                map[id] = name
            }
        }
        return map
    }

    private fun fetchJson(action: String): JSONArray? {
        val url = "$baseUrl/player_api.php?username=$username&password=$password&action=$action"
        return try {
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return null
            val bodyStr = response.body?.string() ?: return null
            JSONArray(bodyStr)
        } catch (e: Exception) {
            null
        }
    }
}
