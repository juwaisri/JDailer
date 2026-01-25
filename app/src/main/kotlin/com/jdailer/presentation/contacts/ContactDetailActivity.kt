package com.jdailer.presentation.contacts

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jdailer.R

class ContactDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_placeholder)
        title = getString(R.string.contact_detail_title)
    }
}
