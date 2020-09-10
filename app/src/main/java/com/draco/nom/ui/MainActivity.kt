package com.draco.nom.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.view.Display
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.draco.nom.*
import com.draco.nom.data.AppInfo
import com.draco.nom.utils.adapters.RecyclerAdapter
import com.draco.nom.utils.extensions.getDefaultDisplay
import com.draco.nom.utils.extensions.notificationChannelId
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerAdapter: RecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val container = findViewById<LinearLayout>(R.id.container)
        val recycler = findViewById<RecyclerView>(R.id.recycler)

        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        recyclerAdapter = RecyclerAdapter(getAppList(), recycler, sharedPrefs)
        recyclerAdapter.setHasStableIds(true)

        val displayMetrics = resources.displayMetrics
        val screenWidthDp = displayMetrics.widthPixels / displayMetrics.density
        val iconSize = resources.getDimension(R.dimen.icon_size) / displayMetrics.density
        val columns = Integer.max(5, (screenWidthDp / iconSize).toInt())

        with (recycler) {
            setItemViewCacheSize(1000)
            adapter = recyclerAdapter
            layoutManager = GridLayoutManager(context, columns)
        }

        createNotificationChannel()

        if (getDefaultDisplay(this) == Display.DEFAULT_DISPLAY &&
            !sharedPrefs.getBoolean("rotation", false)) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        val backgroundColorString = sharedPrefs.getString("background_color", "#88000000")
        try {
            val backgroundColor = Color.parseColor(backgroundColorString)
            window.statusBarColor = backgroundColor
            window.navigationBarColor = backgroundColor
            container.setBackgroundColor(backgroundColor)
        } catch (_: Exception) {}
    }

    override fun onResume() {
        super.onResume()
        recyclerAdapter.updateList(getAppList())
    }

    private fun getAppList(): ArrayList<AppInfo> {
        val launcherIntent = Intent(Intent.ACTION_MAIN, null)
        launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER)

        val activities = packageManager.queryIntentActivities(launcherIntent, 0)
        val appList = ArrayList<AppInfo>()
        for (app in activities) {
            val appId = app.activityInfo.packageName

            if (appId == packageName)
                continue

            val info = AppInfo()
            with (info) {
                id = appId
                name = app.activityInfo.loadLabel(packageManager).toString()
                img = packageManager.getApplicationIcon(appId)
            }

            appList.add(info)
        }

        appList.sortBy {
            it.name.toLowerCase(Locale.getDefault())
        }

        val settingsButton = AppInfo()
        with (settingsButton) {
            id = packageName
            name = "Settings"
            img = packageManager.getApplicationIcon(packageName)
        }

        appList.add(settingsButton)

        return appList
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(notificationChannelId, "Refocus", NotificationManager.IMPORTANCE_HIGH)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    override fun onBackPressed() {}

    override fun onDestroy() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
        super.onDestroy()
    }
}