package com.example.smartwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.view.View
import android.widget.RemoteViews
import java.util.concurrent.TimeUnit
import kotlin.math.max

class SmartWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val prefs = WidgetPreferences(context)
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, prefs)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        val prefs = WidgetPreferences(context)
        for (appWidgetId in appWidgetIds) {
            prefs.deleteWidgetData(appWidgetId)
        }
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            prefs: WidgetPreferences
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout)

            // 1. Calculate Countdown (Days only, no hours/minutes/seconds)
            val targetEpoch = prefs.getTargetDate(appWidgetId)
            val now = System.currentTimeMillis()
            val diff = targetEpoch - now

            if (targetEpoch > 0 && diff > 0) {
                val days = TimeUnit.MILLISECONDS.toDays(diff)
                views.setTextViewText(R.id.tv_countdown_days, days.toString())
                views.setTextViewText(R.id.tv_countdown_subtitle, if (days == 1L) "Day Remaining" else "Days Remaining")
                views.setTextViewText(R.id.tv_event_title, prefs.getEventTitle(appWidgetId) ?: "Target Event")
                
                // Formatted prominent date
                val calendar = java.util.Calendar.getInstance().apply { timeInMillis = targetEpoch }
                val dateStr = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.US).format(calendar.time)
                views.setTextViewText(R.id.tv_target_date, dateStr)
            } else if (targetEpoch > 0 && diff <= 0) {
                views.setTextViewText(R.id.tv_countdown_days, "0")
                views.setTextViewText(R.id.tv_countdown_subtitle, "Goal Arrived!")
                views.setTextViewText(R.id.tv_event_title, "Target Reached")
                views.setTextViewText(R.id.tv_target_date, "Completed")
            } else {
                views.setTextViewText(R.id.tv_countdown_days, "--")
                views.setTextViewText(R.id.tv_countdown_subtitle, "Tap to set date")
                views.setTextViewText(R.id.tv_event_title, "No Event Set")
                views.setTextViewText(R.id.tv_target_date, "Select Date")
            }

            // Current Today Date
            val todayCalendar = java.util.Calendar.getInstance()
            val todayStr = java.text.SimpleDateFormat("EEE, MMM dd, yyyy", java.util.Locale.US).format(todayCalendar.time)
            views.setTextViewText(R.id.tv_current_date, "Today: $todayStr")

            // 2. Battery Indicator (reads current battery level & status)
            val batteryStatus = context.registerReceiver(null, android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val level = batteryStatus?.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryStatus?.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1) ?: -1
            val batteryPct = if (level >= 0 && scale > 0) (level * 100) / scale else 85
            views.setTextViewText(R.id.tv_battery_level, "$batteryPct%")

            // 3. Health & Steps Tracking
            val steps = prefs.getCachedSteps()
            val goal = prefs.getStepGoal()
            val progressPercent = max(0, (steps * 100) / goal)

            views.setTextViewText(R.id.tv_steps_count, String.format("%,d", steps))
            views.setTextViewText(R.id.tv_steps_goal, "/ $goal steps")
            views.setProgressBar(R.id.pb_steps, 100, progressPercent.coerceAtMost(100), false)

            // 3. Background: Custom URI Image or Frosted Glass
            val bgUriStr = prefs.getBgImageUri(appWidgetId)
            if (!bgUriStr.isNullOrEmpty()) {
                try {
                    val uri = Uri.parse(bgUriStr)
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        val bitmap = BitmapFactory.decodeStream(stream)
                        views.setImageViewBitmap(R.id.widget_bg_image, bitmap)
                        views.setViewVisibility(R.id.widget_bg_image, View.VISIBLE)
                    }
                } catch (e: Exception) {
                    views.setViewVisibility(R.id.widget_bg_image, View.GONE)
                }
            } else {
                // Frosted Glass Theme
                views.setViewVisibility(R.id.widget_bg_image, View.GONE)
            }

            // 4. Intent to launch WidgetConfigActivity on tap
            val configIntent = Intent(context, WidgetConfigActivity::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId,
                configIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
            views.setOnClickPendingIntent(R.id.btn_widget_config, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}