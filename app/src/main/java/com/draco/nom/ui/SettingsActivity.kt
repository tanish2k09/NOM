package com.draco.nom.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.draco.nom.R

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        /* Replace our container with our settings fragment */
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settingsActivity, SettingsPreferenceFragment())
            .commit()
    }

    override fun onDestroy() {
        super.onDestroy()
        finishAffinity()
        startActivity(Intent(this, MainActivity::class.java))
    }
}