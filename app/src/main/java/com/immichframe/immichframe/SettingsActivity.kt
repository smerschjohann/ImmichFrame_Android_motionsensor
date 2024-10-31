package com.immichframe.immichframe

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SettingsActivity : AppCompatActivity() {
    private lateinit var editTextUrl: EditText
    private lateinit var buttonSaveUrl: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        editTextUrl = findViewById(R.id.editTextUrl)
        buttonSaveUrl = findViewById(R.id.buttonSaveUrl)

        // Load saved URL
        loadSavedUrl()

        buttonSaveUrl.setOnClickListener {
            val url = editTextUrl.text.toString()
            if (url.isNotEmpty()) {
                // Save the URL
                saveUrl(url)
                Toast.makeText(this, "URL saved!", Toast.LENGTH_SHORT).show()
                setResult(Activity.RESULT_OK)
                finish() // Close the settings activity and return to the previous activity
            }
        }
    }

    private fun loadSavedUrl() {
        val sharedPreferences = getSharedPreferences("ImmichFramePrefs", MODE_PRIVATE)
        val savedUrl = sharedPreferences.getString("webview_url", "https://www.example.com")
        editTextUrl.setText(savedUrl)
    }

    private fun saveUrl(url: String) {
        val sharedPreferences = getSharedPreferences("ImmichFramePrefs", MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("webview_url", url)
            apply()
        }
    }
}