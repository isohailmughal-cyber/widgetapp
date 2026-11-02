package com.example.smartwidget

import android.content.Context
import android.content.SharedPreferences

class WidgetPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    fun saveTargetDate(appWidgetId: Int, epochMillis: Long) {
        prefs.edit().putLong(KEY_TARGET_DATE_PREFIX + appWidgetId, epochMillis).apply()
    }

    fun getTargetDate(appWidgetId: Int): Long {
        return prefs.getLong(KEY_TARGET_DATE_PREFIX + appWidgetId, 0L)
    }

    fun saveBgImageUri(appWidgetId: Int, uriString: String?) {
        prefs.edit().putString(KEY_BG_URI_PREFIX + appWidgetId, uriString).apply()
    }

    fun getBgImageUri(appWidgetId: Int): String? {
        return prefs.getString(KEY_BG_URI_PREFIX + appWidgetId, null)
    }

    fun saveEventTitle(appWidgetId: Int, title: String) {
        prefs.edit().putString(KEY_EVENT_TITLE_PREFIX + appWidgetId, title).apply()
    }

    fun getEventTitle(appWidgetId: Int): String? {
        return prefs.getString(KEY_EVENT_TITLE_PREFIX + appWidgetId, "My Goal Countdown")
    }

    fun saveCachedSteps(steps: Int) {
        prefs.edit().putInt(KEY_CACHED_STEPS, steps).apply()
    }

    fun getCachedSteps(): Int {
        return prefs.getInt(KEY_CACHED_STEPS, 4280)
    }

    fun saveStepGoal(goal: Int) {
        prefs.edit().putInt(KEY_STEP_GOAL, goal).apply()
    }

    fun getStepGoal(): Int {
        return prefs.getInt(KEY_STEP_GOAL, 10000)
    }

    fun deleteWidgetData(appWidgetId: Int) {
        prefs.edit()
            .remove(KEY_TARGET_DATE_PREFIX + appWidgetId)
            .remove(KEY_BG_URI_PREFIX + appWidgetId)
            .remove(KEY_EVENT_TITLE_PREFIX + appWidgetId)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "com.example.smartwidget.prefs"
        private const val KEY_TARGET_DATE_PREFIX = "target_epoch_"
        private const val KEY_BG_URI_PREFIX = "bg_uri_"
        private const val KEY_EVENT_TITLE_PREFIX = "event_title_"
        private const val KEY_CACHED_STEPS = "cached_step_count"
        private const val KEY_STEP_GOAL = "target_step_goal"
    }
}