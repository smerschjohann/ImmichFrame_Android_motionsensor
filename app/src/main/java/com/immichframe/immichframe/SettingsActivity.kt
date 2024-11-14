package com.immichframe.immichframe

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    private lateinit var editTextUrl: EditText
    private lateinit var buttonSaveUrl: Button
    private lateinit var checkboxHideButtons: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        editTextUrl = findViewById(R.id.editTextUrl)
        buttonSaveUrl = findViewById(R.id.buttonSaveUrl)
        checkboxHideButtons = findViewById(R.id.checkboxHideButtons)

        // Load settings
        loadSettings()

        buttonSaveUrl.setOnClickListener {
            val url = editTextUrl.text.toString()
            if (url.isNotEmpty()) {
                saveSettings(url, checkboxHideButtons.isChecked)
                Toast.makeText(this, "Settings saved!", Toast.LENGTH_SHORT).show()
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
    }

    private fun loadSettings() {
        val sharedPreferences = getSharedPreferences("ImmichFramePrefs", MODE_PRIVATE)
        val savedUrl = sharedPreferences.getString("webview_url", getString(R.string.webview_url))
        val savedHideButtons = sharedPreferences.getBoolean("hideButtons",false)
        editTextUrl.setText(savedUrl)
        editTextUrl.requestFocus()
        checkboxHideButtons.isChecked = savedHideButtons
    }

    private fun saveSettings(url: String, hideButtons: Boolean) {
        val sharedPreferences = getSharedPreferences("ImmichFramePrefs", MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("webview_url", url)
            putBoolean("hideButtons", hideButtons)
            apply()
        }
    }
}