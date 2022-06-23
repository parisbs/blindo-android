package com.pbaltazar.blindo.ui.preferences

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.pbaltazar.blindo.R

class BlindoVisionPreferencesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blindo_vision_preferences)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(
                    R.id.settings,
                    BlindoVisionPreferencesFragment()
                )
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
}
