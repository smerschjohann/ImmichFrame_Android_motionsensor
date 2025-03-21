package com.immichframe.immichframe

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    private lateinit var editTextUrl: EditText
    private lateinit var buttonSaveUrl: Button
    private lateinit var chkUseWebView: androidx.appcompat.widget.SwitchCompat
    private lateinit var chkBlurredBackground: androidx.appcompat.widget.SwitchCompat
    private lateinit var chkShowCurrentDate: androidx.appcompat.widget.SwitchCompat
    private lateinit var buttonAndroidSettings: Button
    private lateinit var editTextAuthSecret: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        editTextUrl = findViewById(R.id.editTextUrl)
        buttonSaveUrl = findViewById(R.id.buttonSaveUrl)
        chkUseWebView = findViewById(R.id.chkUseWebView)
        chkBlurredBackground = findViewById(R.id.chkBlurredBackground)
        chkShowCurrentDate = findViewById(R.id.chkShowCurrentDate)
        buttonAndroidSettings = findViewById(R.id.buttonAndroidSettings)
        editTextAuthSecret = findViewById(R.id.editTextAuthSecret)

        loadSettings()

        buttonSaveUrl.setOnClickListener {
            val url = editTextUrl.text.toString()
            val authSecret = editTextAuthSecret.text.toString()
            if (url.isNotEmpty()) {
                saveSettings(url, authSecret)
                Toast.makeText(this, "Settings saved!", Toast.LENGTH_SHORT).show()
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
        additionalSettingsVisibility(chkUseWebView.isChecked)
        chkUseWebView.setOnCheckedChangeListener { _, isChecked ->
            additionalSettingsVisibility(isChecked)
        }

        buttonAndroidSettings.setOnClickListener {
            val intent = Intent(Settings.ACTION_SETTINGS)
            startActivity(intent)
        }
    }

    private fun additionalSettingsVisibility(isChecked: Boolean) {
        if (isChecked) {
            chkBlurredBackground.visibility = View.GONE
            chkShowCurrentDate.visibility = View.GONE
            buttonAndroidSettings.visibility = View.GONE
        } else {
            chkBlurredBackground.visibility = View.VISIBLE
            chkShowCurrentDate.visibility = View.VISIBLE
            buttonAndroidSettings.visibility = View.VISIBLE
        }
    }

    private fun loadSettings() {
        val sharedPreferences = getSharedPreferences("ImmichFramePrefs", MODE_PRIVATE)
        val savedUrl = sharedPreferences.getString("webview_url", getString(R.string.webview_url))
        val blurredBackground = sharedPreferences.getBoolean("blurredBackground", true)
        val showCurrentDate = sharedPreferences.getBoolean("showCurrentDate", true)
        chkBlurredBackground.isChecked = blurredBackground
        chkShowCurrentDate.isChecked = showCurrentDate
        val useWebView = sharedPreferences.getBoolean("useWebView", true)
        val authSecret = sharedPreferences.getString("authSecret", "") ?: ""
        chkUseWebView.isChecked = useWebView
        editTextUrl.setText(savedUrl)
        editTextAuthSecret.setText(authSecret)
        editTextUrl.requestFocus()
    }

    private fun saveSettings(url: String, authSecret: String) {
        val useWebView = chkUseWebView.isChecked
        val blurredBackground = chkBlurredBackground.isChecked
        val showCurrentDate = chkShowCurrentDate.isChecked
        val sharedPreferences = getSharedPreferences("ImmichFramePrefs", MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("webview_url", url)
            putBoolean("useWebView", useWebView)
            putBoolean("blurredBackground", blurredBackground)
            putBoolean("showCurrentDate", showCurrentDate)
            putString("authSecret", authSecret)
            apply()
        }
    }
}