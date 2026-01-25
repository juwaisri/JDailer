package com.jdailer.presentation.incall

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jdailer.R

class InCallActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_placeholder)
        title = getString(R.string.in_call_title)
    }
}
