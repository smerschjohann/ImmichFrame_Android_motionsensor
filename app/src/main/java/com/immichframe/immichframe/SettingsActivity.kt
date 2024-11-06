package com.immichframe.immichframe

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

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

            // Use a coroutine to perform network request asynchronously
            CoroutineScope(Dispatchers.Main).launch {
                val isSuccessful = checkUrlGetResponse(url)
                if (url.isNotEmpty() && isSuccessful) {
                    // Save the URL
                    saveUrl(url)
                    Toast.makeText(this@SettingsActivity, "URL saved!", Toast.LENGTH_SHORT).show()
                    setResult(Activity.RESULT_OK)
                    finish() // Close the settings activity and return to the previous activity
                } else {
                    Toast.makeText(this@SettingsActivity, "Your ImmichFrame URL is not valid!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun checkUrlGetResponse(url: String): Boolean {
        return withContext(Dispatchers.IO) { // Switch to IO context for network request
            val client = OkHttpClient()
            val request = Request.Builder().url("$url/api/Config").build()

            try {
                client.newCall(request).execute().use { response ->
                    response.isSuccessful
                }
            } catch (e: IOException) {
                e.printStackTrace()
                false
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
