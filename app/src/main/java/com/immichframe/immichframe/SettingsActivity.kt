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
    private lateinit var chkUseWebView: CheckBox
    private var useWebView = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        editTextUrl = findViewById(R.id.editTextUrl)
        buttonSaveUrl = findViewById(R.id.buttonSaveUrl)
        chkUseWebView = findViewById(R.id.chkUseWebView)
        // Load settings
        loadSettings()

        buttonSaveUrl.setOnClickListener {
            val url = editTextUrl.text.toString()
            if (url.isNotEmpty()) {
                saveSettings(url)
                Toast.makeText(this, "Settings saved!", Toast.LENGTH_SHORT).show()
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
    }

    private fun loadSettings() {
        val sharedPreferences = getSharedPreferences("ImmichFramePrefs", MODE_PRIVATE)
        val savedUrl = sharedPreferences.getString("webview_url", getString(R.string.webview_url))
        useWebView = sharedPreferences.getBoolean("useWebView", true)
        chkUseWebView.isChecked = useWebView
        editTextUrl.setText(savedUrl)
        editTextUrl.requestFocus()
    }

    private fun saveSettings(url: String) {
        useWebView = chkUseWebView.isChecked
        val sharedPreferences = getSharedPreferences("ImmichFramePrefs", MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("webview_url", url)
            putBoolean("useWebView", useWebView)
            apply()
        }
    }
}