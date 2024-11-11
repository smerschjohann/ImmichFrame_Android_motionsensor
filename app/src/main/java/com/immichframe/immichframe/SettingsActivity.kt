package com.immichframe.immichframe

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class SettingsActivity : AppCompatActivity() {
    private lateinit var editTextUrl: EditText
    private lateinit var buttonSaveUrl: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        editTextUrl = findViewById(R.id.editTextUrl)
        buttonSaveUrl = findViewById(R.id.buttonSaveUrl)

        loadSavedUrl()

        buttonSaveUrl.setOnClickListener {
            val url = editTextUrl.text.toString()
                if (url.isNotEmpty()) {
                    saveUrl(url)
                    Toast.makeText(this@SettingsActivity, "URL saved!", Toast.LENGTH_SHORT).show()
                    setResult(Activity.RESULT_OK)
                    finish()
                } else {
                    Toast.makeText(this@SettingsActivity, "Your ImmichFrame URL is not valid!", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun loadSavedUrl() {
        val sharedPreferences = getSharedPreferences("ImmichFramePrefs", MODE_PRIVATE)
        val savedUrl = sharedPreferences.getString("webview_url", getString(R.string.webview_url))
        editTextUrl.setText(savedUrl)
        editTextUrl.requestFocus()
        editTextUrl.selectAll()
    }

    private fun saveUrl(url: String) {
        val sharedPreferences = getSharedPreferences("ImmichFramePrefs", MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("webview_url", url)
            apply()
        }
    }
}
