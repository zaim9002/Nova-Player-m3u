package com.example.data.pref

import android.content.Context
import android.content.SharedPreferences

class NovaPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("nova_player_settings", Context.MODE_PRIVATE)

    var parentalPin: String
        get() = prefs.getString("parental_pin", "") ?: ""
        set(value) = prefs.edit().putString("parental_pin", value).apply()

    var isParentalEnabled: Boolean
        get() = prefs.getBoolean("parental_enabled", false)
        set(value) = prefs.edit().putBoolean("parental_enabled", value).apply()

    var appLanguage: String
        get() = prefs.getString("app_language", "ar") ?: "ar"
        set(value) = prefs.edit().putString("app_language", value).apply()

    var isAutoPlay: Boolean
        get() = prefs.getBoolean("auto_play", true)
        set(value) = prefs.edit().putBoolean("auto_play", value).apply()

    var defaultPlayer: String
        get() = prefs.getString("default_player", "ExoPlayer (Media3)") ?: "ExoPlayer (Media3)"
        set(value) = prefs.edit().putString("default_player", value).apply()

    var subtitleSize: Float
        get() = prefs.getFloat("subtitle_size", 18f)
        set(value) = prefs.edit().putFloat("subtitle_size", value).apply()

    var lockedGroups: Set<String>
        get() = prefs.getStringSet("locked_groups", emptySet()) ?: emptySet()
        set(value) = prefs.edit().putStringSet("locked_groups", value).apply()

    fun lockGroup(groupName: String) {
        val current = lockedGroups.toMutableSet()
        current.add(groupName)
        lockedGroups = current
    }

    fun unlockGroup(groupName: String) {
        val current = lockedGroups.toMutableSet()
        current.remove(groupName)
        lockedGroups = current
    }

    fun isGroupLocked(groupName: String): Boolean {
        return isParentalEnabled && lockedGroups.contains(groupName)
    }
}
