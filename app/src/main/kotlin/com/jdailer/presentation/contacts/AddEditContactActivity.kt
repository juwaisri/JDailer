package com.jdailer.presentation.contacts

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jdailer.R

class AddEditContactActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_placeholder)
        title = getString(R.string.add_edit_contact_title)
    }
}
