package com.jdailer.presentation.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jdailer.R

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_placeholder)
        title = getString(R.string.settings)
    }
}
